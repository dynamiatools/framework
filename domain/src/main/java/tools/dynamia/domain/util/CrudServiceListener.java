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
package tools.dynamia.domain.util;

import tools.dynamia.domain.query.QueryParameters;

import java.util.List;


/**
 * The listener interface for receiving crudService events. The class that is
 * interested in processing a crudService event implements this interface, and
 * the object created with that class is registered with a component using the
 * component's <code>addCrudServiceListener<code> method. When
 * the crudService event occurs, that object's appropriate
 * method is invoked.
 *
 * @author Mario A. Serrano Leones
 * @param <T> the generic type
 */
public interface CrudServiceListener<T> {

    /**
     * Before create.
     *
     * @param entity the entity
     */
    void beforeCreate(T entity);

    /**
     * After create.
     *
     * @param entity the entity
     */
    void afterCreate(T entity);

    /**
     * Before update.
     *
     * @param entity the entity
     */
    void beforeUpdate(T entity);

    /**
     * After update.
     *
     * @param entity the entity
     */
    void afterUpdate(T entity);

    /**
     * Before delete.
     *
     * @param entity the entity
     */
    void beforeDelete(T entity);

    /**
     * After delete.
     *
     * @param entity the entity
     */
    void afterDelete(T entity);

    /**
     * Before query.
     *
     * @param params the params
     */
    void beforeQuery(QueryParameters params);

    /**
     * After query.
     *
     * @param result the result
     */
    void afterQuery(List<T> result);
}
