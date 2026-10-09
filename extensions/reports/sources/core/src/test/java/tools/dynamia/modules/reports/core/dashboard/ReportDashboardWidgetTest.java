package tools.dynamia.modules.reports.core.dashboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Scope;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.modules.dashboard.ChartWidgetData;
import tools.dynamia.modules.dashboard.DashboardWidgetTypes;
import tools.dynamia.modules.dashboard.InstallDashboardWidget;
import tools.dynamia.modules.dashboard.KpiWidgetData;
import tools.dynamia.modules.dashboard.WidgetContext;
import tools.dynamia.modules.reports.core.ReportNotFoundException;
import tools.dynamia.modules.reports.core.api.ApiFixtures;
import tools.dynamia.modules.reports.core.api.ReportsApiService;
import tools.dynamia.modules.reports.core.domain.ReportFilter;
import tools.dynamia.modules.reports.core.domain.enums.DataType;
import tools.dynamia.modules.reports.core.security.ReportAccessDeniedException;
import tools.dynamia.viewers.Field;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReportDashboardWidgetTest {

    private ApiFixtures.InMemoryReports reports;
    private ReportDashboardWidget widget;

    @BeforeEach
    void setUp() {
        reports = new ApiFixtures.InMemoryReports();
        var sales = reports.add(1L, "Sales", "Sales list", "select id, region, total from sales order by id");
        var filter = new ReportFilter("min");
        filter.setReport(sales);
        filter.setLabel("Minimum");
        filter.setCondition("total >= :min");
        filter.setDataType(DataType.NUMBER);
        sales.getFilters().add(filter);
        reports.chart(sales, "Totals", "pie", "region", "total", true);
        reports.chart(sales, "Points", "line", "region", "total", false);
        reports.add(2L, "Finance", "Sales list", "select 1 as one from sales");
        sales.setTitle("Sales");
        widget = new ReportDashboardWidget(new ReportsApiService(reports));
    }

    private WidgetContext context(Object... keyValues) {
        var field = new Field("slot");
        for (int i = 0; i < keyValues.length; i += 2) {
            field.addParam((String) keyValues[i], keyValues[i + 1]);
        }
        return new WidgetContext(null, field);
    }

    private Object data(WidgetContext context) {
        widget.init(context);
        return widget.getData(context);
    }

    @Test
    void isAPrototypeDashboardWidgetWithTheReportId() {
        assertNotNull(ReportDashboardWidget.class.getAnnotation(InstallDashboardWidget.class));
        assertEquals("report", widget.getId());
        assertEquals("prototype", InstallDashboardWidget.class.getAnnotation(Scope.class).value());
    }

    @Test
    void tableIsTheDefaultDisplayAndLimitsTheRows() {
        var context = context("report", "Sales list", "group", "Sales", "limit", "2");
        var data = (ReportWidgetData) data(context);

        assertEquals("report", widget.getType());
        assertEquals("Sales", widget.getTitle());
        assertTrue(widget.isTitleVisible());
        assertEquals(2, data.rows().size());
        assertEquals(4, data.total());
        assertEquals(3, data.columns().size());
        assertFalse(data.truncated());
    }

    @Test
    void tableCanBeSorted() {
        var data = (ReportWidgetData) data(context("report", "Sales list", "group", "Sales", "sort", "TOTAL", "direction", "desc", "limit", 1));
        assertEquals(3, data.rows().get(0).get("ID"));
    }

    @Test
    void reportCanBeFoundByIdAndGroupDisambiguates() {
        var byId = (ReportWidgetData) data(context("report", "2"));
        assertEquals(1, byId.columns().size());

        var other = new ReportDashboardWidget(new ReportsApiService(reports));
        var ctx = context("report", "sales LIST", "group", "finance");
        other.init(ctx);
        assertEquals(1, ((ReportWidgetData) other.getData(ctx)).columns().size());
    }

    @Test
    void chartServesChartJsDataFromTheSelectedChart() {
        var context = context("report", "Sales list", "group", "Sales", "display", "chart");
        var chart = (ChartWidgetData) data(context);

        assertEquals(DashboardWidgetTypes.CHART, widget.getType());
        assertEquals("pie", chart.type());
        @SuppressWarnings("unchecked") var map = (Map<String, Object>) chart.data();
        assertEquals(List.of("north", "south", "east"), map.get("labels"));
        assertNotNull(map.get("datasets"));

        var second = (ChartWidgetData) data(context("report", "Sales list", "group", "Sales", "display", "chart", "chart", "points"));
        assertEquals("line", second.type());
    }

    @Test
    void chartErrors() {
        assertThrows(ValidationError.class, () -> data(context("report", "2", "display", "chart")), "report without charts");
        assertThrows(ValidationError.class, () -> data(context("report", "Sales list", "group", "Sales", "display", "chart", "chart", "nope")));
    }

    @Test
    void kpiAggregatesAColumn() {
        var first = (KpiWidgetData) data(context("report", "Sales list", "group", "Sales", "display", "kpi", "value", "TOTAL", "unit", "USD", "label", "Revenue"));
        assertEquals(DashboardWidgetTypes.KPI, widget.getType());
        assertEquals("Revenue", first.label());
        assertEquals("USD", first.unit());
        assertEquals(new BigDecimal("100.50"), first.value());

        assertEquals(new BigDecimal("650.75"), ((KpiWidgetData) data(context("report", "Sales list", "group", "Sales", "display", "kpi", "value", "TOTAL", "aggregate", "sum"))).value());
        assertEquals(4, ((KpiWidgetData) data(context("report", "Sales list", "group", "Sales", "display", "kpi", "aggregate", "count"))).value());
        assertEquals(new BigDecimal("50.00"), ((KpiWidgetData) data(context("report", "Sales list", "group", "Sales", "display", "kpi", "value", "TOTAL", "aggregate", "min"))).value());
        assertEquals(new BigDecimal("162.6875"), ((KpiWidgetData) data(context("report", "Sales list", "group", "Sales", "display", "kpi", "value", "TOTAL", "aggregate", "avg"))).value());
    }

    @Test
    void kpiRejectsNonNumericAggregates() {
        assertThrows(ValidationError.class, () -> data(context("report", "Sales list", "group", "Sales", "display", "kpi", "value", "REGION", "aggregate", "sum")));
    }

    @Test
    void filterDefaultsAndRequestParamsAreApplied() {
        var context = context("report", "Sales list", "group", "Sales", "filters.min", "200");
        widget.init(context);
        assertEquals(2, ((ReportWidgetData) widget.getData(context)).total());

        widget.update(new LinkedHashMap<>(Map.of("min", "300")));
        assertEquals(1, ((ReportWidgetData) widget.getData(context)).total());

        widget.update(Map.of("filters.min", "1000"));
        assertEquals(0, ((ReportWidgetData) widget.getData(context)).total());
    }

    @Test
    void configurationErrorsAreReported() {
        assertThrows(ValidationError.class, () -> widget.init(context()));
        assertThrows(ValidationError.class, () -> widget.init(context("report", "x", "display", "gauge")));
        assertThrows(ReportNotFoundException.class, () -> widget.init(context("report", "Does not exist")));
        assertThrows(ValidationError.class, () -> data(context("report", "Sales list", "group", "Sales", "limit", "many")));
    }

    @Test
    void usersWithoutAccessCannotSeeTheReport() {
        reports.reports.get(0).setAccessRoles("HR");

        assertThrows(ReportNotFoundException.class, () -> widget.init(context("report", "Sales list", "group", "Sales")),
                "restricted reports are not even found");
    }

    @Test
    void accessIsRecheckedWhenTheDataIsLoaded() {
        var context = context("report", "Sales list", "group", "Sales");
        widget.init(context);
        reports.reports.get(0).setAccessRoles("HR");

        assertThrows(ReportAccessDeniedException.class, () -> widget.getData(context));
    }

    @Test
    void rowLimitsOfTheModuleApply() {
        reports.settings.setMaxRows(2);
        var data = (ReportWidgetData) data(context("report", "Sales list", "group", "Sales", "limit", 100));
        assertTrue(data.truncated());
        assertEquals(2, data.total());
    }
}
