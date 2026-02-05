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

/**
 * Entity selector that allows setting a listener to handle entity selection events.
 *
 * @param <T> the type of entity to be selected
 */
public class EntitySelector<T> {

    private EntitySelectionListener<T> currentListener;

    /**
     * Selects an entity and notifies the current listener if one is set.
     *
     * @param entity the entity that has been selected
     */
    public void select(T entity) {
        if (currentListener != null) {
            currentListener.entitySelected(entity);
            currentListener = null;
        }
    }

    /**
     * Sets the listener to be notified when an entity is selected.
     *
     * @param listener the listener to set
     */
    public void setListener(EntitySelectionListener<T> listener) {
        this.currentListener = listener;
    }
}
