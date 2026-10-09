package tools.dynamia.modules.reports.api.v2;

/**
 * An option of a filter with predefined values.
 *
 * @param value value to send back when the option is selected
 * @param label text to show
 */
public record FilterOption(Object value, String label) {
}
