package tools.dynamia.modules.reports.api.v2;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A column of a report result.
 *
 * @param name      key of the value in every row
 * @param label     header to show
 * @param dataType  TEXT, NUMBER, CURRENCY, DATE, DATE_TIME, TIME, BOOLEAN, ENUM or ENTITY
 * @param align     LEFT, CENTER or RIGHT
 * @param format    optional display format pattern
 * @param width     optional width, for example {@code 120px}
 * @param upperCase true if the text should be shown in upper case
 * @param cellStyle optional CSS class name(s) the designer chose for every cell of the column (for example {@code green lighten-5});
 *                  omitted from the JSON when not set
 * @param columnStyle optional CSS class name(s) the designer chose for the header of the column; omitted from the JSON when not set
 */
public record ReportColumn(String name, String label, String dataType, String align, String format, String width,
                           boolean upperCase,
                           @JsonInclude(JsonInclude.Include.NON_NULL) String cellStyle,
                           @JsonInclude(JsonInclude.Include.NON_NULL) String columnStyle) {

    /**
     * A column without designer styles.
     */
    public ReportColumn(String name, String label, String dataType, String align, String format, String width, boolean upperCase) {
        this(name, label, dataType, align, format, width, upperCase, null, null);
    }
}
