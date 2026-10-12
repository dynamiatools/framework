package tools.dynamia.modules.reports.core.dashboard;

import tools.dynamia.domain.ValidationError;
import tools.dynamia.modules.dashboard.AbstractDashboardWidgetDefinition;
import tools.dynamia.modules.dashboard.ChartWidgetData;
import tools.dynamia.modules.dashboard.DashboardWidgetTypes;
import tools.dynamia.modules.dashboard.InstallDashboardWidget;
import tools.dynamia.modules.dashboard.KpiWidgetData;
import tools.dynamia.modules.dashboard.WidgetContext;
import tools.dynamia.modules.reports.api.v2.ReportChartResult;
import tools.dynamia.modules.reports.api.v2.ReportRunRequest;
import tools.dynamia.modules.reports.api.v2.ReportRunResult;
import tools.dynamia.modules.reports.api.v2.ReportSummary;
import tools.dynamia.modules.reports.core.ReportNotFoundException;
import tools.dynamia.modules.reports.core.api.ReportsApiService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Dashboard widget that shows a report, or part of it, inside a dashboard. It is a JS-frontend widget: it serves data
 * only and has no ZK view. Declare it in a dashboard view descriptor with the widget id {@value #ID}:
 * <pre>{@code
 * view: dashboard
 * id: salesDashboard
 * fields:
 *   salesByRegion:
 *     params:
 *       widget: report
 *       report: Sales by region      # report name, or its id
 *       group: Sales                 # optional, to tell apart reports with the same name
 *       display: chart               # table (default), chart or kpi
 *       chart: 0                     # chart index or title (display=chart)
 *       filters.year: 2026           # default values for the report filters
 *   totalSales:
 *     params:
 *       widget: report
 *       report: Total sales
 *       display: kpi
 *       value: TOTAL                 # column to use (default: the first one)
 *       aggregate: sum               # first (default), sum, avg, min, max or count
 *       unit: USD
 * }</pre>
 * <ul>
 *     <li>{@code display=chart} serves a {@link ChartWidgetData} and {@code display=kpi} a {@link KpiWidgetData}, so the
 *     standard {@code chart} and {@code kpi} renderers draw them. {@code display=table} serves a
 *     {@link ReportWidgetData} with the widget type {@code report}.</li>
 *     <li>Query parameters of the dashboard request override the {@code filters.*} defaults, by filter name.</li>
 *     <li>The report is run through {@link ReportsApiService}, so the account scoping, active state, access policies and
 *     row limits of the reports module apply. A user that cannot access the report gets no data.</li>
 * </ul>
 */
@InstallDashboardWidget
public class ReportDashboardWidget extends AbstractDashboardWidgetDefinition {

    /** Widget id used in dashboard descriptors. */
    public static final String ID = "report";

    /** Widget type served for {@code display=table}. */
    public static final String TYPE_TABLE = "report";

    /** Default and maximum rows of a table. */
    public static final int DEFAULT_LIMIT = 10;
    public static final int MAX_LIMIT = 100;

    private static final String FILTER_PREFIX = "filters.";

    private final ReportsApiService api;
    private final Map<String, Object> params = new LinkedHashMap<>();
    private final Map<String, Object> overrides = new LinkedHashMap<>();
    private ReportSummary report;
    private String display = "table";

    /**
     * @param api the reports API service that resolves and runs the report
     */
    public ReportDashboardWidget(ReportsApiService api) {
        this.api = api;
        setTitleVisible(true);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getType() {
        return switch (display) {
            case "chart" -> DashboardWidgetTypes.CHART;
            case "kpi" -> DashboardWidgetTypes.KPI;
            default -> TYPE_TABLE;
        };
    }

    @Override
    public void init(WidgetContext context) {
        params.clear();
        if (context.getField() != null) {
            params.putAll(context.getField().getParams());
        }
        display = text("display", "table").toLowerCase(Locale.ROOT);
        if (!List.of("table", "chart", "kpi").contains(display)) {
            throw new ValidationError("Invalid display [" + display + "]. Use table, chart or kpi");
        }
        String reportParam = text("report", null);
        if (reportParam == null) {
            throw new ValidationError("The report widget needs the param [report]");
        }
        report = api.findReport(reportParam, text("group", null))
                .orElseThrow(() -> new ReportNotFoundException("Report not found: " + reportParam));
        String title = text("title", null);
        setTitle(title != null ? title : report.title() != null && !report.title().isBlank() ? report.title() : report.name());
    }

    @Override
    public void update(Map<String, Object> newParams) {
        overrides.clear();
        if (newParams != null) {
            overrides.putAll(newParams);
        }
    }

    @Override
    public Object getData(WidgetContext context) {
        return switch (display) {
            case "chart" -> chart();
            case "kpi" -> kpi();
            default -> table();
        };
    }

    private ReportWidgetData table() {
        int limit = Math.max(1, Math.min(integer("limit", DEFAULT_LIMIT), MAX_LIMIT));
        String sort = text("sort", null);
        ReportRunResult result = api.run(report.id(),
                new ReportRunRequest(filters(), 0, limit, sort, text("direction", "asc")));
        return new ReportWidgetData(result.columns(), result.rows(), result.total(), result.truncated());
    }

    private ChartWidgetData chart() {
        ReportRunResult result = api.run(report.id(), new ReportRunRequest(filters(), 0, 1, null, null));
        if (result.charts().isEmpty()) {
            throw new ValidationError("The report [" + report.name() + "] has no charts");
        }
        ReportChartResult chart = pickChart(result.charts());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("labels", chart.labels());
        data.put("datasets", chart.datasets());
        return new ChartWidgetData(chart.type(), data);
    }

    private ReportChartResult pickChart(List<ReportChartResult> charts) {
        String selector = text("chart", "0");
        for (ReportChartResult chart : charts) {
            if (selector.equalsIgnoreCase(chart.title()) || selector.equals(String.valueOf(chart.index()))) {
                return chart;
            }
        }
        throw new ValidationError("The report [" + report.name() + "] has no chart [" + selector + "]");
    }

    private KpiWidgetData kpi() {
        ReportRunResult result = api.run(report.id(), new ReportRunRequest(filters(), null, null, null, null));
        String column = text("value", result.columns().isEmpty() ? null : result.columns().get(0).name());
        String label = text("label", report.title() != null && !report.title().isBlank() ? report.title() : report.name());
        Object value = aggregate(text("aggregate", "first").toLowerCase(Locale.ROOT), column, result.rows());
        return new KpiWidgetData(value, label, text("unit", null), null);
    }

    private Object aggregate(String function, String column, List<Map<String, Object>> rows) {
        if ("count".equals(function)) {
            return rows.size();
        }
        List<Object> values = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Object value = column == null ? null : row.get(column);
            if (value != null) {
                values.add(value);
            }
        }
        if (values.isEmpty()) {
            return null;
        }
        if ("first".equals(function)) {
            return values.get(0);
        }
        List<BigDecimal> numbers = new ArrayList<>();
        for (Object value : values) {
            try {
                numbers.add(new BigDecimal(value.toString()));
            } catch (NumberFormatException e) {
                throw new ValidationError("Column [" + column + "] is not numeric, it cannot be used with aggregate [" + function + "]");
            }
        }
        BigDecimal sum = numbers.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return switch (function) {
            case "sum" -> sum;
            case "avg" -> sum.divide(BigDecimal.valueOf(numbers.size()), 4, RoundingMode.HALF_UP);
            case "min" -> numbers.stream().min(BigDecimal::compareTo).orElse(null);
            case "max" -> numbers.stream().max(BigDecimal::compareTo).orElse(null);
            default -> throw new ValidationError("Invalid aggregate [" + function + "]. Use first, sum, avg, min, max or count");
        };
    }

    /**
     * Filter values: the {@code filters.*} defaults, overridden by request parameters named like the filters.
     */
    private Map<String, Object> filters() {
        Map<String, Object> filters = new LinkedHashMap<>();
        params.forEach((key, value) -> {
            if (key.startsWith(FILTER_PREFIX) && value != null) {
                filters.put(key.substring(FILTER_PREFIX.length()), value);
            }
        });
        overrides.forEach((key, value) -> {
            if (value != null) {
                filters.put(key.startsWith(FILTER_PREFIX) ? key.substring(FILTER_PREFIX.length()) : key, value);
            }
        });
        return filters;
    }

    private String text(String name, String defaultValue) {
        Object value = params.get(name);
        return value == null || value.toString().isBlank() ? defaultValue : value.toString().trim();
    }

    private int integer(String name, int defaultValue) {
        String value = text(name, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ValidationError("Param [" + name + "] must be a number");
        }
    }
}
