package tools.dynamia.modules.reports.core.dashboard;

import tools.dynamia.modules.reports.api.v2.ReportColumn;

import java.util.List;
import java.util.Map;

/**
 * Data of a {@link ReportDashboardWidget} shown as a table. The widget type is {@code report}.
 *
 * @param columns   the columns of the report
 * @param rows      the first rows of the result
 * @param total     the rows of the whole result
 * @param truncated true if the report reached the maximum rows and has more data
 */
public record ReportWidgetData(List<ReportColumn> columns, List<Map<String, Object>> rows, long total, boolean truncated) {
}
