package tools.dynamia.modules.reports.api.v2;

import java.util.List;

/**
 * Everything a UI needs to show a report: filters, columns and charts.
 *
 * @param report     summary of the report
 * @param autofields true if the columns are taken from the query, so {@code columns} is empty until the report runs
 * @param columns    declared columns
 * @param filters    filters, ordered
 * @param charts     charts
 * @param exportFormats formats accepted by the export endpoint
 */
public record ReportDefinition(ReportSummary report, boolean autofields, List<ReportColumn> columns,
                               List<ReportFilterDefinition> filters, List<ReportChartDefinition> charts,
                               List<String> exportFormats) {
}
