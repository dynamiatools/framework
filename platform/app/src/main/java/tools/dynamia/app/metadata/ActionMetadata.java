package tools.dynamia.app.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.dynamia.actions.ActionRuntime;
import tools.dynamia.actions.ActionRuntimes;
import tools.dynamia.actions.Action;
import tools.dynamia.actions.ClassAction;
import tools.dynamia.actions.Control;
import tools.dynamia.actions.HeadlessActionRenderer;
import tools.dynamia.actions.RemoteAction;
import tools.dynamia.app.controllers.ApplicationMetadataController;
import tools.dynamia.commons.Streams;
import tools.dynamia.crud.CrudAction;
import tools.dynamia.crud.CrudRemoteAction;

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

    /** Value of {@code renderer} for an action drawn from its {@code control}: a stable key, not a class name. */
    public static final String CONTROL_RENDERER = "control";

    /**
     * The logical group to which this action belongs, used for UI organization.
     */
    private String group;

    /**
     * The fully qualified class name of the renderer used to display this action.
     */
    private String renderer;

    /**
     * What a front end must draw in the toolbar for an action that is a filter or a similar control instead of a button, set
     * when the action's renderer is a {@link HeadlessActionRenderer}; null otherwise. In that case {@link #renderer} is
     * {@link #CONTROL_RENDERER}, not a class name.
     */
    private Control control;

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
    private RemoteAction action;

    private String type;
    private String className;
    private String runtime;
    private boolean menuSupported;
    private double position;

    /**
     * Default constructor for serialization and manual instantiation.
     */
    public ActionMetadata() {
    }

    /**
     * Constructs an {@code ActionMetadata} from the given {@link Action} instance. Only a {@link RemoteAction} gets an
     * endpoint and an executable instance; a {@code FRONTEND} action is published without them.
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
        this.runtime = ActionRuntimes.of(action).name();
        this.position = action.getPosition();
        if (action instanceof RemoteAction) {
            setEndpoint(ApplicationMetadataController.PATH + "/actions/execute/" + getId());
        }
        setClassName(action.getClass().getSimpleName());
        setType(switch (action) {
            case CrudRemoteAction crudAction -> "CrudAction";
            case ClassAction classAction -> "ClassAction";
            default -> "Action";
        });
        var actionRenderer = action.getRenderer();
        if (actionRenderer instanceof HeadlessActionRenderer headless) {
            this.control = headless.getControl();
            this.renderer = CONTROL_RENDERER;
        } else {
            this.renderer = actionRenderer != null ? actionRenderer.getClass().getName() : null;
        }
        this.group = action.getGroup() != null ? action.getGroup().getName() : null;

        if (action instanceof CrudAction crudAction) {
            this.applicableStates = Streams.mapAndCollect(crudAction.getApplicableStates(), Enum::name);
            this.menuSupported = crudAction.isMenuSupported();
        } else if (action instanceof CrudRemoteAction crudRemoteAction) {
            this.applicableStates = Streams.mapAndCollect(crudRemoteAction.getApplicableStates(), Enum::name);
            this.menuSupported = crudRemoteAction.isMenuSupported();
        }

        if (action instanceof ClassAction classAction) {
            // Simple name, not FQCN: must match EntityMetadata#getId() so SDK dispatch
            // (ActionsApi.resolveEntityClassName -> /entities/{id}/action/{action}) keeps working.
            this.applicableClasses = Streams.mapAndCollect(classAction.getApplicableClasses(), a -> a.targetClass() == null ? "all" : a.targetClass().getSimpleName());
        }

        this.action = action instanceof RemoteAction remote ? remote : null;
    }

    /**
     * Returns the logical group name of this action.
     *
     * @return the group name
     */
    /**
     * @return where the action runs, one of {@code tools.dynamia.actions.ActionRuntime}
     */
    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    /**
     * @return whether a front end lists this action in the context menu of a record; only meaningful for crud actions
     */
    public boolean isMenuSupported() {
        return menuSupported;
    }

    public void setMenuSupported(boolean menuSupported) {
        this.menuSupported = menuSupported;
    }

    /**
     * @return where the action goes among the others of its list, lower first; the server publishes the actions
     * already sorted by it, and a front end that merges actions of its own sorts by it again
     */
    public double getPosition() {
        return position;
    }

    public void setPosition(double position) {
        this.position = position;
    }

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
     * @return the control a front end draws for this action, or null when it is not a control
     */
    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    /**
     * Returns the fully qualified class name of the renderer for this action, or {@link #CONTROL_RENDERER} when the action is
     * a {@link Control}.
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
    public RemoteAction getAction() {
        return action;
    }

    /**
     * @return whether the server runs this action when asked: only {@code HEADLESS}, {@code FLOW} and {@code REMOTE}
     * actions are executable. A {@code FRONTEND} action is in the catalog but each front end implements it.
     */
    @JsonIgnore
    public boolean isExecutable() {
        return action != null && (ActionRuntime.HEADLESS.name().equals(runtime) || ActionRuntime.FLOW.name().equals(runtime)
                || ActionRuntime.REMOTE.name().equals(runtime));
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
