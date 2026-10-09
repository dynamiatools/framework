package tools.dynamia.modules.reports.core;

/**
 * A report, or one of its filters, does not exist or is not visible to the current account.
 */
public class ReportNotFoundException extends ReportsException {

    /**
     * @param message description of what was not found
     */
    public ReportNotFoundException(String message) {
        super(message);
    }

    /**
     * @param id the report id
     * @return an exception for a report that does not exist or is not visible
     */
    public static ReportNotFoundException report(Long id) {
        return new ReportNotFoundException("Report not found: " + id);
    }
}
