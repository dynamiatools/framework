package tools.dynamia.modules.reports.core.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.modules.reports.api.v2.ReportRunRequest;
import tools.dynamia.modules.reports.core.ReportNotFoundException;
import tools.dynamia.modules.reports.core.domain.enums.DataType;
import tools.dynamia.modules.reports.core.security.ReportAccessDeniedException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReportsApiServiceTest {

    private ApiFixtures.InMemoryReports reports;
    private ReportsApiService service;

    @BeforeEach
    void setUp() {
        reports = new ApiFixtures.InMemoryReports();
        service = new ReportsApiService(reports);
        var all = reports.add(1L, "Sales", "All sales", "select id, region, total from sales order by id");
        var byRegion = reports.add(2L, "Sales", "By region", "select id, region, total from sales order by id");
        var filter = tools.dynamia.modules.reports.core.TestDbAccess.sqlReport("x", "x"); // only to reuse helpers
        reports.add(3L, "Finance", "Totals", "select region, sum(total) as total from sales group by region order by region");
        // filters of report 2
        var f1 = new tools.dynamia.modules.reports.core.domain.ReportFilter("region");
        f1.setReport(byRegion);
        f1.setLabel("Region");
        f1.setCondition("region = :region");
        f1.setDataType(DataType.TEXT);
        f1.setQueryValues("select distinct region from sales order by region");
        f1.setOrder(2);
        byRegion.getFilters().add(f1);
        var f2 = new tools.dynamia.modules.reports.core.domain.ReportFilter("min");
        f2.setReport(byRegion);
        f2.setLabel("Minimum");
        f2.setCondition("total >= :min");
        f2.setDataType(DataType.NUMBER);
        f2.setRequired(false);
        f2.setOrder(1);
        byRegion.getFilters().add(f2);
        reports.chart(reports.reports.get(2), "Total by region", "pie", "region", "total", true);
    }

    private static ReportRunRequest filters(Object... kv) {
        var map = new java.util.LinkedHashMap<String, Object>();
        for (int i = 0; i < kv.length; i += 2) {
            map.put((String) kv[i], kv[i + 1]);
        }
        return ReportRunRequest.ofFilters(map);
    }

    // ---- catalog and definition

    @Test
    void catalogGroupsReportsByGroupName() {
        var catalog = service.catalog();

        assertEquals(List.of("Finance", "Sales"), catalog.stream().map(g -> g.name()).toList());
        assertEquals(List.of("All sales", "By region"), catalog.get(1).reports().stream().map(r -> r.name()).toList());
        assertEquals("sales", catalog.get(1).endpointName());
    }

    @Test
    void catalogHidesInactiveAndRestrictedReports() {
        reports.reports.get(0).setActive(false);
        reports.reports.get(2).setAccessRoles("FINANCE");

        var names = service.catalog().stream().flatMap(g -> g.reports().stream()).map(r -> r.name()).toList();

        assertEquals(List.of("By region"), names);
    }

    @Test
    void definitionDescribesFiltersInOrder() {
        var definition = service.definition(2L);

        assertEquals("By region", definition.report().name());
        assertTrue(definition.autofields());
        assertEquals(List.of("min", "region"), definition.filters().stream().map(f -> f.name()).toList());
        var region = definition.filters().get(1);
        assertEquals("QUERY", region.optionsSource());
        assertEquals("NONE", definition.filters().get(0).optionsSource());
        assertEquals(List.of("xlsx", "csv", "pdf"), definition.exportFormats());
        assertTrue(definition.charts().isEmpty());

        var chartDefinition = service.definition(3L).charts();
        assertEquals(1, chartDefinition.size());
        assertEquals("pie", chartDefinition.get(0).type());
        assertEquals(0, chartDefinition.get(0).index());
    }

    @Test
    void definitionOfDeclaredFieldsListsTheColumns() {
        var report = reports.reports.get(0);
        report.setAutofields(false);
        var field = tools.dynamia.modules.reports.core.domain.ReportField.class;
        var f = new tools.dynamia.modules.reports.core.domain.ReportField();
        f.setName("TOTAL");
        f.setLabel("Total");
        f.setDataType(DataType.CURRENCY);
        f.setAlign(tools.dynamia.modules.reports.core.domain.enums.TextAlign.RIGHT);
        f.setOrder(1);
        report.getFields().add(f);

        var columns = service.definition(1L).columns();

        assertEquals(1, columns.size());
        assertEquals("CURRENCY", columns.get(0).dataType());
        assertEquals("RIGHT", columns.get(0).align());
    }

    @Test
    void unknownInactiveAndRestrictedReportsAreNotAccessible() {
        assertThrows(ReportNotFoundException.class, () -> service.definition(99L));

        reports.reports.get(0).setActive(false);
        assertThrows(ReportNotFoundException.class, () -> service.definition(1L));

        reports.reports.get(1).setAccessRoles("HR");
        assertThrows(ReportAccessDeniedException.class, () -> service.definition(2L));
        assertThrows(ReportAccessDeniedException.class, () -> service.run(2L, filters()));
        assertThrows(ReportAccessDeniedException.class, () -> service.export(2L, filters(), "csv"));
    }

    // ---- run

    @Test
    void runReturnsColumnsRowsAndTotals() {
        var result = service.run(1L, filters());

        assertEquals(List.of("ID", "REGION", "TOTAL"), result.columns().stream().map(c -> c.name()).toList());
        assertEquals(List.of("Id", "Region", "Total"), result.columns().stream().map(c -> c.label()).toList());
        assertEquals("NUMBER", result.columns().get(0).dataType());
        assertEquals("RIGHT", result.columns().get(2).align());
        assertEquals(4, result.total());
        assertEquals(4, result.rows().size());
        assertEquals(List.of("ID", "REGION", "TOTAL"), List.copyOf(result.rows().get(0).keySet()));
        assertFalse(result.truncated());
        assertTrue(result.durationMs() >= 0);
        assertTrue(result.charts().isEmpty());
    }

    @Test
    void runPagesAndSorts() {
        var page0 = service.run(1L, new ReportRunRequest(Map.of(), 0, 3, "TOTAL", "desc"));
        var page1 = service.run(1L, new ReportRunRequest(Map.of(), 1, 3, "TOTAL", "desc"));

        assertEquals(4, page0.total());
        assertEquals(3, page0.rows().size());
        assertEquals(1, page1.rows().size());
        assertEquals(3, page0.rows().get(0).get("ID"), "highest total first");
        assertEquals(4, page1.rows().get(0).get("ID"), "lowest total last");
        assertEquals(1, page1.page());
        assertEquals(3, page1.size());
    }

    @Test
    void pageBeyondTheEndIsEmpty() {
        var result = service.run(1L, new ReportRunRequest(Map.of(), 10, 3, null, null));
        assertTrue(result.rows().isEmpty());
        assertEquals(4, result.total());
    }

    @Test
    void sortByUnknownColumnIsRejected() {
        assertThrows(ValidationError.class, () -> service.run(1L, new ReportRunRequest(Map.of(), null, null, "NOPE", null)));
    }

    @Test
    void runConvertsAndAppliesFilters() {
        var result = service.run(2L, filters("region", "north", "min", "200"));
        assertEquals(1, result.total());
        assertEquals(300.25, ((Number) result.rows().get(0).get("TOTAL")).doubleValue());
    }

    @Test
    void invalidAndRequiredFiltersAreRejected() {
        assertThrows(ValidationError.class, () -> service.run(2L, filters("min", "abc")));

        reports.reports.get(1).getFilters().get(0).setRequired(true);
        var error = assertThrows(ValidationError.class, () -> service.run(2L, filters()));
        assertTrue(error.getMessage().contains("min") || error.getMessage().contains("region"));
    }

    @Test
    void runBuildsChartsFromTheResult() {
        var result = service.run(3L, filters());

        assertEquals(1, result.charts().size());
        var chart = result.charts().get(0);
        assertEquals("pie", chart.type());
        assertEquals(List.of("east", "north", "south"), chart.labels());
        var values = chart.datasets().get(0).data();
        assertEquals(50.0, values.get(0).doubleValue());
        assertEquals(400.75, values.get(1).doubleValue());
        assertEquals(chart.labels().size(), chart.datasets().get(0).backgroundColor().size());
    }

    @Test
    void groupedChartsAddUpRepeatedLabels() {
        var report = reports.reports.get(0);
        reports.chart(report, "By region", "bar", "REGION", "TOTAL", true);

        var chart = service.run(1L, filters()).charts().get(0);

        assertEquals(List.of("north", "south", "east"), chart.labels());
        assertEquals(400.75, chart.datasets().get(0).data().get(0).doubleValue());
    }

    @Test
    void ungroupedChartsKeepRepeatedLabelsAsSeparatePoints() {
        var report = reports.reports.get(0);
        reports.chart(report, "Points", "line", "REGION", "TOTAL", false);

        var chart = service.run(1L, filters()).charts().get(0);

        assertEquals(4, chart.labels().size());
        assertEquals(4, new java.util.HashSet<>(chart.labels()).size());
    }

    @Test
    void truncatedResultsAreFlagged() {
        reports.settings.setMaxRows(2);
        reports.delegate.setSettings(reports.settings);

        var result = service.run(1L, filters());

        assertTrue(result.truncated());
        assertEquals(2, result.total());
    }

    @Test
    void valuesAreSimplifiedForJson() {
        assertEquals("2025-03-01", ReportValues.simplify(java.sql.Date.valueOf("2025-03-01")));
        assertEquals("2025-03-01T10:20:30", ReportValues.simplify(java.sql.Timestamp.valueOf("2025-03-01 10:20:30")));
        assertEquals("2025-03-01", ReportValues.simplify(java.time.LocalDate.of(2025, 3, 1)));
        assertEquals("NORTH", ReportValues.simplify(Region.NORTH));
        assertEquals(5, ReportValues.simplify(5));
        assertNull(ReportValues.simplify(null));
        assertEquals("custom", ReportValues.simplify(new Object() {
            @Override
            public String toString() {
                return "custom";
            }
        }));
    }

    enum Region {NORTH}

    // ---- filter options

    @Test
    void queryOptionsAreLoadedAndSearchable() {
        var all = service.filterOptions(2L, "region", null, null);
        assertEquals(List.of("east", "north", "south"), all.stream().map(o -> o.label()).toList());

        var searched = service.filterOptions(2L, "region", "NOR", null);
        assertEquals(List.of("north"), searched.stream().map(o -> o.label()).toList());

        assertEquals(2, service.filterOptions(2L, "region", null, 2).size());
    }

    @Test
    void staticAndBooleanOptions() {
        var report = reports.reports.get(1);
        var staticFilter = report.findFilter("min");
        staticFilter.setQueryValues(null);
        staticFilter.setValues("10, 20,30");
        assertEquals(List.of("10", "20", "30"), service.filterOptions(2L, "min", null, null).stream().map(o -> o.label()).toList());

        staticFilter.setValues(null);
        staticFilter.setDataType(DataType.BOOLEAN);
        assertEquals(List.of(true, false), service.filterOptions(2L, "min", null, null).stream().map(o -> o.value()).toList());
    }

    @Test
    void filtersWithoutOptionsAndUnknownFiltersAreHandled() {
        assertTrue(service.filterOptions(2L, "min", null, null).isEmpty());
        assertThrows(ReportNotFoundException.class, () -> service.filterOptions(2L, "nope", null, null));
    }

    @Test
    void entityOptionsOnlyListEntitiesPublishedByAProvider() {
        var filter = reports.reports.get(1).findFilter("min");
        filter.setQueryValues(null);
        filter.setDataType(DataType.ENTITY);
        filter.setEntityClassName("tools.dynamia.modules.reports.core.testentities.TestCustomer");

        // no EntityFilterProvider is registered for the class: nothing is listed
        assertTrue(service.filterOptions(2L, "min", null, null).isEmpty());
    }

    // ---- export

    @Test
    void csvExportHasHeaderRowsAndSafeQuoting() {
        var file = service.export(1L, filters(), "csv");
        var lines = new String(file.content(), StandardCharsets.UTF_8).split("\r\n");

        assertEquals("text/csv; charset=UTF-8", file.contentType());
        assertTrue(file.filename().startsWith("all-sales-") && file.filename().endsWith(".csv"), file.filename());
        assertEquals("Id,Region,Total", lines[0]);
        assertEquals("1,north,100.50", lines[1]);
        assertEquals(5, lines.length);
    }

    @Test
    void columnLabelsAreReadable() {
        assertEquals("Total Sales", ReportColumns.label("TOTAL_SALES"));
        assertEquals("Total Sales", ReportColumns.label("total_sales"));
        assertEquals("Total Sales", ReportColumns.label("totalSales"));
        assertEquals("Customer Name", ReportColumns.label("customer.name"));
        assertEquals("Id", ReportColumns.label("ID"));
    }

    @Test
    void xlsxAndPdfExportsAreRealFiles() {
        var xlsx = service.export(1L, filters(), "xlsx");
        assertEquals('P', xlsx.content()[0]);
        assertEquals('K', xlsx.content()[1]);
        assertTrue(xlsx.filename().endsWith(".xlsx"));

        var pdf = service.export(1L, filters(), null).filename(); // default is xlsx
        assertTrue(pdf.endsWith(".xlsx"));

        var file = service.export(1L, filters(), "pdf");
        assertEquals("%PDF", new String(file.content(), 0, 4, StandardCharsets.US_ASCII));
        assertEquals("application/pdf", file.contentType());
    }

    @Test
    void exportAppliesFiltersAndSorting() {
        var file = service.export(2L, new ReportRunRequest(Map.of("region", "north"), null, null, "TOTAL", "desc"), "csv");
        var lines = new String(file.content(), StandardCharsets.UTF_8).split("\r\n");
        assertEquals(3, lines.length);
        assertTrue(lines[1].startsWith("3,north"));
    }

    @Test
    void unsupportedExportFormatIsRejected() {
        assertThrows(ValidationError.class, () -> service.export(1L, filters(), "docx"));
    }
}
