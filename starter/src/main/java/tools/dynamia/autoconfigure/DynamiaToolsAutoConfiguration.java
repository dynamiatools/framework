package tools.dynamia.autoconfigure;

import org.springframework.context.annotation.Configuration;
import tools.dynamia.app.EnableDynamiaTools;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;

@Configuration
@EnableDynamiaTools
public class DynamiaToolsAutoConfiguration {

    private LoggingService logger = new SLF4JLoggingService(DynamiaToolsAutoConfiguration.class);

    public DynamiaToolsAutoConfiguration() {
        logger.info("Starting DynamiaTools auto configuration");
    }

}
