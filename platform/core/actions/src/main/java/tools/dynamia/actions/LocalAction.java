package tools.dynamia.actions;

/**
 * A LocalAction is an Action that is executed server-side, typically triggered from the user interface or application logic.
 *
 */
public interface LocalAction extends Action {

    /**
     * Called when the action is performed server-side (e.g., from UI or logic).
     * <p>
     * Implement this method to define the behavior when the action is triggered by the user or system.
     * </p>
     *
     * @param evt the action event
     */
    void actionPerformed(ActionEvent evt);
}
