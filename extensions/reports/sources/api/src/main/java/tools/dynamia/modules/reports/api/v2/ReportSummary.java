package tools.dynamia.modules.reports.api.v2;

/**
 * A report in the catalog.
 *
 * @param id          report id, used in every other endpoint
 * @param name        internal name
 * @param title       title shown to users, may be null (use the name)
 * @param subtitle    optional subtitle
 * @param description description of the report
 * @param group       group name
 * @param chartable   true if the report has charts
 * @param hasFilters  true if the report accepts filters
 * @param endpoint    legacy export endpoint ({@code /api/reports/{group}/{endpoint}}) or empty when not exported
 */
public record ReportSummary(Long id, String name, String title, String subtitle, String description, String group,
                            boolean chartable, boolean hasFilters, String endpoint) {
}
