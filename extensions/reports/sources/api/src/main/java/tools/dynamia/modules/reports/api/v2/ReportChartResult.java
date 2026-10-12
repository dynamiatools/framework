package tools.dynamia.modules.reports.api.v2;

import java.util.List;

/**
 * Data of a chart, shaped like a Chart.js configuration.
 *
 * @param index    matches {@link ReportChartDefinition#index()}
 * @param title    chart title
 * @param type     Chart.js type
 * @param labels   category labels
 * @param datasets values; one dataset per chart
 */
public record ReportChartResult(int index, String title, String type, List<String> labels, List<Dataset> datasets) {

    /**
     * @param label           dataset name
     * @param data            one value per label
     * @param backgroundColor one color per label
     */
    public record Dataset(String label, List<Number> data, List<String> backgroundColor) {
    }
}
