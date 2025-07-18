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
 * The Interface PropertyChangeListenerContainer. Represents objects that can manage property change listeners.
 *
 * @author Mario A. Serrano Leones
 */
public interface PropertyChangeListenerContainer {

    /**
     * Add a PropertyChangeListener to get object change, subclasses must invoke
     * notifyChange to fire listeners
     *
     * @param listener the property change listener to add
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Remove PropertyChangeListener
     *
     * @param listener the property change listener to remove
     */
    void removePropertyChangeListener(PropertyChangeListener listener);
}
