package tools.dynamia.app.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tools.dynamia.app.controllers.ApplicationMetadataController;

import java.util.List;

public class EntityMetadata extends BasicMetadata {

    private String className;
    private List<ActionMetadata> actions;
    private List<ViewDescriptorMetadata> descriptors;

    private String actionsEndpoint;
    private String viewsEndpoint;

    @JsonIgnore
    private Class entityClass;

    public EntityMetadata() {
    }

    public EntityMetadata(Class entityClass) {
        setEntityClass(entityClass);
        setClassName(entityClass.getName());
        setName(entityClass.getSimpleName());
        setEndpoint(ApplicationMetadataController.PATH + "/entities/" + getClassName());
        setActionsEndpoint(ApplicationMetadataController.PATH + "/entities/" + getClassName() + "/actions");
        setViewsEndpoint(ApplicationMetadataController.PATH + "/entities/" + getClassName() + "/views");
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<ActionMetadata> getActions() {
        return actions;
    }

    public void setActions(List<ActionMetadata> actions) {
        this.actions = actions;
    }

    public List<ViewDescriptorMetadata> getDescriptors() {
        return descriptors;
    }

    public void setDescriptors(List<ViewDescriptorMetadata> descriptors) {
        this.descriptors = descriptors;
    }


    public Class getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class entityClass) {
        this.entityClass = entityClass;
    }

    public String getActionsEndpoint() {
        return actionsEndpoint;
    }

    public void setActionsEndpoint(String actionsEndpoint) {
        this.actionsEndpoint = actionsEndpoint;
    }

    public String getViewsEndpoint() {
        return viewsEndpoint;
    }

    public void setViewsEndpoint(String viewsEndpoint) {
        this.viewsEndpoint = viewsEndpoint;
    }
}
