package tools.dynamia.modules.reports.api.v2;

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
 */
public record ReportColumn(String name, String label, String dataType, String align, String format, String width,
                           boolean upperCase) {
}
