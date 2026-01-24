package tools.dynamia.autoconfigure;

import org.springframework.context.annotation.Configuration;
import tools.dynamia.app.DynamiaToolsWebApplication;
import tools.dynamia.app.EnableDynamiaTools;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;

/**
 * Auto-configuration class for DynamiaTools framework.
 * <p>
 * This class enables DynamiaTools features and provides initial logging during application startup.
 * It is annotated with {@link Configuration} and {@link EnableDynamiaTools} to integrate with Spring Boot's auto-configuration mechanism.
 * This configuration is automatically detected by Spring Boot when the dynamia-tools-starter dependency is present.
 * </p>
 * <p>
 * Upon instantiation, it logs a message indicating the start of DynamiaTools auto-configuration.
 * The {@link EnableDynamiaTools} annotation triggers the import of {@link tools.dynamia.app.DynamiaBaseConfiguration},
 * which sets up all necessary beans and services for the framework.
 * </p>
 *
 * <p><b>Usage:</b> This class is automatically loaded by Spring Boot's auto-configuration mechanism.
 * No manual configuration is required. Simply include the dynamia-tools-starter dependency:
 * </p>
 * <pre>{@code
 * <dependency>
 *     <groupId>tools.dynamia</groupId>
 *     <artifactId>dynamia-tools-starter</artifactId>
 *     <version>LAST_VERSION</version>
 * </dependency>
 * }</pre>
 *
 * @see EnableDynamiaTools
 * @see tools.dynamia.app.DynamiaBaseConfiguration
 * @see Configuration
 */
@Configuration
public class DynamiaToolsAutoConfiguration extends DynamiaToolsWebApplication {

    /**
     * Logger instance using SLF4J for this auto-configuration class.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final LoggingService logger = new SLF4JLoggingService(DynamiaToolsAutoConfiguration.class);

    /**
     * Constructs a new {@code DynamiaToolsAutoConfiguration} and logs the startup message.
     * <p>
     * This constructor is called by the Spring container during application context initialization.
     * It logs an informational message to indicate that DynamiaTools auto-configuration has started.
     * </p>
     */
    public DynamiaToolsAutoConfiguration() {
        logger.info("Starting DynamiaTools auto configuration");
    }



}
