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

package tools.dynamia.modules.dashboard;

import java.util.List;

/**
 * Provides user identity and authorization data for dashboard runtime decisions.
 * Implementations are used by widgets and dashboard services to evaluate visibility,
 * permissions, and user-specific behavior.
 */
public interface UserInfoProvider {

    /**
     * Returns the current authenticated username.
     *
     * @return the username associated with the current user context
     */
    String getUsername();

    /**
     * Indicates whether the current user has administrative privileges.
     *
     * @return {@code true} when the user is an administrator
     */
    boolean isAdmin();

    /**
     * Checks if the current user has the specified role.
     *
     * @param roleName the role name to verify
     * @return {@code true} when the user is assigned to the role
     */
    boolean hasRole(String roleName);

    /**
     * Checks if the current user has the specified grant/permission.
     *
     * @param grant the grant identifier to verify
     * @return {@code true} when the user has the grant
     */
    boolean hasGrant(String grant);

    /**
     * Returns the location identifier associated with the current user.
     *
     * @return the user location id, or {@code null} when not applicable
     */
    Long getUserLocation();

    /**
     * Returns all role names assigned to the current user.
     *
     * @return a list of user role names
     */
    List<String> getUserRoles();
}
