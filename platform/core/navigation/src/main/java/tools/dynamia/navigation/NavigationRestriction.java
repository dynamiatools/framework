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
package tools.dynamia.navigation;

/**
 * Interface for implementing access control restrictions on navigation elements.
 * <p>
 * Classes that implement this interface can define custom logic to allow or deny access
 * to specific navigation elements (such as pages, modules, or groups) within the application.
 * Restrictions are evaluated in order, allowing for flexible and layered access control.
 * </p>
 * <p>
 * Usage: Register implementations as beans to participate in the navigation restriction chain.
 * </p>
 *
 * @author Mario A. Serrano Leones
 * @since 2023
 */
public interface NavigationRestriction {

    /**
     * Returns the order in which this restriction should be evaluated.
     * <p>
     * Lower values indicate higher priority. Restrictions with higher order values are evaluated later.
     * </p>
     *
     * @return the order value for this restriction
     */
    int getOrder();

    /**
     * Determines whether access to the specified navigation element is allowed.
     * <ul>
     *     <li>Return {@code true} to grant access.</li>
     *     <li>Return {@code false} to deny access.</li>
     *     <li>Return {@code null} if undecided, allowing other restrictions to evaluate.</li>
     * </ul>
     *
     * @param element the {@link NavigationElement} to check
     * @return {@code true} if access is granted, {@code false} if denied, or {@code null} if undecided
     */
    Boolean allowAccess(NavigationElement element);

}
