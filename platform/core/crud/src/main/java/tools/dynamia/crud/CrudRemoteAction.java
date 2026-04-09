package tools.dynamia.crud;

import tools.dynamia.actions.ClassAction;
import tools.dynamia.actions.RemoteAction;


public interface CrudRemoteAction extends RemoteAction, ClassAction {

    /**
     * Gets the states where this action is applicable.
     *
     * @return the array of applicable CRUD states
     */
    CrudState[] getApplicableStates();
}
