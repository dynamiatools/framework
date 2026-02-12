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
package tools.dynamia.domain.services;

import tools.dynamia.commons.BeanSorter;
import tools.dynamia.commons.Callback;
import tools.dynamia.domain.query.DataPaginator;
import tools.dynamia.domain.query.QueryMetadata;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.util.QueryBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Core service interface providing comprehensive CRUD (Create, Read, Update, Delete) operations for domain entities.
 * <p>
 * This interface defines the primary persistence layer abstraction in the Dynamia Tools framework, offering a rich set
 * of methods for entity management, querying, batch operations, and transaction handling. It serves as the main entry
 * point for data access operations across the application.
 * </p>
 *
 * <p>
 * <b>Key features:</b>
 * <ul>
 *   <li>Basic CRUD operations: create, read, update, delete, save (create or update)</li>
 *   <li>Advanced querying with {@link QueryParameters} and {@link QueryBuilder}</li>
 *   <li>Pagination support via {@link DataPaginator}</li>
 *   <li>Batch operations for efficient bulk processing</li>
 *   <li>Entity refresh, reload, and merge capabilities</li>
 *   <li>Flexible filtering, sorting, and searching</li>
 *   <li>Transaction management and callback support</li>
 *   <li>Listener mechanism for monitoring CRUD operations</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * @Autowired
 * private CrudService crudService;
 *
 * // Create a new entity
 * User user = new User("john@example.com", "John Doe");
 * user = crudService.create(user);
 *
 * // Find by ID
 * User found = crudService.find(User.class, user.getId());
 *
 * // Update
 * found.setName("John Smith");
 * crudService.update(found);
 *
 * // Query with parameters
 * List<User> activeUsers = crudService.find(User.class,
 *     QueryParameters.with("active", true)
 *         .orderBy("name", true));
 *
 * // Delete
 * crudService.delete(found);
 * }</pre>
 * </p>
 *
 * <p>
 * This interface is typically implemented by JPA-based or other ORM-specific services. The framework provides
 * a default implementation that integrates with JPA/Hibernate.
 * </p>
 *
 * @author Ing. Mario Serrano Leones
 * @see QueryParameters
 * @see DataPaginator
 * @see tools.dynamia.domain.util.CrudServiceListener
 * @since 1.0
 */
@SuppressWarnings({"rawtypes"})
public interface CrudService {

    /**
     * Find the entity class id using query parameters.
     *
     * @param entityClass the entity class
     * @param params      the params
     * @return the id
     */
    Serializable getId(Class entityClass, QueryParameters params);

    /**
     * Create or update the entity T, is id is null create is called, if not
     * update is called.
     *
     * @param <T> the generic type
     * @param t   the t
     * @param id  the id
     * @return the t
     */
    <T> T save(T t, Serializable id);

    /**
     * Create or update the entity T, is id is null create is called, if not
     * update.
     *
     * @param <T> the generic type
     * @param t   the t
     * @return the t
     */
    <T> T save(T t);

    /**
     * make the t entity persisted.
     *
     * @param <T> the generic type
     * @param t   the t
     * @return the t
     */
    <T> T create(T t);

    /**
     * find an entity by id.
     *
     * @param <T>  the generic type
     * @param type the type
     * @param id   the id
     * @return the t
     */
    <T> T find(Class<T> type, Serializable id);

    /**
     * Load and entity and all its relationship graph
     */
    <T> T load(Class<T> type, Serializable id);

    /**
     * A shortcut method to find an entity by a field called remoteId. If this field dont exist an exception
     * will throw
     *
     */
    <T> T findByRemoteId(Class<T> type, Serializable remoteId);

    /**
     * update (reattach or merge) the entity t.
     *
     * @param <T> the generic type
     * @param t   the t
     * @return the t
     */
    <T> T update(T t);

    /**
     * update (reattach or merge) the entity t.
     *
     * @param <T> the generic type
     * @param t   the t
     */
    <T> void delete(T t);

    /**
     * delete entity by id.
     *
     * @param type the type
     * @param id   the id
     */
    void delete(Class type, Serializable id);

    /**
     * Delete all entities. Use it with caution.
     *
     */
    void deleteAll(Class type);

    /**
     * do a bulk update to change the field value.
     *
     * @param entity the entity
     * @param field  the field
     * @param value  the value
     */
    void updateField(Object entity, String field, Object value);

    /**
     * Usuful for counters, increase its value. A counter is just a common
     * numeric field in the entity
     *
     * @param entity      the entity
     * @param counterName the counter name
     */
    void increaseCounter(Object entity, String counterName);

    /**
     * Usuful for counters, deacrease its value. A counter is just a common
     * numeric field in the entity
     *
     * @param entity      the entity
     * @param counterName the counter name
     */
    void deacreaseCounter(Object entity, String counterName);

    /**
     * Find all entities from database.
     *
     * @param <T>  the generic type
     * @param type the type
     * @return the list
     */
    <T> List<T> findAll(Class<T> type);

    /**
     * Find all entities from database ordered.
     *
     * @param <T>     the generic type
     * @param type    the type
     * @param orderBy the order by
     * @return the list
     */
    <T> List<T> findAll(Class<T> type, String orderBy);

    /**
     * Find entities using parameters.
     *
     * @param <T>        the generic type
     * @param type       the type
     * @param parameters the parameters
     * @return the list
     */
    <T> List<T> find(Class<T> type, QueryParameters parameters);

    /**
     * Execute query.
     *
     * @param <T>          the generic type
     * @param queryBuilder the query builder
     * @param parameters   the parameters
     * @return the list
     */
    <T> List<T> executeQuery(QueryBuilder queryBuilder, QueryParameters parameters);

    /**
     * Execute query.
     *
     * @param <T>          the generic type
     * @param queryBuilder the query builder
     * @return the list
     */
    <T> List<T> executeQuery(QueryBuilder queryBuilder);

    /**
     * Execute plain query
     *
     */
    <T> List<T> executeQuery(String queryText);

    /**
     * Execute plain query with parameters
     *
     */
    <T> List<T> executeQuery(String queryText, QueryParameters parameters);

    /**
     * Execute a query update using queryParameters.
     *
     * @param queryText  the query text
     * @param parameters the parameters
     * @return rows updated
     */
    int execute(String queryText, QueryParameters parameters);

    /**
     * Execute a query builder and return affected rows or entries. Userfull for UPDATE or DELETE queries
     *
     */
    default int execute(QueryBuilder query) {
        return execute(query.toString(), query.getQueryParameters());
    }

    /**
     * Find by example.
     *
     * @param <T>     the generic type
     * @param example the example
     * @return the list
     */
    <T> List<T> findByExample(T example);

    /**
     * Find by example.
     *
     * @param <T>       the generic type
     * @param example   the example
     * @param paginator the paginator
     * @param sorter    the sorter
     * @return the list
     */
    <T> List<T> findByExample(T example, DataPaginator paginator, BeanSorter sorter);

    /**
     * Find by example.
     *
     * @param <T>     the generic type
     * @param example the example
     * @param params  the params
     * @return the list
     */
    <T> List<T> findByExample(T example, QueryParameters params);

    /**
     * Find.
     *
     * @param <T>      the generic type
     * @param type     the type
     * @param property the property
     * @param value    the value
     * @return the list
     */
    <T> List<T> find(Class<T> type, String property, Object value);

    /**
     * Find with named query.
     *
     * @param queryName the query name
     * @return the list
     */
    List findWithNamedQuery(String queryName);

    /**
     * Find with named query.
     *
     * @param queryName   the query name
     * @param resultLimit the result limit
     * @return the list
     */
    List findWithNamedQuery(String queryName, int resultLimit);

    /**
     * Find with named query.
     *
     * @param namedQueryName the named query name
     * @param parameters     the parameters
     * @return the list
     */
    List findWithNamedQuery(String namedQueryName, QueryParameters parameters);

    /**
     * Find with named query.
     *
     * @param namedQueryName the named query name
     * @param parameters     the parameters
     * @param resultLimit    the result limit
     * @return the list
     */
    List findWithNamedQuery(String namedQueryName, QueryParameters parameters, int resultLimit);

    /**
     * Find single with name query.
     *
     * @param namedQueryName the named query name
     * @param parameters     the parameters
     * @return the object
     */
    Object findSingleWithNameQuery(String namedQueryName, QueryParameters parameters);

    /**
     * Find by native query.
     *
     * @param sql  the sql
     * @param type the type
     * @return the list
     */
    List findByNativeQuery(String sql, Class type);

    /**
     * Count.
     *
     * @param type       the type
     * @param parameters the parameters
     * @return the long
     */
    long count(Class type, QueryParameters parameters);

    /**
     * Gets the reference.
     *
     * @param <T>  the generic type
     * @param type the type
     * @param id   the id
     * @return the reference
     */
    <T> T getReference(Class<T> type, Serializable id);

    /**
     * Find if null.
     *
     * @param <T>      the generic type
     * @param type     the type
     * @param property the property
     * @return the list
     */
    <T> List<T> findIfNull(Class<T> type, String property);

    /**
     * Count.
     *
     * @param type the type
     * @return the long
     */
    long count(Class type);

    /**
     * find a single entity by property value.
     *
     * @param <T>      the generic type
     * @param type     the type
     * @param property the property
     * @param value    the value
     * @return the t
     */
    <T> T findSingle(Class<T> type, String property, Object value);

    /**
     * Find the first result of the query using parameters.
     *
     * @param <T>         the generic type
     * @param entityClass the entity class
     * @param params      the params
     * @return the t
     */
    <T> T findSingle(Class<T> entityClass, QueryParameters params);

    /**
     * find objects using fields.
     *
     * @param <T>    the generic type
     * @param type   the type
     * @param param  the param
     * @param fields the fields
     * @return the list
     */
    <T> List<T> findByFields(Class<T> type, String param, String... fields);

    /**
     * Find entities by fields.
     *
     * @param <T>           the generic type
     * @param type          the type
     * @param param         the param
     * @param defaultParams parameters that should be included in query
     * @param fields        the fields
     * @return the list
     */
    <T> List<T> findByFields(Class<T> type, String param, QueryParameters defaultParams, String... fields);

    /**
     * Query all values for the specified property. The returned list dont have
     * any duplicate values this is usefull to build filters list
     *
     * @param entityClass the entity class
     * @param property    the property
     * @return the property values
     */
    List getPropertyValues(Class<?> entityClass, String property);

    /**
     * Query all values for the specified property. The returned list dont have
     * any duplicate values this is usefull to build filters list
     *
     * @param entityClass the entity class
     * @param property    the property
     * @return the property values
     */
    List getPropertyValues(Class entityClass, String property, QueryParameters params);

    /**
     * Perform a query using the precache queryMetadata, usefull for send the
     * same query but with different parameters.
     *
     * @param queryMetadata the query metadata
     * @return the list
     */
    List find(QueryMetadata queryMetadata);


    /**
     * Perform a direct bulk update.
     *
     * @param type   the type
     * @param field  the field
     * @param value  the value
     * @param params the params
     * @return the int
     */
    int batchUpdate(Class type, String field, Object value, QueryParameters params);

    /**
     * perform a direct bulk update.
     *
     * @param type        the type
     * @param fieldvalues the fieldvalues
     * @param params      the params
     * @return the int
     */
    int batchUpdate(Class type, Map<String, Object> fieldvalues, QueryParameters params);

    /**
     * Refresh or reload de current entity.
     *
     * @param <T>    the generic type
     * @param entity the entity
     * @return the t
     */
    <T> T reload(T entity);

    /**
     * Execute a query projection like count, sum, max, avg, etc. It returns a
     * single value result.
     *
     * @param <T>                 the generic type
     * @param resultClass         the result class
     * @param projectionQueryText the projection query text
     * @param parameters          the parameters
     * @return the t
     */
    <T> T executeProjection(Class<T> resultClass, String projectionQueryText, QueryParameters parameters);

    /**
     * Execute a query projection like count, sum, max, avg, etc. It returns a
     * single value result.
     *
     * @param <T>         the generic type
     * @param resultClass the result class
     * @param query       the query builder
     * @return the t
     */
    default <T> T executeProjection(Class<T> resultClass, QueryBuilder query) {
        return executeProjection(resultClass, query.toString(), query.getQueryParameters());
    }


    /**
     * Execute callback in a new transaction
     *
     */
    void executeWithinTransaction(Callback callback);

    /**
     * Execute save method in a new transaction
     *
     */
    default void saveWithinTransaction(Object entity) {
        executeWithinTransaction(() -> save(entity));
    }

    /**
     * Find the first result of given type
     *
     */
    default <T> T findFirst(Class<T> type) {
        return findSingle(type, new QueryParameters());
    }

    /**
     * Return the delegate object of this service, for example if this service is a proxy to a JPA implementation it will return the EntityManager or Session object.
     *
     * @return the delegate object
     */
    Object getDelgate();


    /**
     * Query entity field. It execute a query like this 'select e.field from Entity e where e.id = :id'
     *
     */
    <T> T getFieldValue(Object entity, String fieldName, Class<T> fieldClass);
}
