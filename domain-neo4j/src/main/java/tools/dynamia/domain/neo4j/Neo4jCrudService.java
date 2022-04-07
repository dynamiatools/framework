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

package tools.dynamia.domain.neo4j;

import org.neo4j.ogm.cypher.BooleanOperator;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.springframework.transaction.annotation.Transactional;
import tools.dynamia.commons.BeanSorter;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.Callback;
import tools.dynamia.commons.collect.CollectionsUtils;
import tools.dynamia.commons.reflect.PropertyInfo;
import tools.dynamia.domain.query.*;
import tools.dynamia.domain.services.GraphCrudService;
import tools.dynamia.domain.services.ValidatorService;
import tools.dynamia.domain.services.impl.AbstractCrudService;
import tools.dynamia.domain.util.CrudServiceListener;
import tools.dynamia.domain.util.QueryBuilder;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.sterotypes.Service;
import tools.dynamia.io.converters.Converters;

import java.io.Serializable;
import java.util.*;

@Service("neo4jCrudService")
public class Neo4jCrudService extends AbstractCrudService implements GraphCrudService {


    private final Session session;
    private final ValidatorService validatorService;

    public Neo4jCrudService(Session session, ValidatorService validatorService) {
        this.session = session;
        this.validatorService = validatorService;
    }

    private Session s() {
        return session;
    }

    @Override
    public <T> T find(Class<T> type, Serializable id) {
        return s().load(type, id);
    }

    @Override
    public <T> T find(Class<T> type, Serializable id, int depth) {
        return s().load(type, id, depth);
    }

    @Override
    public Serializable getId(Class entityClass, QueryParameters params) {
        Object result = findSingle(entityClass, params);
        if (result != null) {
            return s().resolveGraphIdFor(result);
        }
        return null;
    }

    @Override
    @Transactional
    public <T> T create(T t) {
        if (t != null) {
            fireListeners(t, EventType.BEFORE_CREATE);
            validatorService.validate(t);
            s().save(t);
            fireListeners(t, EventType.AFTER_CREATE);
        }
        return t;

    }

    @Override
    @Transactional
    public <T> T update(T t) {
        if (t != null) {
            fireListeners(t, EventType.BEFORE_UPDATE);
            validatorService.validate(t);
            s().save(t);
            fireListeners(t, EventType.AFTER_UPDATE);
        }
        return t;
    }

    /**
     * Save entity with depth
     *
     * @param t
     * @param depth
     */
    @Transactional
    @Override
    public <T> void save(T t, int depth) {
        if (t != null) {
            fireListeners(t, EventType.BEFORE_UPDATE);
            validatorService.validate(t);
            s().save(t, depth);
            fireListeners(t, EventType.AFTER_UPDATE);
        }
    }

    @Override
    @Transactional
    public <T> void delete(T t) {
        fireListeners(t, EventType.BEFORE_DELETE);
        s().delete(t);
        fireListeners(t, EventType.AFTER_DELETE);
    }

    @Override
    public void delete(Class type, Serializable id) {
        delete(s().load(type, id));
    }

    @Override
    public void deleteAll(Class type) {
        s().deleteAll(type);

    }

    @Override
    @Transactional
    public void updateField(Object entity, String field, Object value) {
        BeanUtils.setFieldValue(field, entity, value);
        s().save(entity);
    }

    @Override
    public <T> List<T> findAll(Class<T> type) {
        return new ArrayList<>(s().loadAll(type));
    }

    @Override
    public <T> List<T> findAll(Class<T> type, String orderBy) {
        SortOrder order = new SortOrder();
        order.add(orderBy);
        return new ArrayList<>(s().loadAll(type, order));
    }

    @Override
    public <T> List<T> find(Class<T> type, QueryParameters parameters) {
        parameters.setType(type);
        fireListeners(parameters, EventType.BEFORE_QUERY);

        Filters filters = buildFilters(parameters);
        SortOrder sortOrder = new SortOrder();
        Pagination pagination = null;
        int depth = getDepth(parameters);

        if (parameters.getPaginator() != null) {
            long totalSize = s().count(type, filters);
            parameters.getPaginator().setTotalSize(totalSize);
            pagination = buildPagination(parameters);
        }

        if (parameters.getSorter() != null) {
            BeanSorter s = parameters.getSorter();
            sortOrder.add(s.isAscending() ? SortOrder.Direction.ASC : SortOrder.Direction.DESC, s.getColumnName());
        }

        List<T> result = new ArrayList<>(s().loadAll(type, filters, sortOrder, pagination, depth));

        fireListeners(parameters, EventType.AFTER_QUERY);
        return result;

    }

    @Override
    public <T> List<T> executeQuery(QueryBuilder queryBuilder, QueryParameters parameters) {
        throw new UnsupportedOperationException("No yet supported");
    }

    @Override
    public <T> List<T> executeQuery(QueryBuilder queryBuilder) {
        throw new UnsupportedOperationException("No yet supported");
    }

    @Override
    public <T> List<T> executeQuery(String queryText) {
        return executeQuery(queryText, new QueryParameters());
    }

    @Override
    public <T> List<T> executeQuery(String queryText, QueryParameters parameters) {
        Result result = s().query(queryText, parameters);
        return CollectionsUtils.iteratorToList(result.iterator());
    }

    @Override
    public int execute(String queryText, QueryParameters parameters) {
        Result result = s().query(queryText, parameters);
        return 0;
    }

    @Override
    public <T> T findSingle(Class<T> type, String property, Object value) {
        if ("id".equalsIgnoreCase(property)) {
            return s().load(type, (Serializable) value);
        } else {
            Optional<T> result = s().loadAll(type, new Filter(property, ComparisonOperator.EQUALS, value), new Pagination(0, 1)).stream()
                    .findFirst();

            return result.orElse(null);
        }
    }

    @Override
    public <T> T findSingle(Class<T> entityClass, QueryParameters params) {

        Optional<T> result;
        if (params.size() == 1 && params.containsKey("id")) {
            result = Optional.of(s().load(entityClass, (Serializable) params.get("id"), params.getDepth()));
        } else {
            params.setType(entityClass);
            fireListeners(params, EventType.BEFORE_QUERY);
            Filters filters = buildFilters(params);
            SortOrder sortOrder = new SortOrder();
            Pagination pagination = new Pagination(0, 1);
            int depth = getDepth(params);
            Collection<T> data = s().loadAll(entityClass, filters, sortOrder, pagination, depth);
            result = data.stream().findFirst();
            fireListeners(params, EventType.AFTER_QUERY);
        }

        return result.orElse(null);
    }

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

    @Override
    public <T> T queryObject(Class<T> type, String query, QueryParameters params) {
        return s().queryForObject(type, query, params);
    }

    @Override
    public <T> Iterable<T> query(Class<T> type, String query, QueryParameters params) {
        return s().query(type, query, params);

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
                    qc = QueryConditions.eq(obj, BooleanOp.OR);
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
                        qc = QueryConditions.eq(realValue, BooleanOp.OR);
                    }
                } catch (Exception e) {
                }
            }
        }
        return qc;

    }

    @Override
    public List findByNativeQuery(String sql, Class type) {
        return CollectionsUtils.iteratorToList(s().query(type, sql, new HashMap<>()).iterator());
    }

    @Override
    public List getPropertyValues(Class<?> entityClass, String property) {
        throw new UnsupportedOperationException("No yet supported");
    }

    @Override
    public List getPropertyValues(Class entityClass, String property, QueryParameters params) {
        throw new UnsupportedOperationException("No yet supported");
    }

    @Override
    public int batchUpdate(Class type, String field, Object value, QueryParameters params) {
        throw new UnsupportedOperationException("No yet supported");
    }

    @Override
    public int batchUpdate(Class type, Map<String, Object> fieldvalues, QueryParameters params) {
        throw new UnsupportedOperationException("No yet supported");
    }

    @Override
    public <T> T reload(T entity) {
        return (T) s().load(entity.getClass(), s().resolveGraphIdFor(entity));

    }

    @Override
    public <T> T executeProjection(Class<T> resultClass, String projectionQueryText, QueryParameters parameters) {
        return queryObject(resultClass, projectionQueryText, parameters);
    }


    @Override
    public void executeWithinTransaction(Callback callback) {
        s().beginTransaction();
        try {
            callback.doSomething();
            s().getTransaction().commit();
        } catch (Exception e) {
            s().getTransaction().rollback();
        } finally {
            s().getTransaction().close();
        }

    }

    @Override
    protected List<CrudServiceListener> getListeners() {
        return new ArrayList<>(Containers.get().findObjects(CrudServiceListener.class));
    }

    private Filters buildFilters(QueryParameters params) {
        Filters filters = new Filters();

        params.forEach((k, v) -> {
            if (v instanceof QueryCondition) {
                Filter filter = new Filter(k, Neo4jQueryCondition.getOperator((QueryCondition) v),
                        ((QueryCondition) v).getValue());
                filter.setBooleanOperator(BooleanOperator.AND);
                filters.add(filter);
            } else {
                Filter filter = new Filter(k,ComparisonOperator.EQUALS, v);
                filter.setBooleanOperator(BooleanOperator.AND);
                filters.add(filter);
            }
        });

        return filters;
    }

    private Pagination buildPagination(QueryParameters params) {
        DataPaginator paginator = params.getPaginator();
        Pagination pag = new Pagination(paginator.getPage(), paginator.getPageSize());
        pag.setOffset(paginator.getFirstResult());
        return pag;
    }

    private int getDepth(QueryParameters params) {
        return params.getDepth();

    }

    @Override
    public Object getDelgate() {
        return s();
    }

}
