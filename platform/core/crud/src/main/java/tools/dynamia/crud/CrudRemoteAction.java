package tools.dynamia.crud;

import tools.dynamia.actions.ClassAction;
import tools.dynamia.actions.RemoteAction;


/**
 * Remote CRUD action contract with class-aware behavior.
 * Implementations represent actions that can be executed remotely
 * and are limited to specific {@link CrudState} values.
 */
public interface CrudRemoteAction extends RemoteAction, ClassAction {

    /**
     * Gets the states where this action is applicable.
     *
     * @return the array of applicable CRUD states
     */
    CrudState[] getApplicableStates();

    /**
     * Whether this action is listed in the context (row) menu of a record, like {@link CrudAction#isMenuSupported()}
     * does for local actions. A hand written remote action is meant for REST clients, whose only place for a
     * per-record action is that menu, so it is listed unless it says otherwise.
     *
     * @return {@code true} by default
     */
    default boolean isMenuSupported() {
        return true;
    }
}
