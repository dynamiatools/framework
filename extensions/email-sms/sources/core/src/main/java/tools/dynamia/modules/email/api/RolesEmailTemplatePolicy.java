package tools.dynamia.modules.email.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import tools.dynamia.integration.sterotypes.Provider;

import java.util.Arrays;

/**
 * The default {@link EmailTemplatePolicy}: the user must have one of the roles listed in the property
 * {@code dynamia.email.editor-roles} (comma separated, with or without the {@code ROLE_} prefix). With the property
 * empty, which is the default, nobody can use the editor API.
 */
@Provider
public class RolesEmailTemplatePolicy implements EmailTemplatePolicy {

    private final String[] roles;

    /**
     * @param roles comma separated roles allowed to edit templates
     */
    public RolesEmailTemplatePolicy(@Value("${dynamia.email.editor-roles:}") String roles) {
        this.roles = Arrays.stream(roles.split(",")).map(String::trim).filter(r -> !r.isEmpty()).toArray(String[]::new);
    }

    @Override
    public boolean canEdit(HttpServletRequest request) {
        for (var role : roles) {
            if (request.isUserInRole(role) || request.isUserInRole("ROLE_" + role)) {
                return true;
            }
        }
        return false;
    }
}
