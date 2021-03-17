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
package tools.dynamia.domain;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Ing. Mario Serrano
 */
public class SelectedEntity<T extends AbstractEntity> implements Serializable {

    private T value;
    private final Set<EntitySelectionListener<T>> listeners = Collections.synchronizedSet(new HashSet<>());

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
        if (value != null) {
            fireListeners();
        }
    }

    public void addListener(EntitySelectionListener<T> listener) {
        listeners.add(listener);
    }

    public void removeListener(EntitySelectionListener<T> listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public void removeListener(Class listenerClass) {
        EntitySelectionListener<T> toRemove = null;
        if (!listeners.isEmpty()) {
            for (EntitySelectionListener<T> listener : listeners) {
                if (listener.getClass().equals(listenerClass)) {
                    toRemove = listener;
                    break;
                }
            }
        }
        removeListener(toRemove);
    }

    private void fireListeners() {
        if (!listeners.isEmpty()) {
            for (EntitySelectionListener<T> listener : listeners) {
                listener.entitySelected(value);
            }
        }
    }
}
