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

import tools.dynamia.integration.Containers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Utility class for managing {@link ActionRestriction} instances and checking access to actions.
 * <p>
 * This class provides static methods to evaluate whether an {@link Action} is allowed based on registered restrictions.
 * Restrictions are discovered via the {@link Containers} integration and sorted by their priority (order).
 * The first restriction that returns a non-null value determines the access decision.
 * </p>
 * <p>
 * <b>Example usage:</b>
 * <pre>
 *     Boolean allowed = ActionRestrictions.allowAccess(myAction);
 *     if (Boolean.TRUE.equals(allowed)) {
 *         // Access granted
 *     } else {
 *         // Access denied
 *     }
 * </pre>
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
public final class ActionRestrictions {

    /**
     * Private constructor to prevent instantiation.
     */
    private ActionRestrictions() {
    }

    /**
     * Checks if access to the given {@link Action} is allowed according to all registered {@link ActionRestriction} instances.
     * <p>
     * Restrictions are sorted by their order (priority). The first restriction that returns a non-null value
     * determines the result. If no restrictions are found or all return null, access is allowed by default.
     * </p>
     *
     * @param action the action to check
     * @return {@code true} if access is allowed, {@code false} if denied, or {@code null} if undecided
     */
    public static Boolean allowAccess(Action action) {
        // Always allow if the action is of type AlwaysAllowedAction
        if (action instanceof AlwaysAllowedAction) {
            return true;
        }

        Boolean allowed = true;
        Collection<ActionRestriction> restrictions = getActionRestrictions();
        if (restrictions != null) {
            List<ActionRestriction> restrictionsSorted = new ArrayList<>(restrictions);
            restrictionsSorted.sort((Comparator.comparingInt(ActionRestriction::getOrder)));
            for (ActionRestriction actionRestriction : restrictionsSorted) {
                allowed = actionRestriction.actionAllowed(action);
                if (allowed != null) {
                    break;
                }
            }
        }
        return allowed;
    }

    /**
     * Retrieves all registered {@link ActionRestriction} instances from the application context.
     *
     * @return a collection of action restrictions
     */
    public static Collection<ActionRestriction> getActionRestrictions() {
        return Containers.get().findObjects(ActionRestriction.class);
    }
}
