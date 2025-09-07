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
 * Represents metadata information for an {@link Action} in the application.
 * <p>
 * This class encapsulates properties and configuration details for actions, such as group, renderer, applicable classes, and states.
 * It is used to describe actions in a way that can be serialized and consumed by clients or other layers of the application.
 * <p>
 * The metadata is typically generated from an {@link Action} instance and includes information relevant for UI rendering and execution.
 *
 * <ul>
 *     <li>Group: Logical grouping of the action for UI organization.</li>
 *     <li>Renderer: The class name of the renderer used to display the action.</li>
 *     <li>Applicable Classes: List of class names where the action is applicable.</li>
 *     <li>Applicable States: List of states in which the action can be executed.</li>
 * </ul>
 *
 * @author Mario A. Serrano Leones
 * @since 2023
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionMetadata extends BasicMetadata {

    /**
     * The logical group to which this action belongs, used for UI organization.
     */
    private String group;

    /**
     * The fully qualified class name of the renderer used to display this action.
     */
    private String renderer;

    /**
     * List of class names where this action is applicable. Used for filtering actions by context.
     */
    private List<String> applicableClasses;

    /**
     * List of states in which this action can be executed. Used for state-based filtering.
     */
    private List<String> applicableStates;

    /**
     * The underlying {@link Action} instance. This field is ignored during JSON serialization.
     */
    @JsonIgnore
    private Action action;

    /**
     * Default constructor for serialization and manual instantiation.
     */
    public ActionMetadata() {
    }

    /**
     * Constructs an {@code ActionMetadata} from the given {@link Action} instance.
     * <p>
     * Copies relevant properties from the action, including id, name, description, icon, endpoint, renderer, group,
     * applicable states (for {@link CrudAction}), and applicable classes (for {@link ClassAction}).
     *
     * @param action the {@link Action} instance to extract metadata from
     */
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

    /**
     * Returns the logical group name of this action.
     *
     * @return the group name
     */
    public String getGroup() {
        return group;
    }

    /**
     * Sets the logical group name for this action.
     *
     * @param group the group name to set
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Returns the fully qualified class name of the renderer for this action.
     *
     * @return the renderer class name
     */
    public String getRenderer() {
        return renderer;
    }

    /**
     * Sets the renderer class name for this action.
     *
     * @param renderer the renderer class name to set
     */
    public void setRenderer(String renderer) {
        this.renderer = renderer;
    }

    /**
     * Returns the list of class names where this action is applicable.
     *
     * @return the list of applicable class names
     */
    public List<String> getApplicableClasses() {
        return applicableClasses;
    }

    /**
     * Sets the list of class names where this action is applicable.
     *
     * @param applicableClasses the list of applicable class names to set
     */
    public void setApplicableClasses(List<String> applicableClasses) {
        this.applicableClasses = applicableClasses;
    }

    /**
     * Returns the list of states in which this action can be executed.
     *
     * @return the list of applicable states
     */
    public List<String> getApplicableStates() {
        return applicableStates;
    }

    /**
     * Sets the list of states in which this action can be executed.
     *
     * @param applicableStates the list of applicable states to set
     */
    public void setApplicableStates(List<String> applicableStates) {
        this.applicableStates = applicableStates;
    }

    /**
     * Returns the underlying {@link Action} instance.
     * <p>
     * This field is ignored during JSON serialization and is intended for internal use only.
     *
     * @return the {@link Action} instance
     */
    public Action getAction() {
        return action;
    }
}
