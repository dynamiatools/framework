package tools.dynamia.app.metadata;

import tools.dynamia.actions.Action;

public class ActionMetadata extends BasicMetadata {

    public ActionMetadata() {
    }

    public ActionMetadata(Action action) {
        setId(action.getId());
        setName(action.getName());
        setDescription(action.getDescription());
        setIcon(action.getImage());
    }
}
