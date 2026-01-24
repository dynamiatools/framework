package tools.dynamia.app;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for Dynamia Tools application settings.
 * <p>
 * This class binds to properties prefixed with {@code dynamia.app} in the application configuration files
 * (application.properties or application.yml). It provides centralized configuration for core application
 * metadata, UI customization, and API settings.
 * </p>
 *
 * <p>Example configuration in application.yml:</p>
 * <pre>{@code
 * dynamia:
 *   app:
 *     name: My Application
 *     short-name: MyApp
 *     version: 1.0.0
 *     default-skin: Green
 *     default-logo: /static/logo.png
 *     api-base-path: /api/v1
 *     web-cache-enabled: true
 * }</pre>
 *
 * <p>Example configuration in application.properties:</p>
 * <pre>{@code
 * dynamia.app.name=My Application
 * dynamia.app.short-name=MyApp
 * dynamia.app.version=1.0.0
 * dynamia.app.default-skin=Green
 * dynamia.app.default-logo=/static/logo.png
 * dynamia.app.default-icon=/static/icon.png
 * dynamia.app.api-base-path=/api/v1
 * dynamia.app.web-cache-enabled=true
 * dynamia.app.description=My application description
 * dynamia.app.company=My Company
 * dynamia.app.url=https://www.example.com
 * dynamia.app.author=John Doe
 * dynamia.app.license=MIT
 * }</pre>
 *
 * @see ConfigurationProperties
 * @since 1.0
 */
@ConfigurationProperties(prefix = "dynamia.app")
public class ApplicationConfigurationProperties {

    /**
     * Application name displayed in the UI.
     */
    private String name;

    /**
     * Template name for the application UI layout.
     */
    private String template;

    /**
     * Default skin/theme for the application UI.
     */
    private String defaultSkin;

    /**
     * Path to the default logo image resource.
     */
    private String defaultLogo;

    /**
     * Path to the default icon image resource (favicon).
     */
    private String defaultIcon;

    /**
     * Base package name for component scanning and auto-configuration.
     */
    private String basePackage;

    /**
     * Application version number.
     */
    private String version;

    /**
     * Build number or build identifier.
     */
    private String build;

    /**
     * Short name or abbreviation of the application.
     */
    private String shortName;

    /**
     * Company or organization name owning the application.
     */
    private String company;

    /**
     * Application's public URL or homepage.
     */
    private String url;

    /**
     * JNDI datasource name for database configuration.
     */
    private String jdni;

    /**
     * Author name(s) of the application.
     */
    private String author;

    /**
     * License type or license information.
     */
    private String license;

    /**
     * Flag to enable or disable web caching mechanisms.
     */
    private boolean webCacheEnabled;

    /**
     * Application description text.
     */
    private String description;

    /**
     * Base path for REST API endpoints. Defaults to "/api".
     */
    private String apiBasePath = "/api";

    /**
     * Additional custom properties map for application-specific configuration.
     */
    private Map<String, String> properties;

    /**
     * Gets the application name.
     *
     * @return the application name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the application name.
     *
     * @param name the application name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the template name.
     *
     * @return the template name
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Sets the template name.
     *
     * @param template the template name to set
     */
    public void setTemplate(String template) {
        this.template = template;
    }

    /**
     * Gets the default skin.
     *
     * @return the default skin
     */
    public String getDefaultSkin() {
        return defaultSkin;
    }

    /**
     * Sets the default skin.
     *
     * @param defaultSkin the default skin to set
     */
    public void setDefaultSkin(String defaultSkin) {
        this.defaultSkin = defaultSkin;
    }

    /**
     * Gets the default logo path.
     *
     * @return the default logo path
     */
    public String getDefaultLogo() {
        return defaultLogo;
    }

    /**
     * Sets the default logo path.
     *
     * @param defaultLogo the default logo path to set
     */
    public void setDefaultLogo(String defaultLogo) {
        this.defaultLogo = defaultLogo;
    }

    /**
     * Gets the default icon path.
     *
     * @return the default icon path
     */
    public String getDefaultIcon() {
        return defaultIcon;
    }

    /**
     * Sets the default icon path.
     *
     * @param defaultIcon the default icon path to set
     */
    public void setDefaultIcon(String defaultIcon) {
        this.defaultIcon = defaultIcon;
    }

    /**
     * Gets the base package name.
     *
     * @return the base package name
     */
    public String getBasePackage() {
        return basePackage;
    }

    /**
     * Sets the base package name.
     *
     * @param basePackage the base package name to set
     */
    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    /**
     * Gets the application version.
     *
     * @return the application version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the application version.
     *
     * @param version the application version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the build number.
     *
     * @return the build number
     */
    public String getBuild() {
        return build;
    }

    /**
     * Sets the build number.
     *
     * @param build the build number to set
     */
    public void setBuild(String build) {
        this.build = build;
    }

    /**
     * Gets the application short name.
     *
     * @return the application short name
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Sets the application short name.
     *
     * @param shortName the application short name to set
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * Gets the company name.
     *
     * @return the company name
     */
    public String getCompany() {
        return company;
    }

    /**
     * Sets the company name.
     *
     * @param company the company name to set
     */
    public void setCompany(String company) {
        this.company = company;
    }

    /**
     * Gets the application URL.
     *
     * @return the application URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the application URL.
     *
     * @param url the application URL to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Gets the JNDI datasource name.
     *
     * @return the JNDI datasource name
     */
    public String getJdni() {
        return jdni;
    }

    /**
     * Sets the JNDI datasource name.
     *
     * @param jdni the JNDI datasource name to set
     */
    public void setJdni(String jdni) {
        this.jdni = jdni;
    }

    /**
     * Gets the author name(s).
     *
     * @return the author name(s)
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author name(s).
     *
     * @param author the author name(s) to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Gets the license information.
     *
     * @return the license information
     */
    public String getLicense() {
        return license;
    }

    /**
     * Sets the license information.
     *
     * @param license the license information to set
     */
    public void setLicense(String license) {
        this.license = license;
    }

    /**
     * Checks if web cache is enabled.
     *
     * @return true if web cache is enabled, false otherwise
     */
    public boolean isWebCacheEnabled() {
        return webCacheEnabled;
    }

    /**
     * Sets the web cache enabled flag.
     *
     * @param webCacheEnabled true to enable web cache, false to disable
     */
    public void setWebCacheEnabled(boolean webCacheEnabled) {
        this.webCacheEnabled = webCacheEnabled;
    }

    /**
     * Gets the application description.
     *
     * @return the application description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the application description.
     *
     * @param description the application description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the custom properties map.
     * Returns a new empty map if properties have not been initialized.
     *
     * @return the custom properties map, never null
     */
    public Map<String, String> getProperties() {
        if (properties == null) {
            properties = new HashMap<>();
        }
        return properties;
    }

    /**
     * Sets the custom properties map.
     *
     * @param properties the custom properties map to set
     */
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     * Gets the API base path.
     *
     * @return the API base path
     */
    public String getApiBasePath() {
        return apiBasePath;
    }

    /**
     * Sets the API base path.
     *
     * @param apiBasePath the API base path to set
     */
    public void setApiBasePath(String apiBasePath) {
        this.apiBasePath = apiBasePath;
    }
}
