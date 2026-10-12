package tools.dynamia.modules.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.modules.security.domain.User;

import java.io.IOException;

/**
 * Binds the account (tenant) of the authenticated user to the request in {@code /apps/api/**}.
 * <p>
 * A request authenticated with a Bearer token already gets its account bound by {@link JWTAuthenticationFilter}. A
 * request authenticated with the web session does not: the session holds the user but nothing binds the account
 * before the first multi-tenant query, which then runs without it and finds nothing. This filter does for the session
 * what the JWT filter does for the token, so both credentials see the same data.
 */
public class AppsApiAccountFilter extends OncePerRequestFilter {

    private final AccountServiceAPI accountServiceAPI;

    /**
     * Creates the filter.
     *
     * @param accountServiceAPI the service used to bind the account of the user to the request
     */
    public AppsApiAccountFilter(AccountServiceAPI accountServiceAPI) {
        this.accountServiceAPI = accountServiceAPI;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user && user.getAccountId() != null) {
            // The account service needs the request in scope; Spring Boot's RequestContextFilter may not be there.
            boolean ownsScope = RequestContextHolder.getRequestAttributes() == null;
            if (ownsScope) {
                RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
            }
            try {
                accountServiceAPI.setCurrentAccount(user.getAccountId());
                filterChain.doFilter(request, response);
            } finally {
                if (ownsScope) {
                    RequestContextHolder.resetRequestAttributes();
                }
            }
            return;
        }

        filterChain.doFilter(request, response);
    }
}
