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
package tools.dynamia.crud;

import tools.dynamia.commons.StringUtils;
import tools.dynamia.domain.AbstractEntity;
import tools.dynamia.navigation.RendereablePage;

/**
 *
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings({"rawtypes"})
public abstract class AbstractCrudPage<T> extends RendereablePage<T> {

    private static final long serialVersionUID = -4645019919823261595L;
    private Class entityClass;

    public AbstractCrudPage(Class<? extends AbstractEntity> entityClass) {
        this("crud" + entityClass.getSimpleName(), StringUtils.addSpaceBetweenWords(entityClass.getSimpleName()), entityClass);
    }

    public AbstractCrudPage(String id, String name, Class<? extends AbstractEntity> entityClass) {
        super(id, name, entityClass.getName());
        this.entityClass = entityClass;
    }

    protected void loadObjectClass() {
        try {
            if (entityClass == null) {
                entityClass = Class.forName(getPath());
            }
        } catch (ClassNotFoundException classNotFoundException) {
            throw new CrudPageException("Invalid class path", classNotFoundException);
        }
    }

    public Class getEntityClass() {
        return entityClass;
    }

}
