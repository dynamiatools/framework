package tools.dynamia.modules.reports.core;

import org.junit.jupiter.api.Test;
import tools.dynamia.modules.reports.core.domain.ReportFilter;
import tools.dynamia.modules.reports.core.domain.enums.DataType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportFilterOptionsTest {

    private ReportFilter optionsFilter(String queryValues) {
        var report = TestDb.sqlReport("r", "select 1");
        var filter = TestDb.filter(report, "region", "region = :region", DataType.TEXT);
        filter.setQueryValues(queryValues);
        return filter;
    }

    @Test
    void twoColumnsGiveValueAndLabel() {
        var db = new ReportDataSource("db", TestDb.salesDatabase());

        var options = optionsFilter("select id, region from sales order by id").loadOptions(db);

        assertEquals(4, options.size());
        assertEquals("north", options.get(0).getName());
        assertEquals(1, options.get(0).getValue());
    }

    @Test
    void oneColumnUsesTheValueAsLabel() {
        var db = new ReportDataSource("db", TestDb.salesDatabase());

        var options = optionsFilter("select distinct region from sales order by region").loadOptions(db);

        assertEquals(List.of("east", "north", "south"), options.stream().map(ReportFilterOption::getName).toList());
    }

    @Test
    void dangerousOptionQueriesAreNotExecuted() {
        var source = TestDb.salesDatabase();
        var db = new ReportDataSource("db", source);

        var options = optionsFilter("delete from sales").loadOptions(db);

        assertTrue(options.isEmpty());
        assertDoesNotThrowCount(source, 4);
    }

    private static void assertDoesNotThrowCount(javax.sql.DataSource source, int expected) {
        try (var c = source.getConnection(); var rs = c.createStatement().executeQuery("select count(*) from sales")) {
            rs.next();
            assertEquals(expected, rs.getInt(1));
        } catch (java.sql.SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void filtersWithoutOptionsSourceHaveNoOptions() {
        var report = TestDb.sqlReport("r", "select 1");
        var filter = TestDb.filter(report, "x", null, DataType.TEXT);
        assertTrue(filter.loadOptions(new ReportDataSource("db", TestDb.salesDatabase())).isEmpty());
    }
}
