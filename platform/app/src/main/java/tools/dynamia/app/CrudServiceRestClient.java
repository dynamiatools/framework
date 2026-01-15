package tools.dynamia.app;

import tools.dynamia.commons.Callback;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.impl.AbstractCrudService;
import tools.dynamia.domain.util.CrudServiceListener;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.domain.util.QueryBuilder;
import tools.dynamia.web.util.HttpRestClient;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class CrudServiceRestClient extends AbstractCrudService {


    private HttpRestClient restClient;

    public CrudServiceRestClient(HttpRestClient restClient) {
        this.restClient = restClient;
    }

    private String uri(Class entityClass, String endpoint) {
        return "/" + entityClass.getName() + endpoint;
    }


    @Override
    protected List<CrudServiceListener> getListeners() {
        return List.of();
    }

    @Override
    public Serializable getId(Class entityClass, QueryParameters params) {
        return restClient.post(uri(entityClass, "/id"), params, Serializable.class);
    }


    @Override
    public <T> T create(T t) {
        return (T) restClient.post(uri(t.getClass(), "/id"), t, t.getClass());
    }

    @Override
    public <T> T update(T t) {
        return (T) restClient.put(uri(t.getClass(), "/id"), t, t.getClass());
    }

    @Override
    public <T> void delete(T t) {
        delete(t.getClass(), DomainUtils.findEntityId(t));
    }

    @Override
    public void delete(Class type, Serializable id) {
        restClient.delete(uri(type, "/" + id));
    }

    @Override
    public void deleteAll(Class type) {

    }

    @Override
    public void updateField(Object entity, String field, Object value) {

    }

    @Override
    public <T> List<T> findAll(Class<T> type) {
        return find(type, new QueryParameters());
    }

    @Override
    public <T> List<T> findAll(Class<T> type, String orderBy) {
        return find(type, new QueryParameters().orderBy(orderBy));
    }

    @Override
    public <T> List<T> find(Class<T> type, QueryParameters parameters) {
        return restClient.post(uri(type, "/find"), parameters, List.class);
    }

    @Override
    public <T> List<T> executeQuery(QueryBuilder queryBuilder, QueryParameters parameters) {
        return List.of();
    }

    @Override
    public <T> List<T> executeQuery(QueryBuilder queryBuilder) {
        return List.of();
    }

    @Override
    public <T> List<T> executeQuery(String queryText) {
        return List.of();
    }

    @Override
    public <T> List<T> executeQuery(String queryText, QueryParameters parameters) {
        return List.of();
    }

    @Override
    public int execute(String queryText, QueryParameters parameters) {
        return 0;
    }

    @Override
    public <T> T findSingle(Class<T> type, String property, Object value) {
        return null;
    }

    @Override
    public <T> T findSingle(Class<T> entityClass, QueryParameters params) {
        return null;
    }

    @Override
    public <T> List<T> findByFields(Class<T> type, String param, String... fields) {
        return List.of();
    }

    @Override
    public <T> List<T> findByFields(Class<T> type, String param, QueryParameters defaultParams, String... fields) {
        return List.of();
    }

    @Override
    public List getPropertyValues(Class<?> entityClass, String property) {
        return List.of();
    }

    @Override
    public List getPropertyValues(Class entityClass, String property, QueryParameters params) {
        return List.of();
    }

    @Override
    public int batchUpdate(Class type, String field, Object value, QueryParameters params) {
        return 0;
    }

    @Override
    public int batchUpdate(Class type, Map<String, Object> fieldvalues, QueryParameters params) {
        return 0;
    }

    @Override
    public <T> T reload(T entity) {
        return null;
    }

    @Override
    public <T> T executeProjection(Class<T> resultClass, String projectionQueryText, QueryParameters parameters) {
        return null;
    }

    @Override
    public void executeWithinTransaction(Callback callback) {

    }

    @Override
    public Object getDelgate() {
        return null;
    }
}
