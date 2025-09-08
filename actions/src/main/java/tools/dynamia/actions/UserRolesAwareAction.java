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

package tools.dynamia.actions;

/**
 * Interface for actions that are aware of user roles for security purposes.
 * <p>
 * Implementations of this interface allow external security modules to enable or restrict actions
 * based on specific user roles. This is useful for integrating role-based access control (RBAC)
 * into the action system, ensuring that only authorized users can execute certain actions.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 *     public class AdminOnlyAction implements UserRolesAwareAction {
 *         @Override
 *         public String[] getRoles() {
 *             return new String[] {"ADMIN"};
 *         }
 *     }
 * </pre>
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
public interface UserRolesAwareAction {

    /**
     * Returns the array of user roles that are allowed to execute this action.
     * <p>
     * The returned array should contain all roles for which this action is enabled. If the array is empty or null,
     * the action may be considered available to all roles, depending on the implementation.
     * </p>
     *
     * @return an array of allowed user roles
     */
    String[] getRoles();
}
