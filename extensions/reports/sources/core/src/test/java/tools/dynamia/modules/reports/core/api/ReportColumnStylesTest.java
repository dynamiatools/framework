package tools.dynamia.modules.reports.core.api;

import org.junit.jupiter.api.Test;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.ReportField;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The styles a designer chooses for a field reach the API columns, so a front end other than ZK can honor them.
 */
class ReportColumnStylesTest {

    private static ReportField field(Report report, String name, int order, String cellStyle, String columnStyle) {
        var field = new ReportField();
        field.setReport(report);
        field.setName(name);
        field.setLabel(name);
        field.setOrder(order);
        field.setCellStyle(cellStyle);
        field.setColumnStyle(columnStyle);
        report.getFields().add(field);
        return field;
    }

    private static Report report() {
        var report = new Report();
        report.setName("Styles");
        report.setAutofields(false);
        return report;
    }

    @Test
    void declaredColumnsCarryTheDesignersStyles() {
        var report = report();
        field(report, "total", 1, "green lighten-5", "bold");

        var column = ReportColumns.declared(report).get(0);

        assertEquals("green lighten-5", column.cellStyle());
        assertEquals("bold", column.columnStyle());
    }

    @Test
    void blankStylesAreNotSent() {
        var report = report();
        field(report, "a", 1, "  ", "");
        field(report, "b", 2, null, null);

        var columns = ReportColumns.declared(report);

        assertNull(columns.get(0).cellStyle());
        assertNull(columns.get(0).columnStyle());
        assertNull(columns.get(1).cellStyle());
    }

    @Test
    void theJsonOmitsTheStyleKeysWhenThereAreNoneAndKeepsTheRest() {
        var report = report();
        field(report, "a", 1, null, null);
        field(report, "b", 2, "red lighten-4", null);
        var mapper = JsonMapper.builder().build();

        var plain = mapper.writeValueAsString(ReportColumns.declared(report).get(0));
        var styled = mapper.writeValueAsString(ReportColumns.declared(report).get(1));

        assertFalse(plain.contains("cellStyle"));
        assertFalse(plain.contains("columnStyle"));
        assertTrue(plain.contains("\"format\":null"), "the existing keys keep being sent as before: " + plain);
        assertTrue(styled.contains("\"cellStyle\":\"red lighten-4\""));
        assertFalse(styled.contains("columnStyle"));
    }

    @Test
    void theOldConstructorStillWorks() {
        var column = new tools.dynamia.modules.reports.api.v2.ReportColumn("a", "A", "TEXT", "LEFT", null, null, false);

        assertNull(column.cellStyle());
        assertNull(column.columnStyle());
    }
}
