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

package tools.dynamia.domain.services.impl;

import tools.dynamia.commons.Callback;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.util.CrudServiceListener;
import tools.dynamia.domain.util.QueryBuilder;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NoOpCrudService extends AbstractCrudService {

    @Override
    public Serializable getId(Class entityClass, QueryParameters params) {
        System.out.println("NoOpCrudService.getId()");
        return null;
    }

    @Override
    public <T> T create(T t) {
        System.out.println("NoOpCrudService.create()");
        return null;
    }

    @Override
    public <T> T update(T t) {
        System.out.println("NoOpCrudService.update()");
        return null;
    }

    @Override
    public <T> void delete(T t) {
        System.out.println("NoOpCrudService.delete()");
    }

    @Override
    public void delete(Class type, Serializable id) {
        System.out.println("NoOpCrudService.delete()");
    }

    @Override
    public void deleteAll(Class type) {
        System.out.println("NoOpCrudService.deleteAll()");
    }

    @Override
    public void updateField(Object entity, String field, Object value) {
        System.out.println("NoOpCrudService.updateField()");
    }

    @Override
    public <T> List<T> findAll(Class<T> type) {
        System.out.println("NoOpCrudService.findAll()");
        return Collections.emptyList();
    }

    @Override
    public <T> List<T> findAll(Class<T> type, String orderBy) {
        System.out.println("NoOpCrudService.findAll()");
        return Collections.emptyList();
    }

    @Override
    public <T> List<T> find(Class<T> type, QueryParameters parameters) {
        System.out.println("NoOpCrudService.find()");
        return Collections.emptyList();
    }

    @Override
    public <T> List<T> executeQuery(QueryBuilder queryBuilder, QueryParameters parameters) {
        System.out.println("NoOpCrudService.executeQuery()");
        return Collections.emptyList();
    }

    @Override
    public <T> List<T> executeQuery(QueryBuilder queryBuilder) {
        System.out.println("NoOpCrudService.executeQuery()");
        return Collections.emptyList();
    }

    @Override
    public <T> List<T> executeQuery(String queryText) {
        System.out.println("NoOpCrudService.executeQuery()");
        return Collections.emptyList();
    }

    @Override
    public <T> List<T> executeQuery(String queryText, QueryParameters parameters) {
        System.out.println("NoOpCrudService.executeQuery()");
        return Collections.emptyList();
    }

    @Override
    public int execute(String queryText, QueryParameters parameters) {
        System.out.println("NoOpCrudService.execute()");
        return 0;
    }

    @Override
    public <T> T findSingle(Class<T> type, String property, Object value) {
        System.out.println("NoOpCrudService.findSingle()");
        return null;
    }

    @Override
    public <T> T findSingle(Class<T> entityClass, QueryParameters params) {
        System.out.println("NoOpCrudService.findSingle()");
        return null;
    }

    @Override
    public <T> List<T> findByFields(Class<T> type, String param, String... fields) {
        System.out.println("NoOpCrudService.findByFields()");
        return Collections.emptyList();
    }

    @Override
    public <T> List<T> findByFields(Class<T> type, String param, QueryParameters defaultParams, String... fields) {
        System.out.println("NoOpCrudService.findByFields()");
        return Collections.emptyList();
    }

    @Override
    public List getPropertyValues(Class<?> entityClass, String property) {
        System.out.println("NoOpCrudService.getPropertyValues()");
        return Collections.emptyList();
    }

    @Override
    public List getPropertyValues(Class entityClass, String property, QueryParameters params) {
        return getPropertyValues(entityClass, property);
    }

    @Override
    public int batchUpdate(Class type, String field, Object value, QueryParameters params) {
        System.out.println("NoOpCrudService.batchUpdate()");
        return 0;
    }

    @Override
    public int batchUpdate(Class type, Map<String, Object> fieldvalues, QueryParameters params) {
        System.out.println("NoOpCrudService.batchUpdate()");
        return 0;
    }

    @Override
    public <T> T reload(T entity) {
        System.out.println("NoOpCrudService.reload()");
        return null;
    }

    @Override
    public <T> T executeProjection(Class<T> resultClass, String projectionQueryText, QueryParameters parameters) {
        System.out.println("NoOpCrudService.executeProjection()");
        return null;
    }


    @Override
    public void executeWithinTransaction(Callback callback) {
        System.out.println("NoOpCrudService.executeWithinTransaction()");
    }

    @Override
    protected List<CrudServiceListener> getListeners() {
        System.out.println("NoOpCrudService.getListeners()");
        return Collections.emptyList();
    }

    @Override
    public Object getDelgate() {
        return null;
    }

    @Override
    public <T> T getFieldValue(Object entity, String fieldName, Class<T> fieldClass) {
        System.out.println("NoOpCrudService.getFieldValue()");
        return null;
    }
}
