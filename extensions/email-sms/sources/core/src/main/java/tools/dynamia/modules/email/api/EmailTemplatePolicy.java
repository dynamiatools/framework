package tools.dynamia.modules.email.api;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Decides who can use the template editor API. Applications provide one (see {@link RolesEmailTemplatePolicy} for the
 * default); with no policy that says yes, nobody can edit templates through the API.
 */
public interface EmailTemplatePolicy {

    /**
     * @param request the current request, already authenticated
     * @return true when the user can read, design, migrate and test templates of the current account
     */
    boolean canEdit(HttpServletRequest request);

    /**
     * The address a test email may be sent to: the user's own. A test email must never go to an address the caller
     * chooses, or the API would be a mail relay.
     *
     * @param request the current request
     * @return the address of the user, or null when it is not known
     */
    default String testAddress(HttpServletRequest request) {
        var principal = request.getUserPrincipal();
        var name = principal == null ? null : principal.getName();
        return name != null && name.contains("@") ? name : null;
    }
}
