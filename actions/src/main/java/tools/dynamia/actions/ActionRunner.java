package tools.dynamia.actions;

/**
 * Defines a contract for running an {@link Action} with a given {@link ActionEvent}.
 * Implementations should provide the logic to execute the specified action.
 */
public interface ActionRunner {

    /**
     * Executes the given action using the provided event context.
     *
     * @param action the action to be executed
     * @param evt the event context for the action
     */
    void run(Action action, ActionEvent evt);
}
