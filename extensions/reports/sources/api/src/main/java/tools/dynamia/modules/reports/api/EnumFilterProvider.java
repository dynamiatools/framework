package tools.dynamia.modules.reports.api;

/**
 * API to provide new Enum filters
 *
 * @param <T>
 */
public interface EnumFilterProvider<T extends Enum> {
    /**
     * Fully qualified enum class name
     *
     * @return
     */
    String getEnumClassName();

    T[] getValues();

    String getName();
}
