package tools.dynamia.modules.reports.core.api;

import tools.dynamia.modules.reports.api.v2.ReportChartDefinition;
import tools.dynamia.modules.reports.api.v2.ReportChartResult;
import tools.dynamia.modules.reports.core.ReportChartPalette;
import tools.dynamia.modules.reports.core.ReportData;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.ReportChart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the chart definitions and chart data of a report. Data follows the Chart.js structure
 * ({@code labels} plus {@code datasets}).
 */
public final class ReportCharts {

    private ReportCharts() {
    }

    public static List<ReportChartDefinition> definitions(Report report) {
        List<ReportChart> charts = sorted(report);
        List<ReportChartDefinition> definitions = new ArrayList<>();
        for (int i = 0; i < charts.size(); i++) {
            ReportChart c = charts.get(i);
            definitions.add(new ReportChartDefinition(i, c.getTitle(), type(c), c.getLabelField(), c.getValueField(), c.isGrouped()));
        }
        return definitions;
    }

    public static List<ReportChartResult> build(Report report, ReportData data) {
        List<ReportChartResult> results = new ArrayList<>();
        List<ReportChart> charts = sorted(report);
        for (int i = 0; i < charts.size(); i++) {
            ReportChart chart = charts.get(i);
            Map<String, Number> points = new LinkedHashMap<>();
            int unnamed = 0;
            for (var entry : data.getEntries()) {
                Object rawLabel = value(entry.getValues(), chart.getLabelField());
                String label = rawLabel == null ? "" : rawLabel.toString();
                Number value = number(value(entry.getValues(), chart.getValueField()));
                if (chart.isGrouped()) {
                    points.merge(label, value, (a, b) -> new BigDecimal(a.toString()).add(new BigDecimal(b.toString())));
                } else {
                    // keep repeated labels as separate points
                    points.put(points.containsKey(label) ? label + " #" + (++unnamed) : label, value);
                }
            }
            List<String> labels = new ArrayList<>(points.keySet());
            List<Number> values = new ArrayList<>(points.values());
            List<String> colors = new ArrayList<>();
            for (int c = 0; c < labels.size(); c++) {
                colors.add(ReportChartPalette.color(c));
            }
            results.add(new ReportChartResult(i, chart.getTitle(), type(chart), labels,
                    List.of(new ReportChartResult.Dataset(chart.getTitle(), values, colors))));
        }
        return results;
    }

    private static List<ReportChart> sorted(Report report) {
        if (!report.isChartable() || report.getCharts() == null) {
            return List.of();
        }
        return report.getCharts().stream().sorted(Comparator.comparingInt(ReportChart::getOrder)).toList();
    }

    private static String type(ReportChart chart) {
        return chart.getType() == null || chart.getType().isBlank() ? "bar" : chart.getType();
    }

    /**
     * Column lookup that tolerates databases returning upper or lower case names.
     */
    private static Object value(Map<String, Object> values, String field) {
        if (field == null) {
            return null;
        }
        if (values.containsKey(field)) {
            return values.get(field);
        }
        for (var entry : values.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(field)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static Number number(Object value) {
        if (value instanceof Number n) {
            return n;
        }
        if (value == null) {
            return 0;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
