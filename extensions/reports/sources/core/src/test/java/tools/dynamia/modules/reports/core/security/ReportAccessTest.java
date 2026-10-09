package tools.dynamia.modules.reports.core.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.dynamia.modules.reports.core.domain.Report;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportAccessTest {

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    private static Report reportWithRoles(String roles) {
        var report = new Report();
        report.setName("r");
        report.setAccessRoles(roles);
        return report;
    }

    private static void requestWithRoles(String... roles) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        for (String role : roles) {
            when(request.isUserInRole(role)).thenReturn(true);
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void reportsWithoutRolesAreOpen() {
        assertTrue(ReportAccess.canAccess(reportWithRoles(null), List.of()));
        assertTrue(ReportAccess.canAccess(reportWithRoles("  "), List.of(new RolesReportAccessPolicy())));
    }

    @Test
    void parsesRoleNames() {
        assertEquals(List.of("ADMIN", "FINANCE"), reportWithRoles(" ADMIN , FINANCE,, ").getAccessRoleNames());
        assertEquals(List.of(), reportWithRoles(null).getAccessRoleNames());
    }

    @Test
    void failsClosedWhenRolesAreRequiredAndNoPolicyCanEvaluateThem() {
        assertFalse(ReportAccess.canAccess(reportWithRoles("ADMIN"), List.of()));
    }

    @Test
    void userInOneOfTheRolesIsAllowed() {
        requestWithRoles("FINANCE");
        assertTrue(ReportAccess.canAccess(reportWithRoles("ADMIN,FINANCE"), List.of(new RolesReportAccessPolicy())));
    }

    @Test
    void springSecurityRolePrefixIsSupported() {
        requestWithRoles("ROLE_ADMIN");
        assertTrue(ReportAccess.canAccess(reportWithRoles("ADMIN"), List.of(new RolesReportAccessPolicy())));
    }

    @Test
    void userWithoutTheRoleIsDenied() {
        requestWithRoles("SALES");
        var report = reportWithRoles("ADMIN");
        assertFalse(ReportAccess.canAccess(report, List.of(new RolesReportAccessPolicy())));
    }

    @Test
    void withoutARequestReportsWithRolesAreDenied() {
        assertFalse(new RolesReportAccessPolicy().canAccess(reportWithRoles("ADMIN")));
        assertTrue(new RolesReportAccessPolicy().canAccess(reportWithRoles(null)));
    }

    @Test
    void everyPolicyMustAllow() {
        ReportAccessPolicy allow = r -> true;
        ReportAccessPolicy deny = r -> false;
        assertTrue(ReportAccess.canAccess(reportWithRoles(null), List.of(allow, allow)));
        assertFalse(ReportAccess.canAccess(reportWithRoles(null), List.of(allow, deny)));
    }

    @Test
    void checkThrowsAccessDenied() {
        // no container in this test: no policy is found, so a restricted report is denied
        var error = assertThrows(ReportAccessDeniedException.class, () -> ReportAccess.check(reportWithRoles("ADMIN")));
        assertTrue(error.getMessage().contains("r"));
        assertDoesNotThrow(() -> ReportAccess.check(reportWithRoles(null)));
    }
}
