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
 * Represents an event indicating that a property value has changed in a source object.
 * <p>
 * This record is commonly used in observer patterns, property change listeners, and event-driven architectures
 * to notify interested parties about changes in object state. It encapsulates the property name, the source object,
 * the old value, and the new value of the property.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * PropertyChangeEvent event = new PropertyChangeEvent("age", person, 30, 31);
 * String property = event.getPropertyName();
 * Object oldValue = event.getOldValue();
 * Object newValue = event.getNewValue();
 * }
 * </pre>
 *
 * @author Mario A. Serrano Leones
 */
public record PropertyChangeEvent(String propertyName, Object source, Object oldValue, Object newValue) {

    /**
     * Returns the name of the property that changed.
     *
     * @return the property name
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Returns the source object where the property change occurred.
     *
     * @return the source object
     */
    public Object getSource() {
        return source;
    }

    /**
     * Returns the old value of the property before the change.
     *
     * @return the old value
     */
    public Object getOldValue() {
        return oldValue;
    }

    /**
     * Returns the new value of the property after the change.
     *
     * @return the new value
     */
    public Object getNewValue() {
        return newValue;
    }
}
