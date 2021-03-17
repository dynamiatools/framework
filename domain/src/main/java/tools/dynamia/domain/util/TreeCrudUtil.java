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

import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;

import java.util.List;


/**
 * The Class TreeCrudUtil.
 *
 * @author Mario A. Serrano Leones
 * @param <T> the generic type
 */
public class TreeCrudUtil<T> {

    /**
     * The crud service.
     */
    private final CrudService crudService;

    /**
     * The entity class.
     */
    private final Class<T> entityClass;

    /**
     * The parent name.
     */
    private final String parentName;

    /**
     * Instantiates a new tree crud util.
     *
     * @param crudService the crud service
     * @param entityClass the entity class
     * @param parentName the parent name
     */
    public TreeCrudUtil(CrudService crudService, Class<T> entityClass, String parentName) {
        this.crudService = crudService;
        this.entityClass = entityClass;
        this.parentName = parentName;
    }

    /**
     * Gets the roots.
     *
     * @return the roots
     */
    public List<T> getRoots() {
        return getRoots(new QueryParameters());
    }

    /**
     * Gets the roots.
     *
     * @param qp the qp
     * @return the roots
     */
    public List<T> getRoots(QueryParameters qp) {
        qp.add(parentName, QueryConditions.isNull());
        return crudService.find(entityClass, qp);
    }

    /**
     * Gets the children.
     *
     * @param parent the parent
     * @return the children
     */
    public List<T> getChildren(T parent) {
        return getChildren(parent, new QueryParameters());
    }

    /**
     * Gets the children.
     *
     * @param parent the parent
     * @param qp the qp
     * @return the children
     */
    private List<T> getChildren(T parent, QueryParameters qp) {
        qp.add(parentName, parent);

        return crudService.find(entityClass, qp);
    }

}
