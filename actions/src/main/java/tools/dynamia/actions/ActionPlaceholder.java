package tools.dynamia.actions;

/**
 * Placeholder action that performs no operation when executed.
 * <p>
 * This class can be used as a dummy or empty action in places where an {@link Action} instance is required
 * but no actual behavior is needed. Useful for UI layouts, default values, or disabling actions.
 * </p>
 *
 * <b>Example usage:</b>
 * <pre>
 *     // Add a placeholder action to a toolbar or menu
 *     viewer.addAction(new ActionPlaceholder());
 * </pre>
 *
 * @author Mario A. Serrano Leones
 */
public class ActionPlaceholder extends AbstractAction {
    /**
     * Does nothing when the action is performed.
     * <p>
     * This method is intentionally left empty to indicate that no operation should occur.
     * </p>
     *
     * @param evt the action event (ignored)
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        // do nothing
    }
}
