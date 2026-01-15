package tools.dynamia.actions;

import java.util.List;

/**
 * Represents a container for {@link Action} objects.
 * <p>
 * Implementations of this interface allow adding actions and retrieving the list of contained actions.
 * This is useful for grouping related actions together, such as in toolbars, menus, or other UI components.
 * </p>
 */
public interface ActionsContainer {

    /**
     * Adds an {@link Action} to this container.
     *
     * @param action the action to add; must not be null
     */
    void addAction(Action action);

    /**
     * Returns the list of {@link Action} objects contained in this container.
     *
     * @return a list of actions; never null
     */
    List<Action> getActions();
}
