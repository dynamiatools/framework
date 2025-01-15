package tools.dynamia.actions;

/**
 * Action filter interface
 */
public interface ActionFilter {

    default void beforeActionPerformed(Action action, ActionEvent evt) {
    }

    default void afterActionPerformed(Action action, ActionEvent evt) {
    }

    default void beforeActionExecution(Action action, ActionExecutionRequest request) {
    }

    default void afterActionExecution(Action action, ActionExecutionRequest request, ActionExecutionResponse response) {
    }
}
