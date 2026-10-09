package tools.dynamia.modules.reports.api.v2;

import java.util.List;

/**
 * A group of reports in the catalog.
 */
public record ReportCatalogGroup(String name, String endpointName, List<ReportSummary> reports) {
}
