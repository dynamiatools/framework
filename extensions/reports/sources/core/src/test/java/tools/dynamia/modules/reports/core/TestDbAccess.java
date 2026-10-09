package tools.dynamia.modules.reports.core;

import tools.dynamia.modules.reports.core.domain.Report;

import javax.sql.DataSource;

/**
 * Public access to the test helpers of this package, for tests in other packages.
 */
public final class TestDbAccess {

    private TestDbAccess() {
    }

    public static DataSource salesDatabase() {
        return TestDb.salesDatabase();
    }

    public static Report sqlReport(String name, String sql) {
        return TestDb.sqlReport(name, sql);
    }
}
