package tools.dynamia.modules.reports.api.v2;

import java.util.List;
import java.util.Map;

/**
 * Result of running a report.
 *
 * @param columns    ordered columns
 * @param rows       the rows of the requested page; every row has one value per column name
 * @param total      rows of the whole result, before paging
 * @param page       zero based page returned
 * @param size       rows per page, or the number of rows when everything was returned
 * @param truncated  true if the report reached the maximum rows ({@code dynamia.reports.max-rows}) and has more data
 * @param durationMs execution time in milliseconds
 * @param charts     data of the report charts, empty when the report has none
 */
public record ReportRunResult(List<ReportColumn> columns, List<Map<String, Object>> rows, long total, int page, int size,
                              boolean truncated, long durationMs, List<ReportChartResult> charts) {
}
