package tools.dynamia.modules.reports.core.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.modules.reports.core.ReportsSettings;

import java.util.Arrays;
import java.util.List;

/**
 * Default designer policy: the request user must be in one of the roles of {@code dynamia.reports.designer-roles}
 * (with or without the {@code ROLE_} prefix). With no roles configured nobody is a designer.
 */
@Provider
public class RolesReportDesignerPolicy implements ReportDesignerPolicy {

    private final ReportsSettings settings;

    /**
     * @param settings the module settings that hold the designer roles
     */
    public RolesReportDesignerPolicy(ReportsSettings settings) {
        this.settings = settings;
    }

    @Override
    public boolean canDesign() {
        List<String> roles = Arrays.stream(settings.getDesignerRoles().split(",")).map(String::trim)
                .filter(r -> !r.isEmpty()).toList();
        if (roles.isEmpty()) {
            return false;
        }
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            return roles.stream().anyMatch(role -> request.isUserInRole(role) || request.isUserInRole("ROLE_" + role));
        }
        return false;
    }
}
