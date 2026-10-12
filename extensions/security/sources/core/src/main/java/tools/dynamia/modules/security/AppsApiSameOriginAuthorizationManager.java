package tools.dynamia.modules.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.net.URI;
import java.util.function.Supplier;

/**
 * Requires an authenticated user and, for session-authenticated requests, a same-origin request. Used on the methods
 * that modify data under {@code /apps/api/**}.
 * <p>
 * CSRF protection is disabled in that chain because the local frontends only call it from the same origin with the
 * session cookie. This rule is the second layer: it validates {@code Origin}, or {@code Sec-Fetch-Site} when the
 * browser does not send {@code Origin} on a same-site request, against the host of the request. When neither header
 * is present the request is denied (fail closed): a modern browser always sends at least one of them on a
 * {@code POST}, {@code PUT}, {@code PATCH} or {@code DELETE}.
 * <p>
 * Requests that carry an {@code Authorization: Bearer} header are exempt from the origin check. That header is not an
 * ambient credential: a browser never adds it by itself and a cross-site page cannot set it without a CORS preflight,
 * which this chain does not allow. The cookie is the credential that needs the check.
 */
public class AppsApiSameOriginAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Decides whether the request may modify data.
     *
     * @param authenticationSupplier the current authentication
     * @param context                the request being authorized
     * @return granted if the user is authenticated and the request is same-origin or uses a Bearer token
     */
    @Override
    public AuthorizationDecision authorize(Supplier<? extends Authentication> authenticationSupplier, RequestAuthorizationContext context) {
        Authentication authentication = authenticationSupplier.get();
        boolean authenticated = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        HttpServletRequest request = context.getRequest();
        return new AuthorizationDecision(authenticated && (hasBearerToken(request) || isSameOriginRequest(request)));
    }

    /**
     * Tells whether the request carries a Bearer token.
     *
     * @param request the request
     * @return true if the {@code Authorization} header is a non-empty Bearer token
     */
    static boolean hasBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        return authorization != null
                && authorization.startsWith(BEARER_PREFIX)
                && authorization.length() > BEARER_PREFIX.length();
    }

    /**
     * Tells whether the request comes from the same origin as the server.
     *
     * @param request the request
     * @return true if {@code Origin} matches the request host, or {@code Sec-Fetch-Site} is same-origin or same-site
     */
    static boolean isSameOriginRequest(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.isBlank()) {
            try {
                String originHost = URI.create(origin).getHost();
                return originHost != null && originHost.equalsIgnoreCase(request.getServerName());
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        String secFetchSite = request.getHeader("Sec-Fetch-Site");
        return "same-origin".equalsIgnoreCase(secFetchSite) || "same-site".equalsIgnoreCase(secFetchSite);
    }
}
