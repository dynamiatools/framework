package tools.dynamia.integration;

import java.util.Map;

/**
 * An interface for providing context objects stored in a thread-local manner. This providers
 * are used to supply context-specific data that can vary between different threads of execution
 * in {@link tools.dynamia.integration.scheduling.SchedulerUtil} and {@link ThreadLocalObjectContainer}.
 *
 */
public interface ThreadLocalContextProvider {

    Map<String, Object> getContextObjects();
}
