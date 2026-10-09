package tools.dynamia.modules.reports.core.security;

import tools.dynamia.integration.Containers;
import tools.dynamia.modules.reports.core.domain.Report;

import java.util.Collection;
import java.util.List;

/**
 * Single entry point to check report authorization. It consults every registered {@link ReportAccessPolicy}.
 * Fails closed: a report that restricts access to roles is denied when no policy is available to evaluate it.
 */
public final class ReportAccess {

    private ReportAccess() {
    }

    public static boolean canAccess(Report report) {
        return canAccess(report, policies());
    }

    public static boolean canAccess(Report report, Collection<ReportAccessPolicy> policies) {
        if (policies.isEmpty()) {
            return report.getAccessRoleNames().isEmpty();
        }
        return policies.stream().allMatch(policy -> policy.canAccess(report));
    }

    /**
     * @throws ReportAccessDeniedException if the current user cannot access the report
     */
    public static void check(Report report) {
        if (!canAccess(report)) {
            throw new ReportAccessDeniedException(report.getName());
        }
    }

    /**
     * Keeps only the reports the current user can access.
     */
    public static List<Report> filter(Collection<Report> reports) {
        var policies = policies();
        return reports.stream().filter(r -> canAccess(r, policies)).toList();
    }

    private static Collection<ReportAccessPolicy> policies() {
        try {
            return Containers.get().findObjects(ReportAccessPolicy.class);
        } catch (RuntimeException e) {
            return List.of();
        }
    }
}
