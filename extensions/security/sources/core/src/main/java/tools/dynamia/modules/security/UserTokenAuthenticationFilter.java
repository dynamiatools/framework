package tools.dynamia.modules.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import tools.dynamia.commons.DateTimeUtils;
import tools.dynamia.commons.Response;
import tools.dynamia.commons.StringPojoParser;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.modules.security.domain.User;
import tools.dynamia.modules.security.domain.UserAccessToken;
import tools.dynamia.modules.security.listeners.SpringSecurtyApplicationListener;
import tools.dynamia.modules.security.services.SecurityService;
import tools.dynamia.web.util.HttpUtils;

import java.io.IOException;
import java.util.Map;

public class UserTokenAuthenticationFilter extends BasicAuthenticationFilter {

    public static final String REQUEST_AUTHORIZATION_PARAM = "authtk";
    private final LoggingService LOGGER = new SLF4JLoggingService(UserTokenAuthenticationFilter.class);
    private static final String HEADER_AUTHORIZACION_KEY = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    public UserTokenAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }


    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        String token = req.getHeader(HEADER_AUTHORIZACION_KEY);

        if (token == null || token.isBlank()) {
            //try with request param
            token = req.getParameter(REQUEST_AUTHORIZATION_PARAM);
        }

        if (token == null || !token.startsWith(TOKEN_PREFIX)) {
            chain.doFilter(req, res);
            return;
        }

        try {
            UsernamePasswordAuthenticationToken authentication = getAuthentication(req, token);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            final User user = (User) authentication.getPrincipal();

            if (isApiDomain(req)) {
                updateCurrentAccount(user);
                SpringSecurtyApplicationListener.fireOnUserTokenLoginListeners(user);
            } else {
                SpringSecurtyApplicationListener.fireOnUserLoginListeners(user);
            }
            chain.doFilter(req, res);

        } catch (ValidationError | IllegalStateException e) {
            var response = new Response<Map<String, String>>();
            response.setError(e.getMessage());
            response.setValid(false);
            response.setData(Map.of("path", req.getRequestURI()));


            var out = res.getWriter();
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");
            res.setStatus(403);
            out.print(StringPojoParser.convertPojoToJson(response));
            out.flush();
        }
    }

    private void updateCurrentAccount(User user) {
        AccountServiceAPI accountServiceAPI = Containers.get().findObject(AccountServiceAPI.class);
        if (accountServiceAPI != null) {
            logger.info("Updating current account id: " + user.getAccountId());
            accountServiceAPI.setCurrentAccount(user.getAccountId());
        }
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request, String token) {

        if (token != null) {
            token = token.replace(TOKEN_PREFIX, "");
            var service = Containers.get().findObject(SecurityService.class);

            UserAccessToken userToken = service.findAccessToken(token, isApiDomain(request));

            if (userToken != null) {
                var accountService = Containers.get().findObject(AccountServiceAPI.class);
                if (accountService != null) {
                    accountService.validateAccountStatus(userToken.getAccountId());
                }

                if (userToken.isOtp() && userToken.getHits() > 0) {
                    throw new ValidationError("OTP  " + userToken.getToken() + " used");
                }


                if (userToken.getExpirationDate() != null && DateTimeUtils.isPast(userToken.getExpirationDate())) {
                    throw new ValidationError("Token [" + userToken.getTokenName() + "] expired: " + userToken.getExpirationDate());
                }

                if (!userToken.getUser().isEnabled()) {
                    throw new ValidationError("User [" + userToken.getUser().getUsername() + "] is disabled");
                }


                LOGGER.info("Logging user session [" + userToken.getUser() + "] - Account Id [" + userToken.getAccountId() + "] using token [" + userToken.getTokenName() + "]   URI: " + request.getRequestURI());

                service.updateAccessToken(userToken);
                return buildAuthentication(userToken.getUser());
            } else {
                throw new ValidationError("Invalid access token. No user found");
            }

        }
        return null;
    }

    private static boolean isApiDomain(HttpServletRequest request) {
        return "api".equalsIgnoreCase(HttpUtils.getSubdomain(request));
    }

    public static UsernamePasswordAuthenticationToken buildAuthentication(User usuario) {
        return new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
    }
}
