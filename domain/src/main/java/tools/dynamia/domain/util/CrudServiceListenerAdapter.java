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
package tools.dynamia.domain.util;

import tools.dynamia.domain.query.QueryParameters;

import java.util.List;


/**
 * The Class CrudServiceListenerAdapter.
 *
 * @author Mario A. Serrano Leones
 * @param <T> the generic type
 */
public class CrudServiceListenerAdapter<T> implements CrudServiceListener<T> {

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.util.CrudServiceListener#beforeCreate(java.lang.Object)
     */
    @Override
    public void beforeCreate(T entity) {
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.util.CrudServiceListener#afterCreate(java.lang.Object)
     */
    @Override
    public void afterCreate(T entity) {
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.util.CrudServiceListener#beforeUpdate(java.lang.Object)
     */
    @Override
    public void beforeUpdate(T entity) {
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.util.CrudServiceListener#afterUpdate(java.lang.Object)
     */
    @Override
    public void afterUpdate(T entity) {
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.util.CrudServiceListener#beforeDelete(java.lang.Object)
     */
    @Override
    public void beforeDelete(T entity) {
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.util.CrudServiceListener#afterDelete(java.lang.Object)
     */
    @Override
    public void afterDelete(T entity) {
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.util.CrudServiceListener#beforeQuery(com.dynamia.tools.domain.query.QueryParameters)
     */
    @Override
    public void beforeQuery(QueryParameters params) {
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.util.CrudServiceListener#afterQuery(java.util.List)
     */
    @Override
    public void afterQuery(List<T> result) {
    }
}
