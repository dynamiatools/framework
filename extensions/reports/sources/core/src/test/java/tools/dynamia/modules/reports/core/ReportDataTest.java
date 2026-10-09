package tools.dynamia.modules.reports.core;

import org.junit.jupiter.api.Test;
import tools.dynamia.domain.jdbc.JdbcDataSet;
import tools.dynamia.modules.reports.core.domain.Report;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReportDataTest {

    private static Map<String, Object> row(Object... keyValues) {
        var map = new LinkedHashMap<String, Object>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    private static ReportData data(Report report, Map<String, Object>... rows) {
        return ReportData.build(report, new JdbcDataSet(new ArrayList<>(List.of(rows))));
    }

    @Test
    void entriesKeepTheOrderOfTheColumns() {
        var data = data(TestDb.sqlReport("r", "select 1"), row("zeta", 1, "alpha", 2, "mid", 3));

        assertEquals(List.of("zeta", "alpha", "mid"), List.copyOf(data.getEntries().get(0).getValues().keySet()));
    }

    @Test
    void nestedExporterGroupsDottedNamesAndKeepsOrder() {
        var data = data(TestDb.sqlReport("r", "select 1"),
                row("id", 1, "customer.name", "Ana", "customer.city", "Cali", "total", 5));

        var result = new NestedMapReportDataExporter().export(data);

        var rows = (List<Map<String, Object>>) result.get("data");
        var first = rows.get(0);
        assertEquals(List.of("id", "customer", "total"), List.copyOf(first.keySet()));
        assertEquals(Map.of("name", "Ana", "city", "Cali"), first.get("customer"));
        assertFalse(result.containsKey("truncated"));
    }

    @Test
    void nestedExporterReplacesScalarsThatCollideWithGroups() {
        var grouped = NestedMapReportDataExporter.groupValues(row("a", 1, "a.b", 2));
        assertEquals(Map.of("b", 2), grouped.get("a"));
    }

    @Test
    void sortHandlesNullsAndMixedValues() {
        var data = data(TestDb.sqlReport("r", "select 1"), row("v", 3), row("v", null), row("v", 1));

        data.sort("v", true);
        assertEquals(java.util.Arrays.asList(null, 1, 3), data.getEntries().stream().map(e -> e.getValues().get("v")).toList());

        data.sort("v", false);
        assertEquals(java.util.Arrays.asList(3, 1, null), data.getEntries().stream().map(e -> e.getValues().get("v")).toList());
    }

    @Test
    void explicitFieldsOfArrayRowsAreMappedByPosition() {
        var report = TestDb.sqlReport("r", "select a, b from X");
        report.setAutofields(false);
        TestDb.field(report, "first");
        TestDb.field(report, "second");

        var data = ReportData.build(report, List.of(new Object[]{1, "x"}, new Object[]{2}));

        assertEquals(Map.of("first", 1, "second", "x"), data.getEntries().get(0).getValues());
        assertNull(data.getEntries().get(1).getValues().get("second"));
    }

    @Test
    void emptyCollectionWithAutofieldsHasResultColumn() {
        var data = ReportData.build(TestDb.sqlReport("r", "select x from X"), List.of());
        assertTrue(data.isEmpty());
        assertEquals(List.of("Result"), data.getFieldNames());
    }

    @Test
    void excelExportCreatesAFileWithTheRowsAndHeaders() throws Exception {
        var report = TestDb.sqlReport("Sales Report", "select 1");
        var data = data(report, row("region", "north", "total", 5), row("region", "south", "total", 7));

        File file = new ExcelReportDataExporter(report).export(data);
        try {
            assertTrue(file.getName().endsWith(".xlsx"));
            assertTrue(file.length() > 0);
        } finally {
            file.delete();
        }
    }
}
