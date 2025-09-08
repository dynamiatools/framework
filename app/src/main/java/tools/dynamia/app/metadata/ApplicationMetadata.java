package tools.dynamia.app.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.dynamia.app.ApplicationInfo;

/**
 * Represents the metadata information for the application, including title, version, logo, template, and author.
 * <p>
 * This class is used to provide descriptive and configuration details about the application, typically for UI or API clients.
 * It extends {@link BasicMetadata} to inherit common metadata fields.
 *
 * @author Mario A. Serrano Leones
 * @since 2023
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationMetadata extends BasicMetadata {

    /**
     * The title of the application, used for display purposes.
     */
    private String title;
    /**
     * The version of the application.
     */
    private String version;
    /**
     * The logo image path or URL for the application.
     */
    private String logo;
    /**
     * The template name used for the application's UI.
     */
    private String template;
    /**
     * The author or organization responsible for the application.
     */
    private String author;

    /**
     * Default constructor for serialization and manual instantiation.
     */
    public ApplicationMetadata() {
    }

    /**
     * Constructs an {@code ApplicationMetadata} from the given {@link ApplicationInfo} instance.
     * <p>
     * Copies relevant properties from the application info, including name, description, icon, logo, template, version, and author.
     *
     * @param info the {@link ApplicationInfo} instance to extract metadata from
     */
    public ApplicationMetadata(ApplicationInfo info) {
        setName(info.getName());
        setTitle(info.getName());
        setDescription(info.getDescription());
        setIcon(info.getDefaultIcon());
        setLogo(info.getDefaultLogo());
        setTemplate(info.getTemplate());
        setVersion(info.getVersion());
        setAuthor(info.getAuthor());
        setEndpoint("/api/app/metadata");
    }

    /**
     * Returns the title of the application.
     * @return the application title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the application.
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the version of the application.
     * @return the application version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version of the application.
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the logo image path or URL.
     * @return the logo path or URL
     */
    public String getLogo() {
        return logo;
    }

    /**
     * Sets the logo image path or URL.
     * @param logo the logo path or URL to set
     */
    public void setLogo(String logo) {
        this.logo = logo;
    }

    /**
     * Returns the template name used for the application's UI.
     * @return the template name
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Sets the template name for the application's UI.
     * @param template the template name to set
     */
    public void setTemplate(String template) {
        this.template = template;
    }

    /**
     * Returns the author or organization responsible for the application.
     * @return the author name
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author or organization responsible for the application.
     * @param author the author name to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }
}
