package tools.dynamia.modules.reports.core;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import tools.dynamia.integration.sterotypes.Provider;

/**
 * Execution limits and security settings of the reports module. Values come from the application properties:
 * <ul>
 *     <li>{@code dynamia.reports.max-rows}: maximum rows returned by a report (default 100000). Extra rows are
 *     dropped and the result is flagged as truncated.</li>
 *     <li>{@code dynamia.reports.query-timeout}: query timeout in seconds (default 60).</li>
 *     <li>{@code dynamia.reports.allowed-drivers}: comma separated JDBC driver classes allowed in external
 *     datasources (a default list of common databases is used when empty).</li>
 *     <li>{@code dynamia.reports.encryption-key}: key used to encrypt datasource passwords at rest.</li>
 * </ul>
 */
@Provider
public class ReportsSettings {

    @Value("${dynamia.reports.max-rows:100000}")
    private int maxRows = 100_000;

    @Value("${dynamia.reports.query-timeout:60}")
    private int queryTimeoutSeconds = 60;

    @Value("${dynamia.reports.allowed-drivers:}")
    private String allowedDrivers = "";

    @Value("${dynamia.reports.encryption-key:}")
    private String encryptionKey = "";

    /**
     * Pushes the security settings to the classes that cannot be injected (JPA converter, static helpers).
     */
    @PostConstruct
    public void apply() {
        ReportSecrets.configure(encryptionKey);
        ReportDataSourceValidator.configureAllowedDrivers(allowedDrivers);
    }

    public int getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    public int getQueryTimeoutSeconds() {
        return queryTimeoutSeconds;
    }

    public void setQueryTimeoutSeconds(int queryTimeoutSeconds) {
        this.queryTimeoutSeconds = queryTimeoutSeconds;
    }

    public String getAllowedDrivers() {
        return allowedDrivers;
    }

    public void setAllowedDrivers(String allowedDrivers) {
        this.allowedDrivers = allowedDrivers;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }
}
