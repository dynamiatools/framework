package tools.dynamia.app.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.dynamia.actions.Action;
import tools.dynamia.actions.ClassAction;
import tools.dynamia.app.controllers.ApplicationMetadataController;
import tools.dynamia.commons.Streams;
import tools.dynamia.crud.CrudAction;

import java.util.List;

/**
 * Metadata for Action
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionMetadata extends BasicMetadata {

    private String group;
    private String renderer;

    private List<String> applicableClasses;

    private List<String> applicableStates;

    @JsonIgnore
    private Action action;

    public ActionMetadata() {
    }

    public ActionMetadata(Action action) {
        setId(action.getId());
        setName(action.getLocalizedName());
        setDescription(action.getLocalizedDescription());
        setIcon(action.getImage());
        setEndpoint(ApplicationMetadataController.PATH + "/actions/execute/" + getId());

        var actionRenderer = action.getRenderer();
        this.renderer = actionRenderer != null ? actionRenderer.getClass().getName() : null;
        this.group = action.getGroup() != null ? action.getGroup().getName() : null;

        if (action instanceof CrudAction crudAction) {
            this.applicableStates = Streams.mapAndCollect(crudAction.getApplicableStates(), Enum::name);
        }

        if (action instanceof ClassAction classAction) {
            this.applicableClasses = Streams.mapAndCollect(classAction.getApplicableClasses(), a -> a.targetClass() == null ? "all" : a.targetClass().getName());
        }
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getRenderer() {
        return renderer;
    }

    public void setRenderer(String renderer) {
        this.renderer = renderer;
    }

    public List<String> getApplicableClasses() {
        return applicableClasses;
    }

    public void setApplicableClasses(List<String> applicableClasses) {
        this.applicableClasses = applicableClasses;
    }

    public List<String> getApplicableStates() {
        return applicableStates;
    }

    public void setApplicableStates(List<String> applicableStates) {
        this.applicableStates = applicableStates;
    }

    public Action getAction() {
        return action;
    }
}
