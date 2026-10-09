package tools.dynamia.modules.reports.api.v2;

import java.util.Map;

/**
 * Request to run or export a report.
 *
 * @param filters   filter values by filter name. Text, number, boolean, date ({@code yyyy-MM-dd}), date time
 *                  ({@code yyyy-MM-dd HH:mm:ss}), time ({@code HH:mm:ss}), enum name or entity id
 * @param page      zero based page, 0 by default
 * @param size      rows per page. Empty or 0 returns every row.
 * @param sort      column to sort by
 * @param direction {@code asc} (default) or {@code desc}
 */
public record ReportRunRequest(Map<String, Object> filters, Integer page, Integer size, String sort, String direction) {

    public ReportRunRequest {
        filters = filters == null ? Map.of() : filters;
    }

    public static ReportRunRequest ofFilters(Map<String, Object> filters) {
        return new ReportRunRequest(filters, null, null, null, null);
    }
}
