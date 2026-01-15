package tools.dynamia.modules.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import tools.dynamia.commons.MapBuilder;
import tools.dynamia.commons.StringPojoParser;
import tools.dynamia.modules.security.domain.User;
import tools.dynamia.modules.security.listeners.SpringSecurtyApplicationListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication filter that processes JSON-based username and password login requests.
 * <p>
 * This filter reads the username and password from a JSON payload in the request body,
 * attempts authentication, and handles success and failure responses in JSON format.
 *
 * @author Mario A. Serrano Leones
 * @since 2026
 */
public class JsonUsernamePasswordAuthFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper = StringPojoParser.createJsonMapper();
    private final RequestCache requestCache = new HttpSessionRequestCache();


    public JsonUsernamePasswordAuthFilter(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
        setFilterProcessesUrl("/login/json");
        setSecurityContextRepository(new HttpSessionSecurityContextRepository());
        setupHandlers();
    }

    private void setupHandlers() {
        setAuthenticationSuccessHandler((request, response, authentication) -> {
            SavedRequest savedRequest = requestCache.getRequest(request, response);
            String redirectUrl = "/";
            if (savedRequest != null) {
                redirectUrl = savedRequest.getRedirectUrl();
                requestCache.removeRequest(request, response);
            }


            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);


            Map<String, Object> body = MapBuilder.put(
                    "username", authentication.getName(),
                    "status", "ok");


            if (authentication.getPrincipal() instanceof User user) {
                body.put("name", user.getFullname());
                body.put("email", user.getEmail());
                body.put("accountId", user.getAccountId());
                body.put("userId", user.getId());
                body.put("redirectUrl", redirectUrl);
                if (user.getPhoto() != null) {
                    body.put("imageUrl", user.getPhoto().toURL());
                }
                SpringSecurtyApplicationListener.fireOnUserLoginListeners(user);
                DynamiaSecurityConfig.sendJwtCookie(request, response, user);
            }


            objectMapper.writeValue(response.getWriter(), body);
        });


        setAuthenticationFailureHandler((request, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            Map<String, Object> body = new HashMap<>();
            body.put("status", "error");
            body.put("message", exception.getMessage());

            DynamiaSecurityConfig.removeJwtCookie(request, response);
            objectMapper.writeValue(response.getOutputStream(), body);
        });
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        if (request.getContentType() == null || !request.getContentType().startsWith(MediaType.APPLICATION_JSON_VALUE)) {
            throw new AuthenticationServiceException("Content-Type not supported: " + request.getContentType());
        }

        try (InputStream is = request.getInputStream()) {
            Map<String, String> credentials = objectMapper.readValue(is, Map.class);
            String username = credentials.get("username");
            String password = credentials.get("password");

            if (username == null || password == null) {
                throw new AuthenticationServiceException("Username or Password not provided");
            }

            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(username, password);

            setDetails(request, authRequest);
            return this.getAuthenticationManager().authenticate(authRequest);
        } catch (IOException e) {
            throw new AuthenticationServiceException("Error reading login request", e);
        }
    }
}
