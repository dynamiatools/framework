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
 * Interface for restricting access to {@link Action} instances based on custom logic or policies.
 * <p>
 * Implement this interface if you need fine-grained control over when an action is allowed for a user or context.
 * Multiple {@code ActionRestriction} implementations can be chained or prioritized using {@link #getOrder()}.
 * </p>
 * <p>
 * The {@link #actionAllowed(Action)} method should return:
 * <ul>
 *     <li>{@code true} if access to the action is explicitly granted</li>
 *     <li>{@code false} if access is explicitly denied</li>
 *     <li>{@code null} if the restriction cannot decide, allowing other {@code ActionRestriction} instances to check</li>
 * </ul>
 * </p>
 * <p>
 * Example usage:
 * <pre>
 *     public class RoleBasedActionRestriction implements ActionRestriction {
 *         @Override
 *         public int getOrder() { return 1; }
 *         @Override
 *         public Boolean actionAllowed(Action action) {
 *             // Custom logic based on user roles
 *         }
 *     }
 * </pre>
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
public interface ActionRestriction {

    /**
     * Returns the order (priority) of this restriction in the chain of restrictions.
     * Lower values indicate higher priority.
     *
     * @return the order value
     */
    int getOrder();

    /**
     * Determines whether access to the given {@link Action} is allowed.
     * <p>
     * Return {@code true} if access is granted, {@code false} if denied, or {@code null} if undecided
     * (to allow other {@code ActionRestriction} instances to check).
     * </p>
     *
     * @param action the action to check
     * @return {@code true} if access is granted, {@code false} if denied, or {@code null} if undecided
     */
    Boolean actionAllowed(Action action);
}
