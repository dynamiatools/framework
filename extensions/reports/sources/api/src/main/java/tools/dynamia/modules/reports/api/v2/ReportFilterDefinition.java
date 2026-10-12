package tools.dynamia.modules.reports.api.v2;

import java.util.List;

/**
 * A filter a report accepts.
 *
 * @param name          key to send the value with
 * @param label         label to show
 * @param dataType      TEXT, NUMBER, CURRENCY, DATE, DATE_TIME, TIME, BOOLEAN, ENUM or ENTITY
 * @param required      true if a value is needed to run the report
 * @param hideLabel     true if the label should not be shown
 * @param defaultValue  optional default value
 * @param order         position in the form
 * @param optionsSource where the options come from: {@code NONE}, {@code STATIC}, {@code ENUM}, {@code ENTITY} or
 *                      {@code QUERY}. Anything other than NONE can be loaded with the options endpoint.
 * @param format        date/time pattern for DATE, DATE_TIME and TIME filters
 */
public record ReportFilterDefinition(String name, String label, String dataType, boolean required, boolean hideLabel,
                                     String defaultValue, int order, String optionsSource, String format) {

    public static final String NONE = "NONE";
    public static final String STATIC = "STATIC";
    public static final String ENUM = "ENUM";
    public static final String ENTITY = "ENTITY";
    public static final String QUERY = "QUERY";
}
