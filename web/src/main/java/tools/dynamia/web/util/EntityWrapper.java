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
package tools.dynamia.web.util;

import java.io.Serializable;

/**
 *
 * @author Ing. Mario Serrano Leones
 * @param <E> the entity type
 */
public class EntityWrapper<E> implements Serializable {

    private static final long serialVersionUID = 2183965104645026082L;
    private E entity;
    private boolean selected;

    public EntityWrapper(E entity) {
        this.entity = entity;
    }

    public E getEntity() {
        return entity;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
