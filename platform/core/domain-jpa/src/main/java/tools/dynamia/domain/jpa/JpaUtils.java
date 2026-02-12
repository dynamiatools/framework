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
 * The Class JpaUtils.
 *
 * @author Mario Serrano Leones
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class JpaUtils {


    /**
     * Configure paginator.
     *
     * @param em           the em
     * @param query        the query
     * @param queryBuilder the query builder
     * @param params       the params
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
     * Sets the hibernate query cacheable.
     *
     * @param query the new hibernate query cacheable
     */
    public static void setHibernateQueryCacheable(Query query) {
        query.setHint("org.hibernate.cacheable", true);
    }


    /**
     * Check if the object is a JPA Entity. Return false if null
     *
     * @param entity the entity
     * @return true, if is JPA entity
     */
    public static boolean isJPAEntity(Object entity) {
        if (entity != null) {
            return isJPAEntity(entity.getClass());
        } else {
            return false;
        }
    }

    /**
     * Check if the class is a JPA Entity. Return false if null
     *
     * @param entityClass the entity class
     * @return true, if is JPA entity
     */
    public static boolean isJPAEntity(Class entityClass) {
        if (entityClass != null) {
            return entityClass.isAnnotationPresent(Entity.class);
        } else {
            return false;
        }

    }

    /**
     * Gets the JPA id value.
     *
     * @param entity the entity
     * @return the JPA id value
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


    public static JpaQuery wrap(Query query) {
        return new JpaQuery(query);
    }


    private JpaUtils() {
    }


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
     * Create an entity graph that include all relationships
     *
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
     * Initialize all entity properties, incluing toOne and toMany asocciation. Should be called inside a Transaction
     *
     */
    public static void initializeEntity(Object entity) {
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

            if (isToMany(p)) {
                var collection = ObjectOperations.invokeGetMethod(entity, p);
                if (collection != null) {
                    Hibernate.initialize(collection);
                }
            }
        });
    }
}
