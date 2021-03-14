/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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

import tools.dynamia.commons.BeanUtils;
import tools.dynamia.domain.Identifiable;
import tools.dynamia.domain.query.DataPaginator;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.util.QueryBuilder;

import javax.persistence.*;
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
            for (Field field : BeanUtils.getAllFields(entity.getClass())) {
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

}
