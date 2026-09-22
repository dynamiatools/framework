package tools.dynamia.app.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tools.dynamia.app.controllers.ApplicationMetadataController;
import java.util.List;

/**
 * Metadata information for an entity in the application.
 * <p>
 * This class encapsulates properties and configuration details for entities, such as class name, actions, view descriptors, and endpoints.
 * It is used to describe entities in a way that can be serialized and consumed by clients or other layers of the application.
 * <p>
 * The metadata is typically generated from an entity class and includes information relevant for UI rendering and execution.
 *
 * @author Mario A. Serrano Leones
 * @since 2023
 */
public class EntityMetadata extends BasicMetadata {

    /**
     * The fully qualified class name of the entity.
     * <p>
     * Kept for server-side resolution only — never serialized to clients. The public identifier
     * for this entity is {@link #getId()} (the class's simple name), which is what appears in
     * {@code endpoint}/{@code actionsEndpoint}/{@code viewsEndpoint} and in
     * {@code /api/app/metadata/entities/{id}}. See {@code docs/design} security note on why the
     * FQCN (package structure) is not sent over the wire.
     */
    @JsonIgnore
    private String className;
    /**
     * List of action metadata objects associated with this entity.
     */
    private List<ActionMetadata> actions;
    /**
     * List of view descriptor metadata objects associated with this entity.
     */
    private List<ViewDescriptorMetadata> descriptors;
    /**
     * API endpoint for actions related to this entity.
     */
    private String actionsEndpoint;
    /**
     * API endpoint for views related to this entity.
     */
    private String viewsEndpoint;
    /**
     * The underlying entity class. This field is ignored during JSON serialization.
     */
    @JsonIgnore
    private Class entityClass;

    /**
     * Default constructor for serialization and manual instantiation.
     */
    public EntityMetadata() {
    }

    /**
     * Constructs an {@code EntityMetadata} from the given entity class.
     * <p>
     * Copies relevant properties from the entity class, including class name, simple name, and sets endpoints for actions and views.
     *
     * @param entityClass the entity class to extract metadata from
     */
    public EntityMetadata(Class entityClass) {
        setEntityClass(entityClass);
        setClassName(entityClass.getName());
        setId(entityClass.getSimpleName());
        setName(entityClass.getSimpleName());
        setEndpoint(ApplicationMetadataController.PATH + "/entities/" + getId());
        setActionsEndpoint(ApplicationMetadataController.PATH + "/entities/" + getId() + "/actions");
        setViewsEndpoint(ApplicationMetadataController.PATH + "/entities/" + getId() + "/views");
    }

    /**
     * Returns the fully qualified class name of the entity. Server-side use only — {@code @JsonIgnore}d,
     * never sent to clients. Use {@link #getId()} for the client-facing identifier.
     * @return the class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the fully qualified class name of the entity.
     * @param className the class name to set
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Returns the list of action metadata objects associated with this entity.
     * @return the list of {@link ActionMetadata}
     */
    public List<ActionMetadata> getActions() {
        return actions;
    }

    /**
     * Sets the list of action metadata objects associated with this entity.
     * @param actions the list of {@link ActionMetadata} to set
     */
    public void setActions(List<ActionMetadata> actions) {
        this.actions = actions;
    }

    /**
     * Returns the list of view descriptor metadata objects associated with this entity.
     * @return the list of {@link ViewDescriptorMetadata}
     */
    public List<ViewDescriptorMetadata> getDescriptors() {
        return descriptors;
    }

    /**
     * Sets the list of view descriptor metadata objects associated with this entity.
     * @param descriptors the list of {@link ViewDescriptorMetadata} to set
     */
    public void setDescriptors(List<ViewDescriptorMetadata> descriptors) {
        this.descriptors = descriptors;
    }

    /**
     * Returns the API endpoint for actions related to this entity.
     * @return the actions endpoint
     */
    public String getActionsEndpoint() {
        return actionsEndpoint;
    }

    /**
     * Sets the API endpoint for actions related to this entity.
     * @param actionsEndpoint the actions endpoint to set
     */
    public void setActionsEndpoint(String actionsEndpoint) {
        this.actionsEndpoint = actionsEndpoint;
    }

    /**
     * Returns the API endpoint for views related to this entity.
     * @return the views endpoint
     */
    public String getViewsEndpoint() {
        return viewsEndpoint;
    }

    /**
     * Sets the API endpoint for views related to this entity.
     * @param viewsEndpoint the views endpoint to set
     */
    public void setViewsEndpoint(String viewsEndpoint) {
        this.viewsEndpoint = viewsEndpoint;
    }

    /**
     * Returns the underlying entity class. This field is ignored during JSON serialization and is intended for internal use only.
     * @return the entity class
     */
    public Class getEntityClass() {
        return entityClass;
    }

    /**
     * Sets the underlying entity class. This field is ignored during JSON serialization and is intended for internal use only.
     * @param entityClass the entity class to set
     */
    public void setEntityClass(Class entityClass) {
        this.entityClass = entityClass;
    }
}
