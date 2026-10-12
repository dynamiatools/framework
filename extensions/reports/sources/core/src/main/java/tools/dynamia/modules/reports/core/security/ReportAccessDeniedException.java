package tools.dynamia.modules.reports.core.security;

import tools.dynamia.modules.reports.core.ReportsException;

/**
 * The current user is not allowed to access a report.
 */
public class ReportAccessDeniedException extends ReportsException {

    public ReportAccessDeniedException(String reportName) {
        super("Access denied to report [" + reportName + "]");
    }
}
