package tools.dynamia.actions;

import java.util.List;

/**
 * Interface to allow actions to be container
 */
public interface ActionsContainer {

    void addAction(Action action);

    List<Action> getActions();
}
