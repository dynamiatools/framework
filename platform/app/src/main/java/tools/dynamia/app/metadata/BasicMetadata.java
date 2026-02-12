package tools.dynamia.app.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;

/**
 * Base class for metadata objects in the application.
 * <p>
 * Provides common fields such as id, name, endpoint, description, and icon, which are shared by all metadata types.
 * This class is intended to be extended by more specific metadata classes.
 * <p>
 * Used for serialization and transport of metadata information to UI and API clients.
 *
 * @author Mario A. Serrano Leones
 * @since 2023
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BasicMetadata implements Serializable {

    /**
     * Unique identifier for the metadata object.
     */
    private String id;
    /**
     * Display name for the metadata object.
     */
    private String name;
    /**
     * API endpoint associated with the metadata object.
     */
    private String endpoint;
    /**
     * Description of the metadata object.
     */
    private String description;
    /**
     * Icon path or URL for the metadata object.
     */
    private String icon;

    /**
     * Returns the unique identifier for the metadata object.
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier for the metadata object.
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the display name for the metadata object.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name for the metadata object.
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the API endpoint associated with the metadata object.
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the API endpoint associated with the metadata object.
     * @param endpoint the endpoint to set
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Returns the description of the metadata object.
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the metadata object.
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the icon path or URL for the metadata object.
     * @return the icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Sets the icon path or URL for the metadata object.
     * @param icon the icon to set
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }
}
