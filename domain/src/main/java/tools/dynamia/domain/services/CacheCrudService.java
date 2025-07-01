package tools.dynamia.domain.services;

import tools.dynamia.commons.BeanSorter;
import tools.dynamia.commons.Callback;
import tools.dynamia.domain.AbstractEntity;
import tools.dynamia.domain.AutoEvictEntityCacheCrudListener;
import tools.dynamia.domain.query.DataPaginator;
import tools.dynamia.domain.query.QueryMetadata;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.impl.AbstractCrudService;
import tools.dynamia.domain.util.CrudServiceListener;
import tools.dynamia.domain.util.QueryBuilder;
import tools.dynamia.integration.Containers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static tools.dynamia.integration.CacheManagerUtils.getValue;

/**
 * EXPERIMENTAL: CacheCrudService is a CrudService implementation that caches the results of some CRUD operations.
 * It uses the CacheManager to store and retrieve the results.
 */
public class CacheCrudService extends AbstractCrudService {


    /**
     * Create a new CacheCrudService instance with the given delegate CrudService.
     *
     * @param delegate the delegate CrudService
     * @return a new CacheCrudService instance
     */
    public static CrudService of(CrudService delegate) {
        return new CacheCrudService(delegate);
    }

    private String cacheName = "crud";
    private CrudService delegate;

    public CacheCrudService(CrudService delegate) {
        this.delegate = delegate;
    }

    public CacheCrudService(String cacheName, CrudService delegate) {
        this.cacheName = cacheName;
        this.delegate = delegate;
    }


    /**
     * Build a key for the given prefix and arguments.
     *
     * @param prefix
     * @param args
     * @return
     */
    protected String buildKey(String prefix, Object... args) {
        if (args == null || args.length == 0) {
            return null;
        }
        StringBuilder keyBuilder = new StringBuilder();
        for (Object arg : args) {
            if (arg != null) {
                keyBuilder.append(toString(arg)).append("-");
            }
        }

        return prefix + "-" + keyBuilder;
    }

    /**
     * Convert the given argument to a string representation.
     *
     * @param arg
     * @return
     */
    protected String toString(Object arg) {
        switch (arg) {
            case null -> {
                return "#";
            }
            case String string -> {
                return string.trim().replace(" ", "_");
            }
            case Class<?> clazz -> {
                return clazz.getSimpleName();
            }
            case AbstractEntity<?> entity -> {
                return entity.getClass().getSimpleName() + "-" + entity.getId();
            }
            case Map<?, ?> map -> {
                StringBuilder mapKey = new StringBuilder();
                map.forEach((k, v) -> mapKey.append(k).append(":").append(toString(v)));
                return mapKey.toString();
            }
            case Collection<?> collection -> {
                StringBuilder colkey = new StringBuilder();
                collection.forEach(v -> colkey.append(toString(v)).append(":"));
                return colkey.toString();
            }
            default -> {
            }
        }


        return arg.toString();
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public Serializable getId(Class entityClass, QueryParameters params) {
        String key = buildKey("GetId", entityClass, params);
        return getValue(cacheName, key, () -> delegate.getId(entityClass, params));

    }

    @Override
    public <T> T save(T t, Serializable id) {
        return delegate.save(t, id);
    }

    @Override
    public <T> T save(T t) {
        return delegate.save(t);
    }

    @Override
    public <T> T create(T t) {
        return delegate.create(t);
    }

    @Override
    public <T> T find(Class<T> type, Serializable id) {
        String key = buildKey("Find", type, id);
        return getValue(cacheName, key, () -> delegate.find(type, id));
    }

    @Override
    public <T> T load(Class<T> type, Serializable id) {
        String key = buildKey("Load", type, id);
        return getValue(cacheName, key, () -> delegate.load(type, id));
    }

    @Override
    public <T> T findByRemoteId(Class<T> type, Serializable remoteId) {
        String key = buildKey("FindByRemoteId", type, remoteId);
        return getValue(cacheName, key, () -> delegate.findByRemoteId(type, remoteId));
    }

    @Override
    public <T> T update(T t) {
        return delegate.update(t);
    }

    @Override
    public <T> void delete(T t) {
        delegate.delete(t);
    }

    @Override
    public void delete(Class type, Serializable id) {
        delegate.delete(type, id);
    }

    @Override
    public void deleteAll(Class type) {
        delegate.deleteAll(type);
    }

    @Override
    public void updateField(Object entity, String field, Object value) {
        delegate.updateField(entity, field, value);
    }

    @Override
    public void increaseCounter(Object entity, String counterName) {
        delegate.increaseCounter(entity, counterName);
    }

    @Override
    public void deacreaseCounter(Object entity, String counterName) {
        delegate.deacreaseCounter(entity, counterName);
    }

    @Override
    public <T> List<T> findAll(Class<T> type) {
        String key = buildKey("FindAll", type);
        return getValue(cacheName, key, () -> delegate.findAll(type));
    }

    @Override
    public <T> List<T> findAll(Class<T> type, String orderBy) {
        String key = buildKey("FindAll", type, orderBy);
        return getValue(cacheName, key, () -> delegate.findAll(type, orderBy));
    }

    @Override
    public <T> List<T> find(Class<T> type, QueryParameters parameters) {
        String key = buildKey("Find", type, parameters);
        return getValue(cacheName, key, () -> delegate.find(type, parameters));
    }

    @Override
    public <T> List<T> executeQuery(QueryBuilder queryBuilder, QueryParameters parameters) {
        return delegate.executeQuery(queryBuilder, parameters);
    }

    @Override
    public <T> List<T> executeQuery(QueryBuilder queryBuilder) {
        return delegate.executeQuery(queryBuilder);
    }

    @Override
    public <T> List<T> executeQuery(String queryText) {
        return delegate.executeQuery(queryText);
    }

    @Override
    public <T> List<T> executeQuery(String queryText, QueryParameters parameters) {
        return delegate.executeQuery(queryText, parameters);
    }

    @Override
    public int execute(String queryText, QueryParameters parameters) {
        return delegate.execute(queryText, parameters);
    }

    @Override
    public int execute(QueryBuilder query) {
        return delegate.execute(query);
    }

    @Override
    public <T> List<T> findByExample(T example) {
        return delegate.findByExample(example);
    }

    @Override
    public <T> List<T> findByExample(T example, DataPaginator paginator, BeanSorter sorter) {
        return delegate.findByExample(example, paginator, sorter);
    }

    @Override
    public <T> List<T> findByExample(T example, QueryParameters params) {
        return delegate.findByExample(example, params);
    }

    @Override
    public <T> List<T> find(Class<T> type, String property, Object value) {
        return delegate.find(type, property, value);
    }

    @Override
    public List findWithNamedQuery(String queryName) {
        return delegate.findWithNamedQuery(queryName);
    }

    @Override
    public List findWithNamedQuery(String queryName, int resultLimit) {
        return delegate.findWithNamedQuery(queryName, resultLimit);
    }

    @Override
    public List findWithNamedQuery(String namedQueryName, QueryParameters parameters) {
        return delegate.findWithNamedQuery(namedQueryName, parameters);
    }

    @Override
    public List findWithNamedQuery(String namedQueryName, QueryParameters parameters, int resultLimit) {
        return delegate.findWithNamedQuery(namedQueryName, parameters, resultLimit);
    }

    @Override
    public Object findSingleWithNameQuery(String namedQueryName, QueryParameters parameters) {
        return delegate.findSingleWithNameQuery(namedQueryName, parameters);
    }

    @Override
    public List findByNativeQuery(String sql, Class type) {
        return delegate.findByNativeQuery(sql, type);
    }

    @Override
    public long count(Class type, QueryParameters parameters) {
        return delegate.count(type, parameters);
    }

    @Override
    public <T> T getReference(Class<T> type, Serializable id) {
        return delegate.getReference(type, id);
    }

    @Override
    protected List<CrudServiceListener> getListeners() {
        return new ArrayList<>(Containers.get().findObjects(CrudServiceListener.class));
    }

    @Override
    public <T> List<T> findIfNull(Class<T> type, String property) {
        return delegate.findIfNull(type, property);
    }

    @Override
    public long count(Class type) {
        return delegate.count(type);
    }

    @Override
    public <T> T findSingle(Class<T> type, String property, Object value) {
        return delegate.findSingle(type, property, value);
    }

    @Override
    public <T> T findSingle(Class<T> entityClass, QueryParameters params) {
        return delegate.findSingle(entityClass, params);
    }

    @Override
    public <T> List<T> findByFields(Class<T> type, String param, String... fields) {
        return delegate.findByFields(type, param, fields);
    }

    @Override
    public <T> List<T> findByFields(Class<T> type, String param, QueryParameters defaultParams, String... fields) {
        return delegate.findByFields(type, param, defaultParams, fields);
    }

    @Override
    public List getPropertyValues(Class<?> entityClass, String property) {
        return delegate.getPropertyValues(entityClass, property);
    }

    @Override
    public List getPropertyValues(Class entityClass, String property, QueryParameters params) {
        return delegate.getPropertyValues(entityClass, property, params);
    }

    @Override
    public List find(QueryMetadata queryMetadata) {
        return delegate.find(queryMetadata);
    }

    @Override
    public int batchUpdate(Class type, String field, Object value, QueryParameters params) {
        return delegate.batchUpdate(type, field, value, params);
    }

    @Override
    public int batchUpdate(Class type, Map<String, Object> fieldvalues, QueryParameters params) {
        return delegate.batchUpdate(type, fieldvalues, params);
    }

    @Override
    public <T> T reload(T entity) {
        return delegate.reload(entity);
    }

    @Override
    public <T> T executeProjection(Class<T> resultClass, String projectionQueryText, QueryParameters parameters) {
        return delegate.executeProjection(resultClass, projectionQueryText, parameters);
    }

    @Override
    public void executeWithinTransaction(Callback callback) {
        delegate.executeWithinTransaction(callback);
    }

    @Override
    public void saveWithinTransaction(Object entity) {
        delegate.saveWithinTransaction(entity);
    }

    @Override
    public <T> T findFirst(Class<T> type) {
        return delegate.findFirst(type);
    }

    @Override
    public Object getDelgate() {
        return delegate.getDelgate();
    }

    @Override
    public <T> T getFieldValue(Object entity, String fieldName, Class<T> fieldClass) {
        return delegate.getFieldValue(entity, fieldName, fieldClass);
    }
}
