package tools.dynamia.modules.reports.api.v2;

import java.util.Map;

/**
 * Query being designed, to preview its first rows.
 *
 * @param queryLang    {@code sql} or {@code jpql}
 * @param queryScript  the read-only query
 * @param dataSourceId optional id of an external datasource; empty uses the application database
 * @param parameters   values for the {@code :name} parameters of the query
 * @param queryTimeout optional timeout in seconds, the same as {@code Report.queryTimeout}; empty uses the default
 */
public record ReportPreviewRequest(String queryLang, String queryScript, Long dataSourceId, Map<String, Object> parameters,
                                   Long queryTimeout) {

    /**
     * A preview that uses the default query timeout.
     */
    public ReportPreviewRequest(String queryLang, String queryScript, Long dataSourceId, Map<String, Object> parameters) {
        this(queryLang, queryScript, dataSourceId, parameters, null);
    }
}
