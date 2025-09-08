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
 * Interface for objects that support registration and removal of {@link PropertyChangeListener}s.
 * <p>
 * Implementing this interface allows a class to manage a set of listeners interested in property change events.
 * Subclasses must invoke their own notification logic (typically via a method like {@code notifyChange}) to fire events to listeners.
 * <p>
 * This is commonly used in observer patterns, event-driven architectures, and UI frameworks to enable reactive behavior
 * when properties of an object change.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * public class MyBean implements PropertyChangeListenerContainer {
 *     private final List<PropertyChangeListener> listeners = new ArrayList<>();
 *     public void addPropertyChangeListener(PropertyChangeListener listener) {
 *         listeners.add(listener);
 *     }
 *     public void removePropertyChangeListener(PropertyChangeListener listener) {
 *         listeners.remove(listener);
 *     }
 *     protected void notifyChange(String property, Object oldValue, Object newValue) {
 *         PropertyChangeEvent evt = new PropertyChangeEvent(property, this, oldValue, newValue);
 *         listeners.forEach(l -> l.propertyChange(evt));
 *     }
 * }
 * }
 * </pre>
 *
 * @author Mario A. Serrano Leones
 */
public interface PropertyChangeListenerContainer {

    /**
     * Registers a {@link PropertyChangeListener} to receive notifications when properties of this object change.
     * <p>
     * Subclasses must invoke their own notification logic to fire events to listeners.
     *
     * @param listener the listener to register
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes a previously registered {@link PropertyChangeListener}.
     *
     * @param listener the listener to remove
     */
    void removePropertyChangeListener(PropertyChangeListener listener);
}
