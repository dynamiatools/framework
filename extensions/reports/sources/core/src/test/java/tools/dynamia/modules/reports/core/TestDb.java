package tools.dynamia.modules.reports.core;

import org.h2.jdbcx.JdbcDataSource;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.ReportField;
import tools.dynamia.modules.reports.core.domain.ReportFilter;
import tools.dynamia.modules.reports.core.domain.enums.DataType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/**
 * Helpers shared by the reports tests: an in-memory H2 database with a sales table and report builders.
 */
final class TestDb {

    private TestDb() {
    }

    static DataSource salesDatabase() {
        var ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:reports" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
            s.execute("create table sales (id int primary key, region varchar(20), total decimal(10,2), account_id bigint)");
            s.execute("insert into sales values (1, 'north', 100.50, 1), (2, 'south', 200.00, 1), (3, 'north', 300.25, 2), (4, 'east', 50.00, 2)");
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return ds;
    }

    static Report sqlReport(String name, String sql) {
        var report = new Report();
        report.setName(name);
        report.setQueryLang("sql");
        report.setQueryScript(sql);
        return report;
    }

    static ReportFilter filter(Report report, String name, String condition, DataType type) {
        var filter = new ReportFilter(name);
        filter.setReport(report);
        filter.setCondition(condition);
        filter.setDataType(type);
        filter.setLabel(name);
        report.getFilters().add(filter);
        return filter;
    }

    static ReportField field(Report report, String name) {
        var field = new ReportField();
        field.setName(name);
        field.setLabel(name);
        field.setReport(report);
        report.getFields().add(field);
        return field;
    }
}
