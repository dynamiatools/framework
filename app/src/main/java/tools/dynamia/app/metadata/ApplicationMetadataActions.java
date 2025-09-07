package tools.dynamia.app.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.dynamia.actions.Action;

import java.util.List;

/**
 * Container for metadata of application actions.
 * <p>
 * This class holds a list of {@link ActionMetadata} objects, representing the actions available in the application.
 * It provides methods to retrieve and manage these action metadata objects, including searching by action ID.
 * <p>
 * Used for API responses and UI clients that need to display or interact with available actions.
 *
 * @author Mario A. Serrano Leones
 * @since 2023
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationMetadataActions {

    /**
     * List of action metadata objects representing available actions.
     */
    private List<ActionMetadata> actions;

    /**
     * Default constructor for serialization and manual instantiation.
     */
    public ApplicationMetadataActions() {
    }

    /**
     * Constructs an {@code ApplicationMetadataActions} with the given list of actions.
     *
     * @param actions the list of {@link ActionMetadata} objects
     */
    public ApplicationMetadataActions(List<ActionMetadata> actions) {
        this.actions = actions;
    }

    /**
     * Returns the list of action metadata objects.
     *
     * @return the list of {@link ActionMetadata}
     */
    public List<ActionMetadata> getActions() {
        return actions;
    }

    /**
     * Sets the list of action metadata objects.
     *
     * @param actions the list of {@link ActionMetadata} to set
     */
    public void setActions(List<ActionMetadata> actions) {
        this.actions = actions;
    }

    /**
     * Returns the {@link ActionMetadata} for the specified action ID, or {@code null} if not found.
     *
     * @param actionId the ID of the action to search for
     * @return the {@link ActionMetadata} if found, otherwise {@code null}
     */
    public ActionMetadata getAction(String actionId) {
        return actions != null ? actions.stream().filter(a -> a.getId().equals(actionId)).findFirst().orElse(null) : null;
    }
}
