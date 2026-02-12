package tools.dynamia.modules.saas.api;

import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.commons.Callback;

/**
 * Interface for providing custom authorization logic for administrative actions in a SaaS environment.
 * <p>
 * This interface allows modules to implement custom authorization checks before executing
 * administrative actions on accounts. It provides a hook point for adding security layers,
 * audit requirements, or business rules that must be validated before an action proceeds.
 * <p>
 * The authorization process is asynchronous, using a callback mechanism to either grant or deny access.
 * This design allows for complex authorization workflows, including user confirmations, multi-factor
 * authentication, or external authorization services.
 * <p>
 * Example usage:
 * <pre>{@code
 * @Component
 * public class MFAAuthorizationProvider implements AccountAdminActionAuthorizationProvider {
 *     @Override
 *     public void authorize(Action action, ActionEvent evt, Callback onAuthorization) {
 *         if (requiresMFA(action)) {
 *             promptForMFA(success -> {
 *                 if (success) {
 *                     onAuthorization.doSomething();
 *                 }
 *             });
 *         } else {
 *             onAuthorization.doSomething();
 *         }
 *     }
 * }
 * }</pre>
 *
 * @author Mario Serrano Leones
 */
public interface AccountAdminActionAuthorizationProvider {

    /**
     * Performs authorization checks for an administrative action.
     * <p>
     * This method is called before executing administrative actions on accounts.
     * Implementations should perform any necessary authorization checks and invoke
     * the callback if authorization is granted. If authorization is denied, the
     * callback should not be invoked.
     *
     * @param action the action being authorized
     * @param evt the event context containing information about the action execution
     * @param onAuthorization callback to invoke if authorization is granted
     */
    void authorize(Action action, ActionEvent evt, Callback onAuthorization);
}
