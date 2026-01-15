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
package tools.dynamia.commons;

/**
 * Functional interface for listening to property change events from beans or other objects.
 * <p>
 * Implementations of this interface can be registered to receive notifications when a property value changes
 * in a source object. This is commonly used in observer patterns, event-driven architectures, and UI frameworks
 * to react to changes in model state.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * PropertyChangeListener listener = evt -> {
 *     System.out.println("Property " + evt.getPropertyName() + " changed from " + evt.getOldValue() + " to " + evt.getNewValue());
 * };
 * }
 * </pre>
 *
 * @author Mario A. Serrano Leones
 */
@FunctionalInterface
public interface PropertyChangeListener {

    /**
     * Invoked when a property value has changed in a source object.
     * <p>
     * The event contains the property name, source object, old value, and new value.
     *
     * @param evt the {@link PropertyChangeEvent} describing the change
     */
    void propertyChange(PropertyChangeEvent evt);
}
