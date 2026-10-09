package tools.dynamia.modules.reports.core;

import tools.dynamia.domain.ValidationError;
import tools.dynamia.modules.reports.core.domain.ReportDataSourceConfig;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Validates the connection settings of an external report datasource: the driver must be in the allowed list and the
 * URL must be a plain JDBC URL without options known to execute code or load remote content.
 * <p>
 * The allowed drivers default to the common server databases. Set {@code dynamia.reports.allowed-drivers} to replace
 * the list.
 */
public final class ReportDataSourceValidator {

    public static final Set<String> DEFAULT_DRIVERS = Set.of(
            "com.mysql.cj.jdbc.Driver",
            "org.mariadb.jdbc.Driver",
            "org.postgresql.Driver",
            "oracle.jdbc.OracleDriver",
            "com.microsoft.sqlserver.jdbc.SQLServerDriver");

    private static final Set<String> DANGEROUS_OPTIONS = Set.of("init=", "runscript", "autodeserialize", "allowloadlocalinfile",
            "allowurlinlocalinfile", "socketfactory", "sslfactory", "allowmultiqueries", "queryinterceptors",
            "statementinterceptors", "connectionlifecycleinterceptors", "serverstatusdiffinterceptors", "trustcertificatekeystoreurl");

    private static volatile Set<String> allowedDrivers = DEFAULT_DRIVERS;

    private ReportDataSourceValidator() {
    }

    /**
     * Replaces the allowed drivers. A blank value restores the default list.
     *
     * @param drivers comma separated driver class names
     */
    public static void configureAllowedDrivers(String drivers) {
        if (drivers == null || drivers.isBlank()) {
            allowedDrivers = DEFAULT_DRIVERS;
        } else {
            allowedDrivers = new LinkedHashSet<>(Arrays.stream(drivers.split(",")).map(String::trim)
                    .filter(d -> !d.isEmpty()).toList());
        }
    }

    public static Set<String> getAllowedDrivers() {
        return allowedDrivers;
    }

    public static void validate(ReportDataSourceConfig config) {
        validate(config.getDriverClassName(), config.getUrl());
    }

    /**
     * @throws ValidationError if the driver is not allowed or the URL is not acceptable
     */
    public static void validate(String driverClassName, String url) {
        if (driverClassName == null || driverClassName.isBlank()) {
            throw new ValidationError("Select datasource driver class");
        }
        if (url == null || url.isBlank()) {
            throw new ValidationError("Enter datasource jdbc valid URL");
        }
        if (!allowedDrivers.contains(driverClassName.trim())) {
            throw new ValidationError("Driver not allowed for report datasources: " + driverClassName
                    + ". Allowed: " + allowedDrivers);
        }
        String lower = url.trim().toLowerCase(Locale.ROOT);
        if (!lower.startsWith("jdbc:")) {
            throw new ValidationError("Datasource URL must start with jdbc:");
        }
        for (String option : DANGEROUS_OPTIONS) {
            if (lower.contains(option)) {
                throw new ValidationError("Datasource URL option not allowed: " + option.replace("=", ""));
            }
        }
    }
}
