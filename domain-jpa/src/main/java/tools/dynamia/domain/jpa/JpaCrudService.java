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

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import tools.dynamia.commons.*;
import tools.dynamia.commons.collect.PagedList;
import tools.dynamia.commons.collect.PagedListDataSource;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.commons.reflect.PropertyInfo;
import tools.dynamia.domain.AbstractEntity;
import tools.dynamia.domain.OrderBy;
import tools.dynamia.domain.jdbc.QueryInterruptedException;
import tools.dynamia.domain.query.*;
import tools.dynamia.domain.services.ValidatorService;
import tools.dynamia.domain.services.impl.AbstractCrudService;
import tools.dynamia.domain.util.CrudServiceListener;
import tools.dynamia.domain.util.QueryBuilder;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.sterotypes.Service;
import tools.dynamia.io.converters.Converters;

import javax.annotation.PostConstruct;
import javax.persistence.*;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static tools.dynamia.domain.jpa.JpaUtils.wrap;
import static tools.dynamia.domain.query.QueryConditions.eq;
import static tools.dynamia.domain.query.QueryParameters.with;
import static tools.dynamia.domain.util.QueryBuilder.select;

/**
 * CrudService implementation.
 *
 * @author Ing. Mario Alejandro Serrano Leones
 * @since 1.0
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@Service("jpaCrudService")
public class JpaCrudService extends AbstractCrudService {

    public static final String HINT_FETCH_GRAPH = "javax.persistence.fetchgraph";
    public static final String HINT_LOAD_GRAPH = "javax.persistence.loadgraph";

    /**
     * The em.
     */
    @PersistenceContext
    private EntityManager em;


    private final PlatformTransactionManager txManager;
    private final ValidatorService validatorService;

    public JpaCrudService(PlatformTransactionManager txManager, ValidatorService validatorService) {
        this.txManager = txManager;
        this.validatorService = validatorService;
    }

    /**
     * The logger.
     */
    private final LoggingService logger = new SLF4JLoggingService(JpaCrudService.class);

    /**
     * Inits the.
     */
    @PostConstruct
    private void init() {
        logger.info("JpaCrudService started");
        logger.info(" ValidatorService: " + validatorService);
        logger.info(" EntityManager: " + em);
    }

    /*
     * (non-Javadoc)
     *
     * @see CrudService#getId(java.lang.Class,
     * QueryParameters)
     */
    @Override
    public Serializable getId(Class entityClass, QueryParameters params) {
        if (params == null) {
            throw new NullPointerException("QueryParameters are required to find entityClass id");
        }
        String queryText = select(entityClass, "e", "id").where(params).toString();
        Query query = em.createQuery(queryText);
        query.setMaxResults(1);
        params.applyTo(wrap(query));

        return (Serializable) query.getSingleResult();

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * AbstractCrudService#save(java.lang.
     * Object, java.io.Serializable)
     */
    @Override
    @Transactional
    public <T> T save(T t, Serializable id) {
        return super.save(t, id);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * AbstractCrudService#save(java.lang.
     * Object)
     */
    @Override
    @Transactional
    public <T> T save(T t) {
        if (t instanceof AbstractEntity) {
            return super.save(t);
        } else {
            Serializable id = JpaUtils.getJPAIdValue(t);
            return save(t, id);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see CrudService#create(java.lang.Object)
     */
    @Override
    @Transactional
    public <T> T create(T t) {
        fireListeners(t, EventType.BEFORE_CREATE);
        validatorService.validate(t);
        this.em.persist(t);
        fireListeners(t, EventType.AFTER_CREATE);
        return t;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * AbstractCrudService#find(java.lang.
     * Class, java.io.Serializable)
     */
    @Override
    public <T> T find(Class<T> type, final Serializable id) {
        if (id == null) {
            return null;
        }

        Object targetId = JpaUtils.checkIdType(type, id);
        if (targetId == null) return null;
        return em.find(type, targetId);
    }

    @Override
    public <T> T load(Class<T> type, Serializable id) {
        if (id == null) {
            return null;
        }

        Object targetId = JpaUtils.checkIdType(type, id);
        if (targetId == null) return null;

        var entityGraph = JpaUtils.createEntityGraph(type, em);


        return em.find(type, targetId, Map.of(HINT_LOAD_GRAPH, entityGraph));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * CrudService#findSingle(java.lang.Class,
     * java.lang.String, java.lang.Object)
     */
    @Override
    public <T> T findSingle(Class<T> type, String property, Object value) {
        QueryParameters qp = with(property, value).setAutocreateSearcheableStrings(false);
        return findSingle(type, qp);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * CrudService#findSingle(java.lang.Class,
     * QueryParameters)
     */
    @Override
    public <T> T findSingle(Class<T> type, QueryParameters parameters) {
        parameters.setType(type);
        fireListeners(parameters, EventType.BEFORE_QUERY);

        QueryBuilder qb = QueryBuilder.fromParameters(type, "t", parameters);

        String queryText = qb.toString();

        Query query = em.createQuery(queryText, type);
        query.setMaxResults(1);
        parameters.applyTo(wrap(query));

        List<T> resultList = new ArrayList();
        T result = null;
        try {
            resultList = query.getResultList();
            if (!resultList.isEmpty()) {
                result = resultList.get(0);
            }
        } catch (Exception e) {
        }

        fireListeners(resultList, EventType.AFTER_QUERY);
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see CrudService#delete(java.lang.Object)
     */
    @Override
    @Transactional
    public void delete(Object t) {
        fireListeners(t, EventType.BEFORE_DELETE);
        this.em.remove(t);
        fireListeners(t, EventType.AFTER_DELETE);
    }

    /*
     * (non-Javadoc)
     *
     * @see CrudService#delete(java.lang.Class,
     * java.io.Serializable)
     */
    @Override
    @Transactional
    public void delete(Class type, Serializable id) {
        Object entity = this.em.getReference(type, id);
        delete(entity);
    }

    @Override
    public void deleteAll(Class type) {
        execute("delete from " + type.getSimpleName() + " e", new QueryParameters());
    }

    /*
     * (non-Javadoc)
     *
     * @see CrudService#update(java.lang.Object)
     */
    @Override
    @Transactional
    public <T> T update(T t) {
        fireListeners(t, EventType.BEFORE_UPDATE);
        validatorService.validate(t);
        t = em.merge(t);
        fireListeners(t, EventType.AFTER_UPDATE);
        return t;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * CrudService#updateField(java.lang.Object,
     * java.lang.String, java.lang.Object)
     */
    @Override
    public void updateField(Object entity, String field, Object value) {
        String queryText = "update " + entity.getClass().getName() + " t set t." + field + "=:value where t=:entity";
        Query query = this.em.createQuery(queryText);
        query.setParameter("entity", entity);
        query.setParameter("value", value);
        query.executeUpdate();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * AbstractCrudService#increaseCounter(
     * java.lang.Object, java.lang.String)
     */
    @Override
    @Transactional
    public void increaseCounter(Object entity, String counterName) {
        String queryText = "update " + entity.getClass().getName() + " t set t." + counterName + "=" + counterName
                + "+1 where t=:entity";
        this.em.createQuery(queryText).setParameter("entity", entity).executeUpdate();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * AbstractCrudService#deacreaseCounter(
     * java.lang.Object, java.lang.String)
     */
    @Override
    @Transactional
    public void deacreaseCounter(Object entity, String counterName) {
        String queryText = "update " + entity.getClass().getName() + " t set t." + counterName + "=" + counterName
                + "-1 where t=:entity";
        this.em.createQuery(queryText).setParameter("entity", entity).executeUpdate();
    }

    /*
     * (non-Javadoc)
     *
     * @see CrudService#findAll(java.lang.Class)
     */
    @Override
    public <T> List<T> findAll(Class<T> type) {
        QueryParameters qp = new QueryParameters();
        return find(type, qp);
    }

    /*
     * (non-Javadoc)
     *
     * @see CrudService#findAll(java.lang.Class,
     * java.lang.String)
     */
    @Override
    public <T> List<T> findAll(Class<T> type, String orderBy) {
        QueryParameters qp = new QueryParameters();
        qp.orderBy(orderBy, true);
        return find(type, qp);
    }

    /*
     * (non-Javadoc)
     *
     * @see CrudService#find(java.lang.Class,
     * QueryParameters)
     */
    @Override
    public <T> List<T> find(Class<T> type, QueryParameters parameters) {
        parameters.setType(type);
        return executeQuery((QueryBuilder) null, parameters);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * CrudService#executeQuery(tools.dynamia.
     * domain.util.QueryBuilder, QueryParameters)
     */
    @Override
    public <T> List<T> executeQuery(QueryBuilder queryBuilder, QueryParameters parameters) {
        try {
            if (queryBuilder != null && parameters.getType() == null) {
                parameters.setType(queryBuilder.getType());
            }

            if (parameters.getType().isAnnotationPresent(OrderBy.class) && parameters.getSorter() == null) {
                OrderBy orderBy = parameters.getType().getAnnotation(OrderBy.class);
                if (orderBy != null) {
                    parameters.orderBy(orderBy.value());
                }
            }

            fireListeners(parameters, EventType.BEFORE_QUERY);

            if (queryBuilder == null && parameters != null && parameters.getType() != null) {
                queryBuilder = QueryBuilder.fromParameters(parameters.getType(), "t", parameters);
            } else if (queryBuilder == null) {
                throw new NullPointerException("Cannot execute query, QueryBuilder is null");
            }

            String queryText = queryBuilder.toString();

            logger.debug("Executing Query: " + queryText);

            Query query = queryBuilder.getResultType() == BeanMap.class ? em.createQuery(queryText, Tuple.class) : this.em.createQuery(queryText);

            if (parameters.getMaxResults() > 0 && parameters.getPaginator() == null) {
                query.setMaxResults(parameters.getMaxResults());
            }

            parameters.applyTo(wrap(query));
            JpaUtils.configurePaginator(em, query, queryBuilder, parameters);


            List result = mapResultsToBeanMaps(queryBuilder, query.getResultList());


            if (parameters.getPaginator() != null) {
                PagedListDataSource<T> dataSource = new JpaPagedListDataSource<>(
                        new QueryMetadata(queryText, queryBuilder, parameters), result);
                result = new PagedList<>(dataSource);
            }

            fireListeners(result, EventType.AFTER_QUERY);
            return result;
        } catch (Throwable ex) {
            if (ex.getCause() != null && ex.getCause().getCause() instanceof SQLException) {
                if (ex.getCause().getCause().getMessage().toLowerCase().contains("interrupted")) {
                    throw new QueryInterruptedException(ex.getCause().getCause().getMessage(), ex);
                }
            }
            throw ex;

        }
    }

    private List mapResultsToBeanMaps(QueryBuilder queryBuilder, List result) {
        if (queryBuilder != null && queryBuilder.getResultType() == BeanMap.class && result != null && !result.isEmpty()) {
            List beanMapResult = new ArrayList<>();
            for (Object o : result) {
                Tuple t = (Tuple) o;
                BeanMap element = new BeanMap();
                element.setBeanClass(queryBuilder.getType());
                element.setFields(queryBuilder.getFields());
                int pos = 0;
                for (String field : element.getFields()) {
                    element.set(field, t.get(pos));
                    if (field.equalsIgnoreCase("id")) {
                        element.setId(t.get(pos));
                    }
                    pos++;
                }
                beanMapResult.add(element);
            }
            result = beanMapResult;
        }
        return result;
    }

    @Override
    public <T> List<T> executeQuery(QueryBuilder queryBuilder) {
        return executeQuery(queryBuilder, queryBuilder.getQueryParameters());
    }

    @Override
    public <T> List<T> executeQuery(String queryText) {
        return executeQuery(queryText, null);
    }

    @Override
    public <T> List<T> executeQuery(String queryText, QueryParameters parameters) {

        Query query = em.createQuery(queryText);
        if (parameters != null) {
            parameters.applyTo(wrap(query));

            if (parameters.getMaxResults() > 0) {
                query.setMaxResults(parameters.getMaxResults());
            }
            if (parameters.getHints() != null) {
                parameters.getHints().forEach(query::setHint);
            }
        }

        return query.getResultList();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * CrudService#executeProjection(java.lang.
     * Class, java.lang.String, QueryParameters)
     */
    @Override
    public <T> T executeProjection(Class<T> resultClass, String projectionQueryText, QueryParameters parameters) {
        Query query = this.em.createQuery(projectionQueryText);
        parameters.applyTo(wrap(query));
        return (T) query.getSingleResult();
    }

    /*
     * (non-Javadoc)
     *
     * @see CrudService#execute(java.lang.String,
     * QueryParameters)
     */
    @Override
    public int execute(String queryText, QueryParameters parameters) {
        Query query = em.createQuery(queryText);
        if (parameters != null) {
            parameters.applyTo(wrap(query));
        }
        return query.executeUpdate();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * AbstractCrudService#find(tools.dynamia
     * .domain.query.QueryMetadata)
     */
    @Override
    public List find(QueryMetadata queryMetada) {
        Query query = null;
        if (queryMetada.getQueryBuilder() != null && queryMetada.getQueryBuilder().getResultType() == BeanMap.class) {
            query = em.createQuery(queryMetada.getText(), Tuple.class);
        } else {
            query = em.createQuery(queryMetada.getText());
        }
        queryMetada.getParameters().applyTo(wrap(query));
        JpaUtils.configurePaginator(em, query, null, queryMetada.getParameters());

        return mapResultsToBeanMaps(queryMetada.getQueryBuilder(), query.getResultList());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * CrudService#findByFields(java.lang.Class,
     * java.lang.String, java.lang.String[])
     */
    @Override
    public <T> List<T> findByFields(Class<T> type, String param, String... fields) {
        return findByFields(type, param, null, fields);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * CrudService#findByFields(java.lang.Class,
     * java.lang.String, QueryParameters,
     * java.lang.String[])
     */
    @Override
    public <T> List<T> findByFields(Class<T> entityClass, String param, QueryParameters defaultParams,
                                    String... fields) {
        List<T> result = null;


        QueryParameters params = new QueryParameters();
        if (defaultParams != null) {
            params.sort(defaultParams.getSorter());
            params.paginate(defaultParams.getPaginator());
            params.setMaxResults(defaultParams.getMaxResults());
            params.putAll(defaultParams);
        }

        QueryParameters fieldsParams = new QueryParameters();

        if (param == null || param.isEmpty()) {
            result = find(entityClass, params);
        } else if (fields.length > 0) {
            for (String fieldName : fields) {
                QueryCondition qc = createQueryCondition(entityClass, fieldName, param);
                if (qc != null) {
                    fieldsParams.add(fieldName, qc);
                }
            }

            if (!fieldsParams.isEmpty()) {
                params.addGroup(fieldsParams, BooleanOp.AND);
                result = find(entityClass, params);
            } else {
                result = Collections.EMPTY_LIST;
            }

        }

        return result;
    }

    /**
     * Creates the query condition.
     *
     * @param <T>         the generic type
     * @param entityClass the entity class
     * @param fieldName   the field name
     * @param value       the value
     * @return the query condition
     */
    private <T> QueryCondition createQueryCondition(Class<T> entityClass, String fieldName, String value) {
        QueryCondition qc = null;
        PropertyInfo pinfo = BeanUtils.getPropertyInfo(entityClass, fieldName);
        if (pinfo != null) {
            if (pinfo.getType().isEnum()) {
                try {
                    Class<? extends Enum> enumType = (Class<? extends Enum>) pinfo.getType();
                    Object obj = Enum.valueOf(enumType, value.toUpperCase());
                    qc = eq(obj, BooleanOp.OR);
                } catch (Exception e) {
                }
            } else if (pinfo.getType() == Boolean.class || pinfo.getType() == boolean.class) {
                qc = null;
            } else {
                try {
                    Object realValue = Converters.convert(pinfo.getType(), value);
                    if (realValue instanceof String) {
                        qc = QueryConditions.like(realValue, true, BooleanOp.OR);
                    } else {
                        qc = eq(realValue, BooleanOp.OR);
                    }
                } catch (Exception e) {
                }
            }
        }
        return qc;

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * AbstractCrudService#findByExample(java
     * .lang.Object, DataPaginator,
     * tools.dynamia.commons.BeanSorter)
     */
    @Override
    public <T> List<T> findByExample(T example, DataPaginator paginator, BeanSorter sorter) {
        QueryParameters params = new QueryParameters();
        params.paginate(paginator);
        params.sort(sorter);

        return findByExample(example, params);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * AbstractCrudService#findByExample(java
     * .lang.Object, QueryParameters)
     */
    @Override
    public <T> List<T> findByExample(T example, QueryParameters params) {
        if (example == null) {
            return null;
        }
        Class clazz = example.getClass();
        QueryParameters qp = new QueryExample(example).build();
        qp.putAll(params);
        qp.sort(params.getSorter());
        qp.paginate(params.getPaginator());
        return find(clazz, qp);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * AbstractCrudService#findByExample(java
     * .lang.Object)
     */
    @Override
    public <T> List<T> findByExample(T example) {
        return findByExample(example, null, null);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * AbstractCrudService#findWithNamedQuery
     * (java.lang.String)
     */
    @Override
    public List findWithNamedQuery(String queryName) {
        return findWithNamedQuery(queryName, 0);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * AbstractCrudService#findWithNamedQuery
     * (java.lang.String, int)
     */
    @Override
    public List findWithNamedQuery(String queryName, int resultLimit) {
        return findWithNamedQuery(queryName, null, resultLimit);
    }

    /* (non-Javadoc)
     *
     * @see
     * AbstractCrudService#findWithNamedQuery
     * (java.lang.String, QueryParameters)
     */
    @Override
    public List findWithNamedQuery(String namedQueryName, QueryParameters parameters) {
        return findWithNamedQuery(namedQueryName, parameters, 0);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * AbstractCrudService#findWithNamedQuery
     * (java.lang.String, QueryParameters, int)
     */
    @Override
    public List findWithNamedQuery(String namedQueryName, QueryParameters parameters, int resultLimit) {
        Query query = em.createNamedQuery(namedQueryName);
        if (resultLimit > 0) {
            query.setFirstResult(resultLimit);
        }
        if (parameters != null && !parameters.isEmpty()) {
            parameters.applyTo(wrap(query));
        }
        return query.getResultList();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * AbstractCrudService#findByNativeQuery(
     * java.lang.String, java.lang.Class)
     */
    @Override
    public List findByNativeQuery(String sql, Class type) {
        return em.createNativeQuery(sql, type).getResultList();
    }

    /*
     * (non-Javadoc)
     *
     * @see AbstractCrudService#
     * findSingleWithNameQuery(java.lang.String,
     * QueryParameters)
     */
    @Override
    public Object findSingleWithNameQuery(String namedQueryName, QueryParameters parameters) {
        List list = findWithNamedQuery(namedQueryName, parameters, 1);
        if (list != null && list.size() == 1) {
            return list.get(0);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * AbstractCrudService#find(java.lang.
     * Class, java.lang.String, java.lang.Object)
     */
    @Override
    public <T> List<T> find(Class<T> type, String property, Object value) {
        return find(type, with(property, value));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * AbstractCrudService#findIfNull(java.
     * lang.Class, java.lang.String)
     */
    @Override
    public <T> List<T> findIfNull(Class<T> type, String property) {
        String queryText = select(type, "e").where("e." + property + " is null ").toString();
        return em.createQuery(queryText).getResultList();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * AbstractCrudService#count(java.lang.
     * Class)
     */
    @Override
    public long count(Class type) {
        String queryText = select(type, "e").createProjection("count", "id");
        return (Long) em.createQuery(queryText).getSingleResult();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * AbstractCrudService#count(java.lang.
     * Class, QueryParameters)
     */
    @Override
    public long count(Class type, QueryParameters parameters) {

        QueryBuilder qb = QueryBuilder.fromParameters(type, "t", parameters);

        String queryText = qb.createProjection("count", "id");
        Query query = em.createQuery(queryText);
        parameters.applyTo(wrap(query));
        return (Long) query.getSingleResult();

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * AbstractCrudService#getReference(java.
     * lang.Class, java.io.Serializable)
     */
    @Override
    public <T> T getReference(Class<T> type, Serializable id) {
        return em.getReference(type, id);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * CrudService#getPropertyValues(java.lang.
     * Class, java.lang.String)
     */
    @Override
    public List getPropertyValues(Class entityClass, String property) {
        return getPropertyValues(entityClass, property, new QueryParameters().orderBy(property));
    }


    @Override
    public List getPropertyValues(Class entityClass, String property, QueryParameters params) {
        return executeQuery(select(property).from(entityClass, "p").where(params).groupBy(property), params);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * CrudService#batchUpdate(java.lang.Class,
     * java.lang.String, java.lang.Object,
     * QueryParameters)
     */
    @Override
    @Transactional
    public int batchUpdate(Class type, String field, Object value, QueryParameters params) {
        return batchUpdate(type, MapBuilder.put(field, value), params);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * CrudService#batchUpdate(java.lang.Class,
     * java.util.Map, QueryParameters)
     */
    @Override
    @Transactional
    public int batchUpdate(Class type, Map<String, Object> fieldvalues, QueryParameters params) {
        QueryBuilder qb;
        if (params != null) {
            qb = QueryBuilder.fromParameters(type, "e", params);
        } else {
            qb = select(type, "e");
        }

        StringBuilder sb = new StringBuilder();

        QueryParameters values = new QueryParameters();
        values.setAutocreateSearcheableStrings(false);

        sb.append("update ").append(type.getName()).append(" e set ");
        for (Map.Entry<String, Object> fv : fieldvalues.entrySet()) {
            String valuelabel = fv.getKey() + "newvalue";
            sb.append("e.").append(fv.getKey()).append("=").append(":").append(valuelabel).append(", ");
            values.add(valuelabel, fv.getValue());
        }

        String custom = sb.toString();

        qb.customSelect(custom.substring(0, custom.lastIndexOf(",")));
        qb.customFrom("");

        String fullUpdateQueryText = qb.toString();
        Query query = em.createQuery(fullUpdateQueryText);
        values.applyTo(wrap(query));
        if (params != null) {
            params.applyTo(wrap(query));
        }

        return query.executeUpdate();
    }

    /*
     * (non-Javadoc)
     *
     * @see CrudService#reload(java.lang.Object)
     */
    @Override
    public <T> T reload(T entity) {
        if (entity instanceof AbstractEntity) {
            entity = (T) find(entity.getClass(), ((AbstractEntity) entity).getId());
        } else {
            try {
                Serializable id = JpaUtils.getJPAIdValue(entity);
                if (id != null) {
                    entity = (T) find(entity.getClass(), id);
                }
            } catch (Exception e) {
            }
        }

        return entity;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * AbstractCrudService#getListeners()
     */
    @Override
    protected List<CrudServiceListener> getListeners() {
        return new ArrayList<>(Containers.get().findObjects(CrudServiceListener.class));
    }


    @Override
    public void executeWithinTransaction(Callback callback) {
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        tx.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                callback.doSomething();
            }
        });

    }

    @Override
    public Object getDelgate() {
        return em;
    }

    @Override
    public <T> T getFieldValue(Object entity, String fieldName, Class<T> fieldClass) {
        var query = select(fieldName).from(entity.getClass(), "e").where("e = :entity");
        return (T) em.createQuery(query.toString())
                .setParameter("entity", entity)
                .getSingleResult();

    }
}
