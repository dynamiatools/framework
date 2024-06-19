package tools.dynamia.domain;

import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.Callback;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.commons.reflect.ReflectionException;
import tools.dynamia.domain.query.QueryCondition;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.ValidatorService;
import tools.dynamia.domain.services.impl.AbstractCrudService;
import tools.dynamia.domain.services.impl.DefaultValidatorService;
import tools.dynamia.domain.util.CrudServiceListener;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.domain.util.QueryBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Very simple in memory implementation of {@link tools.dynamia.domain.services.CrudService} use only for unit testing.
 * Never use this class in production or something bad could happen.
 */
public class InMemoryCrudService extends AbstractCrudService {

    private static LoggingService LOGGER = new SLF4JLoggingService(InMemoryCrudService.class);
    private List<CrudServiceListener> listeners;
    private Map<Class<?>, List<Object>> database = new ConcurrentHashMap<>();
    private ValidatorService validator;

    public InMemoryCrudService() {
    }

    public InMemoryCrudService(List<CrudServiceListener> listeners) {
        this.listeners = listeners;
    }

    public InMemoryCrudService(List<CrudServiceListener> listeners, ValidatorService validator) {
        this.listeners = listeners;
        this.validator = validator;
    }

    public List<Object> getEntities(Class<?> entityClass) {
        return database.computeIfAbsent(entityClass, k -> new ArrayList<>());
    }

    protected List<?> filter(QueryParameters parameters, List<?> objects, Class<?> type) {
        LOGGER.info("Filtering entities of type " + type + " using parameters " + parameters);
        if (parameters == null) {
            parameters = new QueryParameters();
        }
        parameters.setType(type);
        fireListeners(parameters, EventType.BEFORE_QUERY);
        if (parameters.isEmpty()) {
            return objects;
        } else {
            List<?> filtered = objects;
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                String property = entry.getKey();
                Object value = entry.getValue();

                filtered = filterByProperty(filtered, property, value);

            }
            objects = filtered;
        }
        fireListeners(parameters, EventType.AFTER_QUERY);
        return objects;
    }

    private List<?> filterByProperty(List<?> sample, String property, Object value) {
        List<Object> result = new ArrayList<>();
        for (Object bean : sample) {
            if (bean != null) {
                Object beanPropertyValue = null;

                try {
                    beanPropertyValue = BeanUtils.invokeGetMethod(bean, property);
                } catch (ReflectionException e) {
                    try {
                        beanPropertyValue = BeanUtils.invokeBooleanGetMethod(bean, property);
                    } catch (Exception ex) {
                        LOGGER.warn("Cannot filter: " + ex.getMessage());
                    }
                }

                if (beanPropertyValue != null) {
                    if (value instanceof QueryCondition<?> queryCondition) {
                        try {
                            if (queryCondition.match(beanPropertyValue)) {
                                result.add(bean);
                            }
                        } catch (UnsupportedOperationException e) {
                            LOGGER.warn("QueryCondition " + value + " not supported");
                        }
                    } else if (beanPropertyValue.equals(value)) {
                        result.add(bean);

                    }
                }
            }
        }

        return result;
    }


    @Override
    protected List<CrudServiceListener> getListeners() {
        return listeners;
    }

    public void setListeners(List<CrudServiceListener> listeners) {
        this.listeners = listeners;
    }

    public void addListener(CrudServiceListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }

        listeners.add(listener);
    }

    public void setValidator(ValidatorService validator) {
        this.validator = validator;
    }

    public ValidatorService getValidator() {
        return validator;
    }

    @Override
    public Serializable getId(Class entityClass, QueryParameters params) {
        Serializable id = null;
        Object result = findSingle(entityClass, params);
        id = findId(result);

        return id;
    }

    private static Serializable findId(Object result) {
        Serializable id;
        id = DomainUtils.findEntityId(result);
        if (id == null) {
            try {
                id = (Serializable) BeanUtils.getFieldValue("id", result);
            } catch (Exception e) {
                LOGGER.error("Error find by id ", e);
            }
        }
        return id;
    }

    @Override
    public <T> T create(T t) {
        if (t != null) {
            fireListeners(t, EventType.BEFORE_CREATE);
            validate(t);
            LOGGER.info("Creating entity: " + t);
            var entities = getEntities(t.getClass());
            entities.add(t);
            try {
                BeanUtils.setFieldValue("id", t, Long.valueOf(entities.size()));
            } catch (Exception e) {
                //ignore
            }
            fireListeners(t, EventType.AFTER_CREATE);
        }
        return t;
    }

    @Override
    public <T> T update(T t) {
        fireListeners(t, EventType.BEFORE_UPDATE);
        validate(t);
        LOGGER.info("Updating entity: " + t);
        fireListeners(t, EventType.AFTER_UPDATE);
        return t;
    }

    @Override
    public <T> void delete(T t) {
        if (t != null) {
            fireListeners(t, EventType.BEFORE_DELETE);
            LOGGER.info("Deleting entity: " + t);
            getEntities(t.getClass()).remove(t);
            fireListeners(t, EventType.AFTER_DELETE);
        }
    }

    @Override
    public void delete(Class type, Serializable id) {

        try {
            List entities = getEntities(type);
            Object entity = entities.stream().filter(o -> id.equals(findId(o))).findFirst().orElse(null);
            if (entity != null) {
                delete(entity);
            }
        } catch (Exception e) {
            LOGGER.error("Error deleting by class and id", e);
        }
    }

    @Override
    public void deleteAll(Class type) {
        LOGGER.info("Deleting all entities ");
        getEntities(type).clear();
    }

    @Override
    public void updateField(Object entity, String field, Object value) {
        try {
            LOGGER.info("Updating entity: " + entity + " field: " + field + " value: " + value);
            BeanUtils.setFieldValue(field, entity, value);
        } catch (Exception e) {
            LOGGER.error("Error updating field  [" + field + "] of entity " + entity + " with value [" + value + "]");
        }

    }

    @Override
    public <T> List<T> findAll(Class<T> type) {
        return (List<T>) filter(new QueryParameters(), getEntities(type), type);
    }

    @Override
    public <T> List<T> findAll(Class<T> type, String orderBy) {
        return findAll(type);
    }

    @Override
    public <T> List<T> find(Class<T> type, QueryParameters parameters) {
        return (List<T>) filter(parameters, getEntities(type), type);
    }

    @Override
    public <T> List<T> executeQuery(QueryBuilder queryBuilder, QueryParameters parameters) {
        return (List<T>) find(queryBuilder.getType(), parameters);
    }

    @Override
    public <T> List<T> executeQuery(QueryBuilder queryBuilder) {
        return executeQuery(queryBuilder, queryBuilder.getQueryParameters());
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
        var result = filter(QueryParameters.with(property, value), getEntities(type), type);
        return (T) result.stream().findFirst().orElse(null);
    }

    @Override
    public <T> T findSingle(Class<T> entityClass, QueryParameters params) {
        var result = filter(params, getEntities(entityClass), entityClass);
        return (T) result.stream().findFirst().orElse(null);
    }

    @Override
    public <T> List<T> findByFields(Class<T> type, String param, String... fields) {
        return (List<T>) getEntities(type);
    }

    @Override
    public <T> List<T> findByFields(Class<T> type, String param, QueryParameters defaultParams, String...
            fields) {
        return (List<T>) getEntities(type);
    }

    @Override
    public List getPropertyValues(Class<?> entityClass, String property) {
        try {
            return getEntities(entityClass).stream().map(o -> BeanUtils.getFieldValue(property, o))
                    .toList();
        } catch (Exception e) {
            LOGGER.error("Error getting property values for " + entityClass + " [" + property + "]", e);
            return List.of();
        }
    }

    @Override
    public List getPropertyValues(Class entityClass, String property, QueryParameters params) {
        try {
            return filter(params, getEntities(entityClass), entityClass)
                    .stream()
                    .map(o -> BeanUtils.getFieldValue(property, o))
                    .toList();
        } catch (Exception e) {
            LOGGER.error("Error getting property values for " + entityClass + " [" + property + "]", e);
            return List.of();
        }
    }

    @Override
    public int batchUpdate(Class type, String field, Object value, QueryParameters params) {
        var filtered = filter(params, getEntities(type), type);
        filtered.forEach(o -> updateField(o, field, value));
        return filtered.size();
    }

    @Override
    public int batchUpdate(Class type, Map<String, Object> fieldvalues, QueryParameters params) {
        var filtered = filter(params, getEntities(type), type);
        filtered.forEach(o -> fieldvalues.forEach((field, value) -> updateField(o, field, value)));
        return filtered.size();
    }

    @Override
    public <T> T reload(T entity) {
        return entity;
    }

    @Override
    public <T> T executeProjection(Class<T> resultClass, String projectionQueryText, QueryParameters parameters) {
        return null;
    }

    @Override
    public void executeWithinTransaction(Callback callback) {
        callback.doSomething();
    }

    @Override
    public void increaseCounter(Object entity, String counterName) {
        Object counterValue = BeanUtils.getFieldValue(counterName, entity);
        if (counterValue instanceof Long number) {
            number = number + 1;
            BeanUtils.setFieldValue(counterName, entity, number);
        } else if (counterValue instanceof Integer number) {
            number = number + 1;
            BeanUtils.setFieldValue(counterName, entity, number);
        }
    }

    @Override
    public void deacreaseCounter(Object entity, String counterName) {
        Object counterValue = BeanUtils.getFieldValue(counterName, entity);
        if (counterValue instanceof Long number) {
            number = number - 1;
            BeanUtils.setFieldValue(counterName, entity, number);
        } else if (counterValue instanceof Integer number) {
            number = number - 1;
            BeanUtils.setFieldValue(counterName, entity, number);
        }
    }


    @Override
    public <T> T getFieldValue(Object entity, String fieldName, Class<T> fieldClass) {
        return (T) BeanUtils.getFieldValue(fieldName, entity);
    }

    @Override
    public Object getDelgate() {
        return this;
    }

    protected void validate(Object obj) {

        if (validator == null) {
            try {
                validator = new DefaultValidatorService();
            } catch (Exception e) {
                LOGGER.warn("Cannot create default validator service: " + e.getMessage());
            }
        }

        if (validator != null) {
            validator.validate(obj);
        }
    }

}
