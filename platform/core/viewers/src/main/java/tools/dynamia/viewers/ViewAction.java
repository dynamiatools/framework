package tools.dynamia.viewers;

import tools.dynamia.actions.AbstractAction;

/**
 * Base class for actions that are contextually bound to a {@link View}.
 *
 * <p>A {@code ViewAction} extends {@link AbstractAction} to provide all standard action
 * capabilities (id, label, icon, enabled/visible state, execution) in a viewer-aware context.
 * Subclasses are typically registered with a view or a {@link ViewDescriptor} via
 * {@link ViewDescriptor#getActions()} and rendered as toolbar buttons, menu items, or
 * inline controls by the {@link ViewRenderer}.</p>
 *
 * <p>Because view actions inherit from {@link AbstractAction}, they participate in the same
 * action lifecycle (enable/disable, show/hide, execute with an
 * {@link tools.dynamia.actions.ActionEvent}) as any other framework action, but can also
 * interact directly with the hosting view.</p>
 *
 * @see AbstractAction
 * @see ViewDescriptor#getActions()
 */
public abstract class ViewAction extends AbstractAction {
}
