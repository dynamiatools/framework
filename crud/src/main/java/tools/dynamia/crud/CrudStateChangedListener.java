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
package tools.dynamia.crud;

/**
 * The Interface CrudStateChangedListener. Listens for changes in CRUD operation states.
 * This interface provides a mechanism for observing state transitions in CRUD components,
 * allowing applications to respond to changes between CREATE, READ, UPDATE, and DELETE modes.
 * State change listeners are commonly used for updating UI elements, enabling/disabling actions,
 * refreshing data, and implementing business logic that depends on the current operation mode.
 * <br><br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * public class CrudToolbarManager implements CrudStateChangedListener {
 *     
 *     public void changedState(ChangedStateEvent evt) {
 *         switch (evt.getNewState()) {
 *             case CREATE:
 *                 enableSaveButton();
 *                 disableDeleteButton();
 *                 break;
 *             case READ:
 *                 enableEditButton();
 *                 enableDeleteButton();
 *                 break;
 *             case UPDATE:
 *                 enableSaveButton();
 *                 enableCancelButton();
 *                 break;
 *         }
 *     }
 * }
 * </code>
 *
 * @author Mario A. Serrano Leones
 */
public interface CrudStateChangedListener {

    /**
     * Called when the CRUD state changes.
     *
     * @param evt the state change event
     */
    void changedState(ChangedStateEvent evt);

}
