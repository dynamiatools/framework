package tools.dynamia.modules.reports.core.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.modules.reports.core.domain.Report;

/**
 * Default policy: when a report defines {@link Report#getAccessRoles()}, the current request user must be in at least
 * one of those roles ({@link HttpServletRequest#isUserInRole(String)}, with and without the {@code ROLE_} prefix).
 * Reports without roles are open to any user. Without a web request (background threads) a report with roles is
 * denied.
 */
@Provider
public class RolesReportAccessPolicy implements ReportAccessPolicy {

    @Override
    public boolean canAccess(Report report) {
        var roles = report.getAccessRoleNames();
        if (roles.isEmpty()) {
            return true;
        }
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            for (String role : roles) {
                if (request.isUserInRole(role) || request.isUserInRole("ROLE_" + role)) {
                    return true;
                }
            }
        }
        return false;
    }
}
