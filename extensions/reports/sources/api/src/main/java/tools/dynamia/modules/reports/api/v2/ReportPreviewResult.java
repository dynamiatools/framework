package tools.dynamia.modules.reports.api.v2;

import java.util.List;
import java.util.Map;

/**
 * First rows of a query preview.
 *
 * @param columns    column names
 * @param rows       the rows, at most the preview limit
 * @param truncated  true if there are more rows than the preview shows
 * @param durationMs execution time in milliseconds
 */
public record ReportPreviewResult(List<String> columns, List<Map<String, Object>> rows, boolean truncated, long durationMs) {
}
