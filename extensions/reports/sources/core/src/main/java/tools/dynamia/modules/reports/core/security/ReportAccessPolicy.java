package tools.dynamia.modules.reports.core.security;

import tools.dynamia.modules.reports.core.domain.Report;

/**
 * Decides if the current user can see and run a report. Applications register their own policies as Spring beans
 * (for example based on their permission model); a report is accessible only when <b>all</b> policies allow it.
 * The default {@link RolesReportAccessPolicy} checks {@link Report#getAccessRoles()}.
 */
public interface ReportAccessPolicy {

    /**
     * @param report the report
     * @return true if the current user may see and execute the report
     */
    boolean canAccess(Report report);
}
