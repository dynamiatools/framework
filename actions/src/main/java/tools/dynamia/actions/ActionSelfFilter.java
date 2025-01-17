package tools.dynamia.actions;

/**
 * Action filter interface
 */
public interface ActionSelfFilter {

    default void beforeActionPerformed(ActionEvent evt) {
    }

    default void afterActionPerformed(ActionEvent evt) {
    }

    default void beforeActionExecution(ActionExecutionRequest request) {
    }

    default void afterActionExecution(ActionExecutionRequest request, ActionExecutionResponse response) {
    }
}
