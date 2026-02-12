package tools.dynamia.modules.reports.api;

/**
 * API to provide new Entity filters
 */
public interface EntityFilterProvider {
    /**
     * Fully qualified class name
     *
     * @return
     */
    String getEntityClassName();

    String getName();
}
