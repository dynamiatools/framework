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
import tools.dynamia.actions.AbstractLocalAction;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.ActionSelfFilter;
import tools.dynamia.commons.Callback;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.saas.api.dto.AccountDTO;

import java.util.function.Consumer;
import java.util.function.Supplier;

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
public abstract class AccountAdminAction extends AbstractLocalAction implements ActionSelfFilter {

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

    /**
     * Runs the work as the target account of this action. Admin actions run in the session of another account (usually
     * the system account), so queries on tenant-filtered entities must be bound to the target account. The tenant is
     * held in a scoped value, so call it again inside every callback that runs later (dialog answers, long
     * operations); binding it in {@link #actionPerformed(ActionEvent)} alone does not reach them.
     *
     * @param account the target account
     * @param work    the work
     * @param <T>     the result type
     * @return the result of the work
     */
    protected <T> T withAccount(AccountDTO account, Supplier<T> work) {
        var service = Containers.get().findObject(AccountServiceAPI.class);
        if (service == null) {
            return work.get();
        }
        return service.withAccount(account.getId(), work);
    }

    /**
     * Same as {@link #withAccount(AccountDTO, Supplier)} for work without a result.
     *
     * @param account the target account
     * @param work    the work
     */
    protected void runAsAccount(AccountDTO account, Runnable work) {
        withAccount(account, () -> {
            work.run();
            return null;
        });
    }

    /**
     * Wraps a callback so it runs as the target account, for dialog answers such as
     * {@code UIMessages.showInput(..., inAccount(account, value -> ...))}.
     *
     * @param account  the target account
     * @param callback the callback
     * @param <T>      the callback argument type
     * @return a callback that binds the target account while it runs
     */
    protected <T> Consumer<T> inAccount(AccountDTO account, Consumer<T> callback) {
        return value -> runAsAccount(account, () -> callback.accept(value));
    }

    /**
     * Wraps a {@link Callback} so it runs as the target account, for confirmation dialogs such as
     * {@code UIMessages.showQuestion(..., inAccount(account, () -> ...))}.
     *
     * @param account  the target account
     * @param callback the callback
     * @return a callback that binds the target account while it runs
     */
    protected Callback inAccount(AccountDTO account, Callback callback) {
        return () -> runAsAccount(account, callback::doSomething);
    }
}
