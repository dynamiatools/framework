package tools.dynamia.modules.reports.core;

/**
 * Common exception for DynamiaReports services
 */
public class ReportsException extends RuntimeException {
    public ReportsException() {
    }

    public ReportsException(String var1) {
        super(var1);
    }

    public ReportsException(String var1, Throwable var2) {
        super(var1, var2);
    }

    public ReportsException(Throwable var1) {
        super(var1);
    }
}
