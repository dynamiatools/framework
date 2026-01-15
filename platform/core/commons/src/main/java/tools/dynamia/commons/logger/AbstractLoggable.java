package tools.dynamia.commons.logger;

/**
 * Simple abstract class to log messages. Intended to be implemented by classes that want to log messages.<br>
 * <br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * public class SomeClass extends AbstractLoggable {
 *     public void doSomething() {
 *         log("Starting to do something");
 *     }
 * }
 * </code>
 *
 * Use @{@link LoggingService} to get a logging service.<br>
 */
public abstract class AbstractLoggable {

    private final LoggingService logger = LoggingService.get(getClass());

    protected void log(String message) {
        logger.info(message);
    }

    protected void log(String message, Throwable throwable) {
        logger.info(message, throwable);
    }

    protected void logWarn(String message) {
        logger.warn(message);
    }

    protected LoggingService getLogger() {
        return logger;
    }
}
