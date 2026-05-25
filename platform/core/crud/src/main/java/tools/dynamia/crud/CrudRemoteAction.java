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
}
