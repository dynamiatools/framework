package tools.dynamia.modules.reports.api.v2;

/**
 * A chart of a report. Its data is returned with the run result.
 *
 * @param index      position, matches {@link ReportChartResult#index()}
 * @param title      chart title
 * @param type       Chart.js type: bar, line, pie, doughnut...
 * @param labelField column used for the labels
 * @param valueField column used for the values
 * @param grouped    true if rows with the same label are added up
 */
public record ReportChartDefinition(int index, String title, String type, String labelField, String valueField,
                                    boolean grouped) {
}
