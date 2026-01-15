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

public class JWTAuthenticationFilter extends OncePerRequestFilter implements Loggable {

    private final JWTService jwtService;
    private final AccountServiceAPI accountServiceAPI;

    public JWTAuthenticationFilter(JWTService jwtService, AccountServiceAPI accountServiceAPI) {
        this.jwtService = jwtService;
        this.accountServiceAPI = accountServiceAPI;
        log("JWTAuthenticationFilter initialized");
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

        if (authHeader != null && authHeader.startsWith("Bearer ") && authHeader.length() > 7) {
            token = authHeader.substring(7);
        }


        // Validar el token
        doJwtLogin(token, request, response);

        filterChain.doFilter(request, response);
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
        // Aplica solo a rutas /api/**
        String path = request.getRequestURI();
        return !path.startsWith("/api/");
    }
}
