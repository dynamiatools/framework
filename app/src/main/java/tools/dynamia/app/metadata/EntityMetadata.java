package tools.dynamia.app.metadata;

import tools.dynamia.viewers.ViewDescriptor;

import java.util.List;

public class EntityMetadata extends BasicMetadata {

    private String className;
    private List<ActionMetadata> actions;
    private List<ViewDescriptor> descriptors;

    public EntityMetadata() {
    }

    public EntityMetadata(Class entityClass) {
        setClassName(entityClass.getName());

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

    public List<ViewDescriptor> getDescriptors() {
        return descriptors;
    }

    public void setDescriptors(List<ViewDescriptor> descriptors) {
        this.descriptors = descriptors;
    }
}
