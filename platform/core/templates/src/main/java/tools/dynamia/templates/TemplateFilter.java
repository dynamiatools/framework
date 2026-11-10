package tools.dynamia.templates;

import java.util.Map;

/**
 * A filter of the template expressions: the {@code name} in {@code ${value|name}} or {@code {{value|name:argument}}}.
 * It receives the value the previous step produced and returns what the next step (or the print) gets.
 * <p>
 * Register one with {@link TemplateFilters#register} or list its class in
 * {@code META-INF/services/tools.dynamia.templates.TemplateFilter} to have it found on its own.
 * Implementations must be stateless and thread-safe, and must not run anything the template author controls.
 */
public interface TemplateFilter {

    /** The name used in templates: a letter followed by letters, digits or underscores. */
    String name();

    /**
     * @param value     what comes into the filter; null when the variable is missing or empty
     * @param argument  the text after the first {@code :} (not split, may hold other colons), or null when there is none
     * @param variables the data of the template, e.g. {@code locale}
     * @return the new value; null prints as nothing. Returning {@code value} unchanged is the way to say "I cannot
     * do anything with this".
     */
    Object apply(Object value, String argument, Map<String, Object> variables);
}
