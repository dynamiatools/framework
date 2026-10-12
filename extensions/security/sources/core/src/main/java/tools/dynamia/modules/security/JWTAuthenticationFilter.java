package tools.dynamia.modules.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.dynamia.commons.logger.Loggable;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.modules.security.domain.User;
import tools.dynamia.modules.security.services.JWTService;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;

/**
 * Authenticates requests with a JWT, taken from the {@code DYNAMIA_JWT} cookie or the {@code Authorization: Bearer}
 * header.
 * <p>
 * By default it protects {@code /api/**} and accepts both sources. In {@code bearerOnly} mode it ignores the cookie:
 * used for {@code /apps/api/**}, where the browser session is the primary credential and a cookie that is sent
 * automatically must never authenticate a request by itself (CSRF).
 */
public class JWTAuthenticationFilter extends OncePerRequestFilter implements Loggable {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JWTService jwtService;
    private final AccountServiceAPI accountServiceAPI;
    private final String pathPrefix;
    private final boolean bearerOnly;

    /**
     * Creates the filter for {@code /api/**}, accepting the JWT cookie and the Bearer header.
     *
     * @param jwtService        the service that validates tokens
     * @param accountServiceAPI the account service used to validate the account status
     */
    public JWTAuthenticationFilter(JWTService jwtService, AccountServiceAPI accountServiceAPI) {
        this(jwtService, accountServiceAPI, "/api/", false);
    }

    /**
     * Creates the filter for the given path prefix.
     *
     * @param jwtService        the service that validates tokens
     * @param accountServiceAPI the account service used to validate the account status
     * @param pathPrefix        only requests whose URI starts with this prefix are filtered
     * @param bearerOnly        if true the JWT cookie is ignored and only {@code Authorization: Bearer} is accepted
     */
    public JWTAuthenticationFilter(JWTService jwtService, AccountServiceAPI accountServiceAPI, String pathPrefix, boolean bearerOnly) {
        this.jwtService = jwtService;
        this.accountServiceAPI = accountServiceAPI;
        this.pathPrefix = pathPrefix;
        this.bearerOnly = bearerOnly;
        log("JWTAuthenticationFilter initialized for " + pathPrefix + (bearerOnly ? " (bearer only)" : ""));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        Cookie jwtCookie = DynamiaSecurityConfig.getJwtCookie(request);
        String token = jwtCookie != null ? jwtCookie.getValue() : null;

        String authHeader = request.getHeader("Authorization");

        // Si no hay Authorization y no empieza por Bearer → dejar seguir
        if (jwtCookie == null && (authHeader == null || authHeader.isBlank())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Bearer only: anything that is not a Bearer token (Basic, ...) is left to the rest of the chain
        boolean bearer = authHeader != null && authHeader.startsWith(BEARER_PREFIX) && authHeader.length() > BEARER_PREFIX.length();
        if (bearerOnly && !bearer) {
            filterChain.doFilter(request, response);
            return;
        }

        if (bearer) {
            token = authHeader.substring(BEARER_PREFIX.length());
        }


        // Token requests are stateless: a session only created while handling this request (session-scoped
        // beans such as CurrentUser) must not outlive it, or every cookie-less client leaks one session per call.
        boolean hadSession = request.getSession(false) != null;
        try {
            // Validar el token
            doJwtLogin(token, request, response);

            filterChain.doFilter(request, response);
        } finally {
            if (!hadSession) {
                invalidateSession(request);
            }
        }
    }

    private static void invalidateSession(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) {
            try {
                session.invalidate();
            } catch (IllegalStateException ignored) {
                // already invalidated
            }
        }
    }

    public SecurityContext doJwtLogin(String token, HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (jwtService.isTokenValid(token)) {
            String username = jwtService.extractUsername(token);
            Long accountId = jwtService.extractClaim(token, "act_id", Long.class);
            Long userId = jwtService.extractClaim(token, "usr_id", Long.class);

            if (accountId == null || userId == null) {
                throw new AuthenticationException("Invalid JWT token: missing account or user ID");
            }

            try {
                accountServiceAPI.validateAccountStatus(accountId);
            } catch (ValidationError e) {
                throw new AuthenticationException(e.getMessage());
            }


            // Evitar reautenticar si ya hay contexto
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                log("Authenticating user from JWT token: " + username + " -> AccountID: " + accountId + " = " + request.getServerName());
                User user = User.load(accountId, userId);
                if (user == null) {
                    throw new BadCredentialsException("User not found for  JWT token");
                }
                if (!user.isEnabled()) {
                    throw new BadCredentialsException("User account is disabled");
                }

                var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                var context = DynamiaSecurityConfig.directAuthentication(authentication, request, response);

                if (jwtService.shouldRefresh(token)) {
                    log("Refreshing JWT token for user: " + username);
                    DynamiaSecurityConfig.sendJwtCookie(request, response, user);
                }
                return context;
            }
        }
        return null;
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Applies only to the configured prefix (/api/ by default)
        String path = request.getRequestURI();
        return !path.startsWith(pathPrefix);
    }
}
