package tools.dynamia.modules.reports.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.enums.DataType;
import tools.dynamia.modules.reports.core.services.impl.ReportsServiceImpl;
import tools.dynamia.modules.saas.api.AccountServiceAPI;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportsServiceExecuteTest {

    private DataSource db;
    private ReportDataSource datasource;
    private AccountServiceAPI accounts;
    private ReportsServiceImpl service;
    private ReportsSettings settings;

    @BeforeEach
    void setUp() {
        db = TestDb.salesDatabase();
        datasource = new ReportDataSource("test", db);
        accounts = mock(AccountServiceAPI.class);
        when(accounts.getSystemAccountId()).thenReturn(1L);
        when(accounts.getCurrentAccountId()).thenReturn(2L);
        settings = new ReportsSettings();
        service = new ReportsServiceImpl(accounts);
        service.setSettings(settings);
    }

    @Test
    void executesSqlWithAutomaticFields() {
        var report = TestDb.sqlReport("all", "select id, region from sales order by id");

        var data = service.execute(report, new ReportFilters(), datasource);

        assertEquals(4, data.getSize());
        assertEquals(List.of("ID", "REGION"), data.getFieldNames());
        assertEquals("north", data.getEntries().get(0).getValues().get("REGION"));
        assertFalse(data.isTruncated());
    }

    @Test
    void appliesFilterConditionsWithBoundParameters() {
        var report = TestDb.sqlReport("by region", "select id from sales order by id");
        var filter = TestDb.filter(report, "region", "region = :region", DataType.TEXT);
        var filters = new ReportFilters();
        filters.add(filter, "north");

        var data = service.execute(report, filters, datasource);

        assertEquals(2, data.getSize());
    }

    @Test
    void filterValueIsNeverConcatenatedIntoTheQuery() {
        var report = TestDb.sqlReport("injection", "select id from sales");
        var filter = TestDb.filter(report, "region", "region = :region", DataType.TEXT);
        var filters = new ReportFilters();
        filters.add(filter, "north' or '1'='1");

        assertEquals(0, service.execute(report, filters, datasource).getSize());
    }

    @Test
    void usesDeclaredFieldsWhenNotAutomatic() {
        var report = TestDb.sqlReport("fields", "select id, region as REGION from sales order by id");
        report.setAutofields(false);
        TestDb.field(report, "REGION");

        var data = service.execute(report, new ReportFilters(), datasource);

        assertEquals(List.of("REGION"), data.getFieldNames());
        assertEquals(Map.of("REGION", "north"), data.getEntries().get(0).getValues());
    }

    @Test
    void accountIdParameterIsAlwaysTheCurrentAccountForSystemReports() {
        var report = TestDb.sqlReport("mine", "select id from sales where account_id = :accountId order by id");
        report.setAccountId(1L); // system report
        var filters = new ReportFilters();
        // a caller trying to read another account's data
        filters.add(TestDb.filter(report, "accountId", null, DataType.NUMBER), 1L);

        var data = service.execute(report, filters, datasource);

        assertEquals(List.of(3, 4), data.getEntries().stream().map(e -> e.getValues().get("ID")).toList());
    }

    @Test
    void tenantReportUsesItsOwnAccountWhenAccountIdIsNotAFilter() {
        var report = TestDb.sqlReport("tenant", "select id from sales where account_id = :accountId order by id");
        report.setAccountId(2L);

        var data = service.execute(report, new ReportFilters(), datasource);

        assertEquals(2, data.getSize());
    }

    @Test
    void unknownQueryLanguageFailsWithAClearError() {
        var report = TestDb.sqlReport("bad", "select 1");
        report.setQueryLang("groovy");

        var error = assertThrows(ReportsException.class, () -> service.execute(report, new ReportFilters(), datasource));
        assertTrue(error.getMessage().contains("groovy"));
    }

    @Test
    void rejectsQueriesThatModifyData() {
        var report = TestDb.sqlReport("evil", "delete from sales");

        assertThrows(ValidationError.class, () -> service.execute(report, new ReportFilters(), datasource));
        assertDoesNotThrow(() -> {
            try (var c = db.getConnection(); var rs = c.createStatement().executeQuery("select count(*) from sales")) {
                rs.next();
                assertEquals(4, rs.getInt(1));
            }
        });
    }

    @Test
    void rejectsConditionsThatSmuggleStatements() {
        var report = TestDb.sqlReport("evil filter", "select id from sales");
        var filter = TestDb.filter(report, "x", "1=1; drop table sales", DataType.TEXT);
        var filters = new ReportFilters();
        filters.add(filter, "a");

        assertThrows(ValidationError.class, () -> service.execute(report, filters, datasource));
    }

    @Test
    void truncatesAtMaxRowsAndFlagsIt() {
        settings.setMaxRows(3);
        var report = TestDb.sqlReport("limited", "select id from sales order by id");

        var data = service.execute(report, new ReportFilters(), datasource);

        assertEquals(3, data.getSize());
        assertTrue(data.isTruncated());
        assertEquals(Boolean.TRUE, new NestedMapReportDataExporter().export(data).get("truncated"));
    }

    @Test
    void exactlyMaxRowsIsNotTruncated() {
        settings.setMaxRows(4);
        var data = service.execute(TestDb.sqlReport("exact", "select id from sales"), new ReportFilters(), datasource);

        assertEquals(4, data.getSize());
        assertFalse(data.isTruncated());
        assertFalse(new NestedMapReportDataExporter().export(data).containsKey("truncated"));
    }

    @Test
    void runsOnAReadOnlyConnectionAndRestoresIt() throws Exception {
        Connection connection = spy(db.getConnection());
        var report = TestDb.sqlReport("ro", "select id from sales");

        service.execute(report, new ReportFilters(), new ReportDataSource("conn", connection));

        var order = inOrder(connection);
        order.verify(connection).setReadOnly(true);
        order.verify(connection).setReadOnly(false);
    }

    @Test
    void queryTimeoutIsAppliedToTheStatement() throws Exception {
        settings.setQueryTimeoutSeconds(7);
        Connection connection = spy(db.getConnection());
        var statements = new java.util.ArrayList<java.sql.PreparedStatement>();
        doAnswer(inv -> {
            var stm = spy((java.sql.PreparedStatement) inv.callRealMethod());
            statements.add(stm);
            return stm;
        }).when(connection).prepareStatement(anyString(), anyInt(), anyInt());
        doAnswer(inv -> {
            var stm = spy((java.sql.PreparedStatement) inv.callRealMethod());
            statements.add(stm);
            return stm;
        }).when(connection).prepareStatement(anyString());

        service.execute(TestDb.sqlReport("timeout", "select id from sales"), new ReportFilters(), new ReportDataSource("conn", connection));

        assertFalse(statements.isEmpty());
        verify(statements.get(0)).setQueryTimeout(7);
    }

    @Test
    void sqlErrorsAreWrapped() {
        var report = TestDb.sqlReport("broken", "select * from missing_table");

        var error = assertThrows(ReportsException.class, () -> service.execute(report, new ReportFilters(), datasource));
        assertNotNull(error.getCause());
    }

    @Test
    void reportDataCarriesNumbersAsReturnedByTheDatabase() {
        var data = service.execute(TestDb.sqlReport("sum", "select sum(total) as total from sales"), new ReportFilters(), datasource);

        assertEquals(new BigDecimal("650.75"), data.getEntries().get(0).getValues().get("TOTAL"));
    }

    @Test
    void reportWithoutRowsHasNoEntries() {
        var data = service.execute(TestDb.sqlReport("none", "select id from sales where id < 0"), new ReportFilters(), datasource);

        assertTrue(data.isEmpty());
        assertNotNull(data.getFieldNames());
    }

    @Test
    void executeOverloadResolvesTheDatasourceFromTheReport() {
        // without a Spring container the default datasource cannot be resolved: it must fail, not return null
        var report = TestDb.sqlReport("x", "select 1");
        assertThrows(RuntimeException.class, () -> service.execute(report, new ReportFilters()));
    }

    @Test
    void reportEntityDefaultsAreSane() {
        Report report = new Report();
        assertTrue(report.isActive());
        assertFalse(report.getExportEndpoint());
        assertEquals("sql", report.getQueryLang());
    }
}
