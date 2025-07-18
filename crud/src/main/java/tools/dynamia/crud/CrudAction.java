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

import tools.dynamia.actions.ClassAction;

/**
 * The Interface CrudAction. Represents actions that can be performed in CRUD operations.
 * This interface extends ClassAction to provide specialized functionality for Create, Read, Update,
 * and Delete operations in data management interfaces. CRUD actions are context-aware and can be
 * configured to appear only in specific states (viewing, editing, creating), providing a dynamic
 * and state-sensitive user interface for data manipulation operations.
 * <br><br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * public class DeleteUserAction implements CrudAction {
 *     
 *     public CrudState[] getApplicableStates() {
 *         return new CrudState[]{CrudState.READ};
 *     }
 *     
 *     public boolean isMenuSupported() {
 *         return true;
 *     }
 *     
 *     public void actionPerformed(ActionEvent evt) {
 *         User user = (User) evt.getData();
 *         userService.delete(user);
 *     }
 * }
 * </code>
 *
 * @author Mario A. Serrano Leones
 */
public interface CrudAction extends ClassAction {

    /**
     * Gets the states where this action is applicable.
     *
     * @return the array of applicable CRUD states
     */
    CrudState[] getApplicableStates();

    /**
     * Checks if this action supports menu display.
     *
     * @return true if menu is supported, false otherwise
     */
    boolean isMenuSupported();

}
