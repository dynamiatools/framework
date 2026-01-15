package tools.dynamia.app.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Container for metadata of application entities.
 * <p>
 * This class holds a list of {@link EntityMetadata} objects, representing the entities available in the application.
 * It provides methods to retrieve and manage these entity metadata objects, including searching by class name.
 * <p>
 * Used for API responses and UI clients that need to display or interact with available entities.
 *
 * @author Mario A. Serrano Leones
 * @since 2023
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationMetadataEntities {
    /**
     * List of entity metadata objects representing available entities.
     */
    private List<EntityMetadata> entities;

    /**
     * Default constructor for serialization and manual instantiation.
     */
    public ApplicationMetadataEntities() {
    }

    /**
     * Constructs an {@code ApplicationMetadataEntities} with the given list of entities.
     *
     * @param entities the list of {@link EntityMetadata} objects
     */
    public ApplicationMetadataEntities(List<EntityMetadata> entities) {
        this.entities = entities;
    }

    /**
     * Returns the list of entity metadata objects.
     *
     * @return the list of {@link EntityMetadata}
     */
    public List<EntityMetadata> getEntities() {
        return entities;
    }

    /**
     * Sets the list of entity metadata objects.
     *
     * @param entities the list of {@link EntityMetadata} to set
     */
    public void setEntities(List<EntityMetadata> entities) {
        this.entities = entities;
    }

    /**
     * Returns the {@link EntityMetadata} for the specified class name, or {@code null} if not found.
     *
     * @param className the class name of the entity to search for
     * @return the {@link EntityMetadata} if found, otherwise {@code null}
     */
    public EntityMetadata getEntityMetadata(String className) {
        if (entities == null) {
            return null;
        }
        return entities.stream().filter(e -> e.getClassName().equals(className)).findFirst().orElse(null);
    }
}
