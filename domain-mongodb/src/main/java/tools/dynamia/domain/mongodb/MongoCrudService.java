package tools.dynamia.domain.mongodb;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import tools.dynamia.commons.Callback;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.impl.AbstractCrudService;
import tools.dynamia.domain.util.CrudServiceListener;
import tools.dynamia.domain.util.QueryBuilder;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.sterotypes.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("mongoCrudService")
public class MongoCrudService extends AbstractCrudService {

    private final MongoTemplate mongoTemplate;

    public MongoCrudService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    private String collectionName(Object obj) {
        return collectionName(obj.getClass());
    }

    private String collectionName(Class clazz) {
        return clazz.getSimpleName().toLowerCase();
    }

    @Override
    public Serializable getId(Class entityClass, QueryParameters params) {
        return null;
    }

    @Override
    public <T> T find(Class<T> type, Serializable id) {
        return mongoTemplate.findById(id, type, collectionName(type));
    }

    @Override
    public <T> T create(T t) {
        return mongoTemplate.insert(t, collectionName(t));
    }


    @Override
    public <T> T update(T t) {
        return mongoTemplate.save(t, collectionName(t));
    }

    @Override
    public <T> void delete(T t) {
        mongoTemplate.remove(t, collectionName(t));
    }

    @Override
    public void delete(Class type, Serializable id) {
        var obj = find(type, id);
        if (obj != null) {
            delete(obj);
        }
    }

    @Override
    public void deleteAll(Class type) {
        mongoTemplate.remove(type);
    }

    @Override
    public void updateField(Object entity, String field, Object value) {

    }

    @Override
    public <T> List<T> findAll(Class<T> type) {
        return mongoTemplate.findAll(type, collectionName(type));
    }

    @Override
    public <T> List<T> findAll(Class<T> type, String orderBy) {
        var query = new Query();
        query.with(Sort.by(orderBy));
        return mongoTemplate.find(query, type);
    }

    @Override
    public <T> List<T> find(Class<T> type, QueryParameters parameters) {
        return null;
    }

    @Override
    public <T> List<T> executeQuery(QueryBuilder queryBuilder, QueryParameters parameters) {
        return null;
    }

    @Override
    public <T> List<T> executeQuery(QueryBuilder queryBuilder) {
        return null;
    }

    @Override
    public <T> List<T> executeQuery(String queryText) {
        return null;
    }

    @Override
    public <T> List<T> executeQuery(String queryText, QueryParameters parameters) {
        return null;
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
        return null;
    }

    @Override
    public <T> List<T> findByFields(Class<T> type, String param, QueryParameters defaultParams, String... fields) {
        return null;
    }

    @Override
    public List getPropertyValues(Class<?> entityClass, String property) {
        return null;
    }

    @Override
    public List getPropertyValues(Class entityClass, String property, QueryParameters params) {
        return null;
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
        return mongoTemplate;
    }

    @Override
    protected List<CrudServiceListener> getListeners() {
        return new ArrayList<>(Containers.get().findObjects(CrudServiceListener.class));
    }
}
