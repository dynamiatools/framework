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

package tools.dynamia.domain;

import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.DomainUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Helper class to work with entities.
 */
public class EntityHandler<T> {

    private final Class<T> type;
    private final CrudService crudService;


    public EntityHandler(Class clazz) {
        this(clazz, DomainUtils.lookupCrudService());
    }

    public EntityHandler(Class clazz, CrudService crudService) {
        //noinspection unchecked
        this.type = clazz;
        this.crudService = crudService;

        if (!DomainUtils.isEntity(clazz)) {
            throw new IllegalArgumentException("Class " + clazz + " is not an Entity");
        }
    }

    /**
     * Find a single result
     *
     */
    public T findSingle(QueryParameters params) {
        return crudService.findSingle(type, params);
    }


    /**
     * Execute query
     *
     */
    public List<T> find(QueryParameters params) {
        return crudService.find(type, params);
    }

    /**
     * Find all
     * <p>
     * s
     *
     */
    public List<T> findAll() {
        return crudService.findAll(type);
    }

    /**
     * Count all
     *
     */
    public long count() {
        return crudService.count(type);
    }

    /**
     * Count all with parameters
     *
     */
    public long count(QueryParameters params) {
        return crudService.count(type, params);
    }

    /**
     * Find all ordering by
     *
     */
    public List<T> findAll(String orderBy) {
        return crudService.findAll(type, orderBy);
    }

    /**
     * Find by ID
     *
     */
    public T findById(Serializable id) {
        return crudService.find(type, id);
    }

    /**
     * Delete All
     */
    public void deleteAll() {
        crudService.deleteAll(type);
    }

    /**
     * Batch update
     *
     * @return affected result
     */
    public int batchUpdate(String field, Object value, QueryParameters params) {
        return crudService.batchUpdate(type, field, value, params);
    }

    /**
     * Batch Update
     *
     * @return affected result
     */
    public int batchUpdate(Map<String, Object> fieldvalues, QueryParameters params) {
        return crudService.batchUpdate(type, fieldvalues, params);
    }


    /**
     * Find first
     *
     */
    public T findFirst() {
        return crudService.findFirst(type);
    }

    /**
     * Create or update the Entity
     *
     */
    public T save(T t) {
        return crudService.save(t);
    }

    /**
     * Create a the entity
     *
     */
    public T create(T t) {
        return crudService.create(t);
    }

    /**
     * Update the entity
     *
     */
    public T update(T t) {
        return crudService.update(t);
    }

    /**
     * Delete the entity
     *
     */
    public void delete(T t) {
        crudService.delete(t);
    }

    /**
     * Create a new {@link EntityHandler} for clazz using default CrudService
     *
     * @return EntityHandler
     */
    public static <T> EntityHandler<T> handle(Class<T> clazz) {
        return new EntityHandler<>(clazz);
    }

    /**
     * Create a new {@link EntityHandler} for clazz
     *
     */
    public static <T> EntityHandler<T> handle(Class<T> clazz, CrudService crudService) {
        return new EntityHandler<>(clazz, crudService);
    }

    @Override
    public String toString() {
        return EntityHandler.class.getSimpleName() + " for entity " + type + " using crudService " + crudService;
    }
}
