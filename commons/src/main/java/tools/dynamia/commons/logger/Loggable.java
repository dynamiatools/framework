package tools.dynamia.commons.logger;

/**
 * Simple interface to log messages. Intended to be implemented by classes that want to log messages.<br>
 * <br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * public class SomeClass implements Loggable {
 *     public void doSomething() {
 *         log("Starting to do something");
 *     }
 * }
 * </code>
 *
 * Use @{@link LoggingService} to get a logging service.<br>
 */
public interface Loggable {


    /**
     * Log a message at the INFO level.
     *
     * @param message the message
     */
    default void log(String message) {
        LoggingService.get(getClass()).info(message);
    }

    /**
     * Log a message at the ERROR level, with the given throwable.
     * @param message the message
     * @param throwable the throwable
     */
    default void log(String message, Throwable throwable) {
        LoggingService.get(getClass()).error(message, throwable);
    }

    /**
     * Log a message at the DEBUG level.
     *
     * @param message the message
     */
    default void debug(String message) {
        LoggingService.get(getClass()).debug(message);
    }

    /**
     * Log a message at the WARN level.
     *
     * @param message the message
     */
    default void logWarn(String message) {
        LoggingService.get(getClass()).warn(message);
    }
}
