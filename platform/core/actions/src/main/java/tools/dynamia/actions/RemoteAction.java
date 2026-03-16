package tools.dynamia.actions;

/**
 * A RemoteAction is an Action that can be executed remotely, such as through a REST API or automation framework.
 * <p>
 * This interface extends the basic Action interface and adds a method for executing the action using an ActionExecutionRequest, which allows for processing action results in external systems.
 * Implement this interface for actions that need to be triggered from outside the application, such as via API calls or automated tasks.
 * </p>
 */
public interface RemoteAction extends Action {



    /**
     * Executes this action using an {@link ActionExecutionRequest} instead of an {@link ActionEvent}.
     * <p>
     * Implement this method to process the action result for external systems (e.g., REST API, automation).
     * The default implementation returns a response with success=false.
     * </p>
     *
     * @param request the execution request
     * @return the execution response
     */
    ActionExecutionResponse execute(ActionExecutionRequest request);
}
