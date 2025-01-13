package tools.dynamia.app.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationMetadataActions {

    private List<ActionMetadata> actions;

    public ApplicationMetadataActions() {
    }

    public ApplicationMetadataActions(List<ActionMetadata> actions) {
        this.actions = actions;
    }

    public List<ActionMetadata> getActions() {
        return actions;
    }

    public void setActions(List<ActionMetadata> actions) {
        this.actions = actions;
    }
}
