package tools.dynamia.modules.reports.api.v2;

/**
 * Result of testing a datasource connection.
 *
 * @param ok      true if the connection is valid
 * @param message short description
 */
public record DataSourceTestResult(boolean ok, String message) {
}
