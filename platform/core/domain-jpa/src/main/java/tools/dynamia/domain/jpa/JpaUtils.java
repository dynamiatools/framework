/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.domain.jpa;

import jakarta.persistence.*;
import org.hibernate.Hibernate;
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.Identifiable;
import tools.dynamia.commons.reflect.PropertyInfo;
import tools.dynamia.domain.query.DataPaginator;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.util.QueryBuilder;
import tools.dynamia.io.converters.Converters;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Static helpers for working with JPA entities, queries and Hibernate-specific features.
 * <p>
 * Groups small utilities used across the JPA layer of the framework:
 * <ul>
 *     <li>Pagination of JPA {@link Query} instances ({@link #configurePaginator}).</li>
 *     <li>Query hints and wrapping ({@link #setHibernateQueryCacheable}, {@link #wrap}).</li>
 *     <li>Entity inspection ({@link #isJPAEntity(Object)}, {@link #getJPAIdValue}, {@link #checkIdType}).</li>
 *     <li>Association loading ({@link #createEntityGraph}, {@link #initializeEntity(Object, boolean)}).</li>
 * </ul>
 * This class is not instantiable.
 * <p>
 * Example:
 * <pre>{@code
 * if (JpaUtils.isJPAEntity(obj)) {
 *     Serializable id = JpaUtils.getJPAIdValue(obj);
 * }
 * }</pre>
 *
 * @author Mario Serrano Leones
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class JpaUtils {


    /**
     * Applies the paginator found in the given {@link QueryParameters} to a JPA query.
     * <p>
     * When the paginator has no total size yet ({@code 0}) and a {@link QueryBuilder} is provided, a
     * {@code count(id)} query is built and executed first, applying the same parameters, so the paginator
     * knows the total number of records. If the count query returns several rows (for example because of a
     * group by), their values are added up. Finally, the first result and the page size are set on the query.
     * <p>
     * Does nothing if the paginator or the query is {@code null}.
     *
     * @param em           the entity manager used to create the count query
     * @param query        the query to paginate, may be {@code null}
     * @param queryBuilder the builder used to derive the count query, may be {@code null} to skip counting
     * @param params       the query parameters holding the {@link DataPaginator}
     */
    public static void configurePaginator(EntityManager em, Query query, QueryBuilder queryBuilder, QueryParameters params) {
        DataPaginator paginator = params.getPaginator();
        if (paginator != null && query != null) {
            if (paginator.getTotalSize() == 0 && queryBuilder != null) {
                Query counter = em.createQuery(queryBuilder.createProjection("count", "id"));
                JpaQuery jpaQuery = new JpaQuery(counter);
                params.applyTo(jpaQuery);
                long count = 0;
                try {
                    count = (Long) counter.getSingleResult();
                } catch (NonUniqueResultException e) {
                    List<Long> result = counter.getResultList();
                    for (Long res : result) {
                        count += res;
                    }
                }
                paginator.setTotalSize(count);
            }
            query.setFirstResult(paginator.getFirstResult());
            query.setMaxResults(paginator.getPageSize());
        }
    }

    /**
     * Marks a query as cacheable in the Hibernate second-level query cache by setting the
     * {@code org.hibernate.cacheable} hint. Only effective if the query cache is enabled in Hibernate.
     *
     * @param query the query to mark as cacheable, must not be {@code null}
     */
    public static void setHibernateQueryCacheable(Query query) {
        query.setHint("org.hibernate.cacheable", true);
    }


    /**
     * Checks whether the class of the given object is a JPA entity, that is, annotated with {@link Entity}.
     *
     * @param entity the object to check, may be {@code null}
     * @return {@code true} if the object's class is a JPA entity, {@code false} otherwise or if the object is
     * {@code null}
     * @see #isJPAEntity(Class)
     */
    public static boolean isJPAEntity(Object entity) {
        if (entity != null) {
            return isJPAEntity(entity.getClass());
        } else {
            return false;
        }
    }

    /**
     * Checks whether the given class is a JPA entity, that is, annotated with {@link Entity}.
     * <p>
     * Only the annotation on the class itself is checked; Hibernate proxy subclasses are not unwrapped.
     *
     * @param entityClass the class to check, may be {@code null}
     * @return {@code true} if the class is annotated with {@link Entity}, {@code false} otherwise or if the
     * class is {@code null}
     */
    public static boolean isJPAEntity(Class entityClass) {
        if (entityClass != null) {
            return entityClass.isAnnotationPresent(Entity.class);
        } else {
            return false;
        }

    }

    /**
     * Gets the identifier value of an entity.
     * <p>
     * If the entity implements {@link Identifiable} its {@code getId()} is used. Otherwise the first field
     * annotated with {@link Id} (searching the whole class hierarchy) is read reflectively, even if it is
     * private.
     *
     * @param entity the entity instance, must not be {@code null}
     * @return the identifier value, which may be {@code null} for a not yet persisted entity
     * @throws PersistenceException if the object is not a JPA entity, no {@link Id} field exists, or the field
     *                              cannot be read
     */
    public static Serializable getJPAIdValue(Object entity) {
        if (entity instanceof Identifiable) {
            return ((Identifiable) entity).getId();
        }

        if (isJPAEntity(entity)) {
            for (Field field : ObjectOperations.getAllFields(entity.getClass())) {
                if (field.isAnnotationPresent(Id.class)) {
                    try {
                        field.setAccessible(true);
                        return (Serializable) field.get(entity);
                    } catch (Exception e) {
                        throw new PersistenceException("Cannot get @Id valuein JPA Entity " + entity.getClass(), e);
                    }
                }
            }
        } else {
            throw new PersistenceException(entity.getClass().getName() + " is not a JPA Entity");
        }
        throw new PersistenceException("Cannot find @Id annotation in " + entity.getClass());
    }


    /**
     * Checks whether the given entity has not been persisted yet, that is, its identifier is {@code null}.
     * <p>
     * Note that entities with a manually assigned identifier are reported as not new.
     *
     * @param entity the entity instance, must not be {@code null}
     * @return {@code true} if the identifier value is {@code null}
     * @throws PersistenceException if the object is not a JPA entity or has no {@link Id} field
     * @see #getJPAIdValue(Object)
     */
    public static boolean isNew(Object entity) {
        return getJPAIdValue(entity) == null;
    }

    /**
     * Checks whether two objects represent the same persistent entity: both are non-null JPA entities of the
     * same real class (Hibernate proxies are resolved) with equal, non-null identifiers.
     * <p>
     * Example:
     * <pre>{@code
     * boolean same = JpaUtils.isSameEntity(invoice, invoiceProxy);
     * }</pre>
     *
     * @param a the first entity, may be {@code null}
     * @param b the second entity, may be {@code null}
     * @return {@code true} if both refer to the same persisted row, {@code false} otherwise (including when
     * either is {@code null}, not an entity, or not persisted yet)
     */
    public static boolean isSameEntity(Object a, Object b) {
        if (a == null || b == null) {
            return false;
        }
        Class<?> classA = getEntityClass(a);
        if (!isJPAEntity(classA) || !classA.equals(getEntityClass(b))) {
            return false;
        }
        Serializable idA = getJPAIdValue(a);
        return idA != null && idA.equals(getJPAIdValue(b));
    }

    /**
     * Gets the real class of an object, resolving Hibernate proxies to the underlying entity class.
     *
     * @param entity the object, may be {@code null}
     * @return the real class, or {@code null} if the object is {@code null}
     */
    public static Class<?> getEntityClass(Object entity) {
        return entity != null ? Hibernate.getClass(entity) : null;
    }

    /**
     * Gets the entity name used in JPQL queries: the {@code name} attribute of {@link Entity} when set,
     * otherwise the simple class name, as defined by the JPA specification.
     *
     * @param entityClass the entity class, must not be {@code null}
     * @return the JPQL entity name
     * @throws PersistenceException if the class is not annotated with {@link Entity}
     */
    public static String getEntityName(Class<?> entityClass) {
        Entity entity = entityClass.getAnnotation(Entity.class);
        if (entity == null) {
            throw new PersistenceException(entityClass.getName() + " is not a JPA Entity");
        }
        return entity.name().isBlank() ? entityClass.getSimpleName() : entity.name();
    }

    /**
     * Returns the real entity behind a Hibernate proxy, initializing it if needed. Objects that are not
     * proxies are returned as they are.
     *
     * @param entity the entity or proxy, may be {@code null}
     * @param <T>    the entity type
     * @return the unproxied entity, or {@code null} if the argument is {@code null}
     */
    public static <T> T unproxy(T entity) {
        return (T) Hibernate.unproxy(entity);
    }

    /**
     * Wraps a JPA {@link Query} in a {@link JpaQuery}, the framework's adapter used to apply
     * {@link QueryParameters} to it.
     *
     * @param query the JPA query to wrap
     * @return a new {@link JpaQuery} delegating to the given query
     */
    public static JpaQuery wrap(Query query) {
        return new JpaQuery(query);
    }


    private JpaUtils() {
    }


    /**
     * Converts a textual identifier to the type of the {@link Id} field of the given entity class.
     * <p>
     * Only {@link String} identifiers are converted, using the registered {@link Converters}. In every other
     * case, or when the {@code Id} field is itself a {@code String}, no converter exists or the conversion
     * fails, the original identifier is returned unchanged. A blank {@link String} yields {@code null}.
     * <p>
     * Example:
     * <pre>{@code
     * Object id = JpaUtils.checkIdType(Customer.class, "123"); // Long 123 if Customer.id is a Long
     * }</pre>
     *
     * @param type the entity class whose {@link Id} field defines the target type
     * @param id   the identifier to convert, may be {@code null}
     * @param <T>  the entity type
     * @return the identifier converted to the {@code Id} type, the original value if it cannot be converted,
     * or {@code null} if the identifier is a blank string
     */
    public static <T> Object checkIdType(Class<T> type, Serializable id) {
        Object targetId = id;
        if (id instanceof String) {
            if (id.toString().isBlank()) {
                return null;
            }

            //check id type and convert
            var field = ObjectOperations.getFirstFieldWithAnnotation(type, Id.class);
            if (field != null && field.getType() != String.class) {
                var converter = Converters.getConverter(field.getType());
                if (converter != null) {
                    try {
                        var value = converter.toObject(id.toString());
                        if (value != null) {
                            targetId = value;
                        }
                    } catch (Exception e) {
//cannot convert
                    }
                }
            }
        }
        return targetId;
    }

    /**
     * Creates an {@link EntityGraph} for the given entity type that includes its relationships.
     * <p>
     * To-one associations ({@link OneToOne}, {@link ManyToOne}) are added as attribute nodes and to-many
     * associations ({@link OneToMany}, {@link ManyToMany}) as subgraphs. Nested associations of the to-many
     * subgraphs are not expanded. The graph is typically passed to a query as the
     * {@code jakarta.persistence.fetchgraph} or {@code jakarta.persistence.loadgraph} hint.
     * <p>
     * Example:
     * <pre>{@code
     * var graph = JpaUtils.createEntityGraph(Invoice.class, em);
     * var invoice = em.find(Invoice.class, id, Map.of("jakarta.persistence.fetchgraph", graph));
     * }</pre>
     *
     * @param type the entity class
     * @param em   the entity manager used to create the graph
     * @param <T>  the entity type
     * @return a new entity graph for the given type
     */
    public static <T> EntityGraph<T> createEntityGraph(Class<T> type, EntityManager em) {
        var graph = em.createEntityGraph(type);
        var properties = ObjectOperations.getPropertiesInfo(type);

        properties.forEach(p -> {
            if (isToOne(p)) {
                graph.addAttributeNodes(p.getName());
            }

            if (isToMany(p)) {
                graph.addSubgraph(p.getName());
            }
        });

        return graph;
    }

    private static void loadSubgraph(Subgraph<Object> subgraph, PropertyInfo collection) {
        if (collection.getGenericType() != null) {
            var subproperties = ObjectOperations.getPropertiesInfo(collection.getGenericType());

            subproperties.forEach(p -> {
                if (isToOne(p)) {
                    subgraph.addAttributeNodes(p.getName());
                }

                if (isToMany(p)) {
                    var innerSubgraph = subgraph.addSubgraph(p.getName());
                    loadSubgraph(innerSubgraph, p);
                }
            });
        }
    }

    private static boolean isToMany(PropertyInfo p) {
        return p.isCollection() && (p.isAnnotationPresent(OneToMany.class) || p.isAnnotationPresent(ManyToMany.class));
    }

    private static boolean isToOne(PropertyInfo p) {
        return p.isAnnotationPresent(OneToOne.class) || p.isAnnotationPresent(ManyToOne.class);
    }


    /**
     * Initializes the entity and its to-one associations, without loading to-many collections.
     * Equivalent to {@code initializeEntity(entity, false)}.
     *
     * @param entity the entity to initialize, may be {@code null}
     * @see #initializeEntity(Object, boolean)
     */
    public static void initializeEntity(Object entity) {
        initializeEntity(entity, false);
    }

    /**
     * Forces the initialization of a (possibly lazy) entity and its associations using
     * {@link Hibernate#initialize(Object)}, so they can be used after the persistence context is closed.
     * <p>
     * The entity itself and its to-one associations ({@link OneToOne}, {@link ManyToOne}) are always
     * initialized; to-many collections ({@link OneToMany}, {@link ManyToMany}) only when
     * {@code includeToMany} is {@code true}. Only the first level of associations is initialized. Must be
     * called inside an active transaction or session.
     * <p>
     * Example:
     * <pre>{@code
     * Invoice invoice = crudService.find(Invoice.class, id);
     * JpaUtils.initializeEntity(invoice, true); // also loads invoice.getItems()
     * }</pre>
     *
     * @param entity        the entity to initialize, does nothing if {@code null}
     * @param includeToMany whether to-many collections should be initialized too
     */
    public static void initializeEntity(Object entity, boolean includeToMany) {
        if (entity == null) {
            return;
        }

        Hibernate.initialize(entity);
        var properties = ObjectOperations.getPropertiesInfo(entity.getClass());

        properties.forEach(p -> {
            if (isToOne(p)) {
                var property = ObjectOperations.invokeGetMethod(entity, p);
                if (property != null) {
                    Hibernate.initialize(property);
                }
            }

            if (includeToMany && isToMany(p)) {
                var collection = ObjectOperations.invokeGetMethod(entity, p);
                if (collection != null) {
                    Hibernate.initialize(collection);
                }
            }
        });
    }
}
