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

package tools.dynamia.modules.saas.api;

import tools.dynamia.actions.AbstractAction;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.ActionSelfFilter;
import tools.dynamia.integration.Containers;

/**
 * Base class for administrative actions that require authorization in a SaaS environment.
 * <p>
 * This class extends {@link AbstractAction} and provides built-in support for authorization
 * checks before executing administrative operations. It integrates with
 * {@link AccountAdminActionAuthorizationProvider} to enforce security policies.
 * <p>
 * Actions extending this class can require authorization by setting the
 * {@code authorizationRequired} flag. When enabled, the action will delegate to the
 * configured authorization provider before executing.
 * <p>
 * Example usage:
 * <pre>{@code
 * public class DeleteAccountAction extends AccountAdminAction {
 *     public DeleteAccountAction() {
 *         setName("Delete Account");
 *         setAuthorizationRequired(true);
 *     }
 *
 *     @Override
 *     public void actionPerformed(ActionEvent evt) {
 *         // Delete account logic
 *     }
 * }
 * }</pre>
 *
 * @author Mario Serrano Leones
 * @see AccountAdminActionAuthorizationProvider
 * @see AbstractAction
 */
public abstract class AccountAdminAction extends AbstractAction implements ActionSelfFilter {

    private boolean authorizationRequired;

    /**
     * Hook method executed before the action is performed.
     * <p>
     * If authorization is required, this method will delegate to the
     * {@link AccountAdminActionAuthorizationProvider} to perform authorization checks.
     * The actual action will only execute if authorization is granted.
     *
     * @param evt the action event
     */
    @Override
    public void beforeActionPerformed(ActionEvent evt) {
        if (isAuthorizationRequired()) {
            var provider = Containers.get().findObject(AccountAdminActionAuthorizationProvider.class);

            if (provider != null) {
                evt.stopPropagation();
                provider.authorize(this, evt, () -> actionPerformed(evt));
            }
        }
    }

    /**
     * Hook method executed after the action is performed.
     * <p>
     * This implementation does nothing and can be overridden by subclasses
     * to add post-execution logic.
     *
     * @param evt the action event
     */
    @Override
    public void afterActionPerformed(ActionEvent evt) {
        //do nothing
    }

    /**
     * Checks if authorization is required before executing this action.
     *
     * @return true if authorization is required, false otherwise
     */
    public boolean isAuthorizationRequired() {
        return authorizationRequired;
    }

    /**
     * Sets whether authorization is required before executing this action.
     *
     * @param authorizationRequired true to require authorization, false otherwise
     */
    public void setAuthorizationRequired(boolean authorizationRequired) {
        this.authorizationRequired = authorizationRequired;
    }
}
