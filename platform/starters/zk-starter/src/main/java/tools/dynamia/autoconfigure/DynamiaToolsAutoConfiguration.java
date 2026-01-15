package tools.dynamia.autoconfigure;

import org.springframework.context.annotation.Configuration;
import tools.dynamia.app.EnableDynamiaTools;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;

/**
 * Auto-configuration class for DynamiaTools framework.
 * <p>
 * This class enables DynamiaTools features and provides initial logging during application startup.
 * It is annotated with {@link Configuration} and {@link EnableDynamiaTools} to integrate with Spring Boot's auto-configuration mechanism.
 * </p>
 * <p>
 * Upon instantiation, it logs a message indicating the start of DynamiaTools auto-configuration.
 * </p>
 */
@Configuration
@EnableDynamiaTools
public class DynamiaToolsAutoConfiguration {

    /**
     * Logger instance using SLF4J for this auto-configuration class.
     */
    private LoggingService logger = new SLF4JLoggingService(DynamiaToolsAutoConfiguration.class);

    /**
     * Constructs a new {@code DynamiaToolsAutoConfiguration} and logs the startup message.
     * <p>
     * This constructor is called by the Spring container during application context initialization.
     * </p>
     */
    public DynamiaToolsAutoConfiguration() {
        logger.info("Starting DynamiaTools auto configuration");
    }

}
