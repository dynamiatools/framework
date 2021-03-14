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
package tools.dynamia.domain.services.impl;

import tools.dynamia.commons.BeanSorter;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.AbstractEntity;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.query.DataPaginator;
import tools.dynamia.domain.query.QueryExample;
import tools.dynamia.domain.query.QueryMetadata;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.CrudServiceListener;

import java.io.Serializable;
import java.util.List;

/**
 * CrudService implementation.
 *
 * @author Ing. Mario Alejandro Serrano Leones
 * @since 1.0
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class AbstractCrudService implements CrudService {

    /**
     * The logger.
     */
    private LoggingService logger = new SLF4JLoggingService(CrudService.class);

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.domain.services.CrudService#save(java.lang.Object,
     * java.io.Serializable)
     */
    @Override
    public <T> T save(T t, Serializable id) {
        if (id == null) {
            return create(t);
        } else {
            return update(t);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.domain.services.CrudService#save(java.lang.Object)
     */
    @Override
    public <T> T save(T t) {
        Serializable id = null;
        if (t instanceof AbstractEntity) {
            id = ((AbstractEntity) t).getId();
        }
        return save(t, id);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.domain.services.CrudService#find(java.lang.Class,
     * java.io.Serializable)*/
    @Override
    public <T> T find(Class<T> type, Serializable id) {
        return findSingle(type, "id", id);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.domain.services.CrudService#find(com.dynamia.tools.
     * domain.query.QueryMetadata)
     */
    @Override
    public List find(QueryMetadata queryInfo) {
        throw new UnsupportedOperationException("Not supported");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.domain.services.CrudService#increaseCounter(java.lang.
     * Object, java.lang.String)
     */
    @Override
    public void increaseCounter(Object entity, String counterName) {
        throw new UnsupportedOperationException("Method not supported in this CrudService");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.domain.services.CrudService#deacreaseCounter(java.lang.
     * Object, java.lang.String)
     */
    @Override
    public void deacreaseCounter(Object entity, String counterName) {
        throw new UnsupportedOperationException("Method not supported in this CrudService");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.domain.services.CrudService#findByExample(java.lang.
     * Object, com.dynamia.tools.domain.query.DataPaginator,
     * com.dynamia.tools.commons.BeanSorter)
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
     * com.dynamia.tools.domain.services.CrudService#findByExample(java.lang.
     * Object, com.dynamia.tools.domain.query.QueryParameters)
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
     * com.dynamia.tools.domain.services.CrudService#findByExample(java.lang.
     * Object)
     */
    @Override
    public <T> List<T> findByExample(T example) {
        return findByExample(example, null, null);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.domain.services.CrudService#findWithNamedQuery(java.
     * lang.String)
     */
    @Override
    public List findWithNamedQuery(String queryName) {
        return findWithNamedQuery(queryName, 0);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.domain.services.CrudService#findWithNamedQuery(java.
     * lang.String, int)
     */
    @Override
    public List findWithNamedQuery(String queryName, int resultLimit) {
        return findWithNamedQuery(queryName, null, resultLimit);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.domain.services.CrudService#findWithNamedQuery(java.
     * lang.String, com.dynamia.tools.domain.query.QueryParameters)
     */
    @Override
    public List findWithNamedQuery(String namedQueryName, QueryParameters parameters) {
        return findWithNamedQuery(namedQueryName, parameters, 0);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.domain.services.CrudService#findWithNamedQuery(java.
     * lang.String, com.dynamia.tools.domain.query.QueryParameters, int)
     */
    @Override
    public List findWithNamedQuery(String namedQueryName, QueryParameters parameters, int resultLimit) {
        throw new UnsupportedOperationException("Method not supported in this CrudService");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.domain.services.CrudService#findByNativeQuery(java.lang
     * .String, java.lang.Class)
     */
    @Override
    public List findByNativeQuery(String sql, Class type) {
        throw new UnsupportedOperationException("Method not supported in this CrudService");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.domain.services.CrudService#findSingleWithNameQuery(
     * java.lang.String, com.dynamia.tools.domain.query.QueryParameters)
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
     * @see com.dynamia.tools.domain.services.CrudService#find(java.lang.Class,
     * java.lang.String, java.lang.Object)
     */
    @Override
    public <T> List<T> find(Class<T> type, String property, Object value) {
        return find(type, QueryParameters.with(property, value));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.domain.services.CrudService#findIfNull(java.lang.Class,
     * java.lang.String)
     */
    @Override
    public <T> List<T> findIfNull(Class<T> type, String property) {
        throw new UnsupportedOperationException("Method not supported in this CrudService");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.domain.services.CrudService#count(java.lang.Class)
     */
    @Override
    public long count(Class type) {
        throw new UnsupportedOperationException("Method not supported in this CrudService");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.domain.services.CrudService#count(java.lang.Class,
     * com.dynamia.tools.domain.query.QueryParameters)
     */
    @Override
    public long count(Class type, QueryParameters parameters) {
        throw new UnsupportedOperationException("Method not supported in this CrudService");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.domain.services.CrudService#getReference(java.lang.
     * Class, java.io.Serializable)
     */
    @Override
    public <T> T getReference(Class<T> type, Serializable id) {
        throw new UnsupportedOperationException("Method not supported in this CrudService");
    }

    /**
     * Fire listeners.
     *
     * @param value the value
     * @param type  the type
     */
    protected void fireListeners(Object value, EventType type) {
        List<CrudServiceListener> listeners = getListeners();

        if (listeners != null && !listeners.isEmpty()) {

            for (CrudServiceListener listener : listeners) {
                try {
                    switch (type) {
                        case BEFORE_CREATE:
                            listener.beforeCreate(value);
                            break;
                        case AFTER_CREATE:
                            listener.afterCreate(value);
                            break;
                        case BEFORE_UPDATE:
                            listener.beforeUpdate(value);
                            break;
                        case AFTER_UPDATE:
                            listener.afterUpdate(value);
                            break;
                        case BEFORE_DELETE:
                            listener.beforeDelete(value);
                            break;
                        case AFTER_DELETE:
                            listener.afterDelete(value);
                            break;
                        case BEFORE_QUERY:
                            if (value != null && value instanceof QueryParameters) {
                                listener.beforeQuery((QueryParameters) value);
                            }
                            break;
                        case AFTER_QUERY:
                            if (value instanceof List) {
                                listener.afterQuery((List) value);
                            }
                            break;
                    }
                } catch (ClassCastException e) {
                    // Ignore, this error is because generics
                } catch (ValidationError v) {
                    throw v;
                } catch (Exception e) {
                    logger.error("Exception Firing CrudServiceListener: " + listener.getClass(), e);

                }
            }

        }
    }

    /**
     * Gets the listeners.
     *
     * @return the listeners
     */
    protected abstract List<CrudServiceListener> getListeners();

    /**
     * The Enum EventType.
     */
    protected enum EventType {

        /**
         * The before create.
         */
        BEFORE_CREATE,
        /**
         * The after create.
         */
        AFTER_CREATE,
        /**
         * The before update.
         */
        BEFORE_UPDATE,
        /**
         * The after update.
         */
        AFTER_UPDATE,
        /**
         * The before delete.
         */
        BEFORE_DELETE,
        /**
         * The after delete.
         */
        AFTER_DELETE,
        /**
         * The before query.
         */
        BEFORE_QUERY,
        /**
         * The after query.
         */
        AFTER_QUERY
    }

    @Override
    public <T> T getFieldValue(Object entity, String fieldName, Class<T> fieldClass) {
        throw new UnsupportedOperationException("Method not supported in this CrudService");
    }
}
