package tools.dynamia.modules.security;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.modules.security.services.JWTService;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JWTAuthenticationFilterTest {

    private final List<String> validatedTokens = new ArrayList<>();
    private JWTService jwtService;
    private AccountServiceAPI accountService;

    @BeforeEach
    void setUp() {
        // Every token is reported as invalid: the tests only care about which tokens reach the validation.
        jwtService = (JWTService) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{JWTService.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("isTokenValid")) {
                        validatedTokens.add((String) args[0]);
                        return false;
                    }
                    return method.getReturnType() == boolean.class ? Boolean.FALSE : null;
                });
        accountService = (AccountServiceAPI) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{AccountServiceAPI.class},
                (proxy, method, args) -> method.getReturnType() == boolean.class ? Boolean.FALSE : null);
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
    }

    private MockFilterChain run(JWTAuthenticationFilter filter, MockHttpServletRequest request) throws Exception {
        var chain = new MockFilterChain();
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        return chain;
    }

    private static MockHttpServletRequest request(String uri) {
        return new MockHttpServletRequest("GET", uri);
    }

    @Test
    void apiFilterAcceptsTheCookie() throws Exception {
        var request = request("/api/things");
        request.setCookies(new Cookie(DynamiaSecurityConfig.JWT_COOKIE_NAME, "cookie-token"));

        var chain = run(new JWTAuthenticationFilter(jwtService, accountService), request);

        assertEquals(List.of("cookie-token"), validatedTokens);
        assertNotNull(chain.getRequest());
    }

    @Test
    void apiFilterAcceptsTheBearerHeader() throws Exception {
        var request = request("/api/things");
        request.addHeader("Authorization", "Bearer header-token");

        run(new JWTAuthenticationFilter(jwtService, accountService), request);

        assertEquals(List.of("header-token"), validatedTokens);
    }

    @Test
    void apiFilterIgnoresOtherPrefixes() throws Exception {
        var request = request("/apps/api/things");
        request.addHeader("Authorization", "Bearer header-token");

        var chain = run(new JWTAuthenticationFilter(jwtService, accountService), request);

        assertTrue(validatedTokens.isEmpty());
        assertNotNull(chain.getRequest());
    }

    @Test
    void bearerOnlyFilterIgnoresTheCookie() throws Exception {
        var request = request("/apps/api/things");
        request.setCookies(new Cookie(DynamiaSecurityConfig.JWT_COOKIE_NAME, "cookie-token"));

        var chain = run(new JWTAuthenticationFilter(jwtService, accountService, "/apps/api/", true), request);

        assertTrue(validatedTokens.isEmpty());
        assertNotNull(chain.getRequest(), "the request must continue down the chain to the session authentication");
    }

    @Test
    void bearerOnlyFilterUsesTheHeaderEvenWithACookie() throws Exception {
        var request = request("/apps/api/things");
        request.setCookies(new Cookie(DynamiaSecurityConfig.JWT_COOKIE_NAME, "cookie-token"));
        request.addHeader("Authorization", "Bearer header-token");

        run(new JWTAuthenticationFilter(jwtService, accountService, "/apps/api/", true), request);

        assertEquals(List.of("header-token"), validatedTokens);
    }

    @Test
    void bearerOnlyFilterIgnoresOtherAuthorizationSchemes() throws Exception {
        var request = request("/apps/api/things");
        request.addHeader("Authorization", "Basic YW5hOnNlY3JldA==");

        var chain = run(new JWTAuthenticationFilter(jwtService, accountService, "/apps/api/", true), request);

        assertTrue(validatedTokens.isEmpty());
        assertNotNull(chain.getRequest());
    }

    @Test
    void bearerOnlyFilterIgnoresTheApiPrefix() throws Exception {
        var request = request("/api/things");
        request.addHeader("Authorization", "Bearer header-token");

        run(new JWTAuthenticationFilter(jwtService, accountService, "/apps/api/", true), request);

        assertTrue(validatedTokens.isEmpty());
    }

    @Test
    void appsApiRequestsCountAsApiRequests() {
        assertTrue(DynamiaSecurityConfig.isApiRequest(request("/apps/api/dashboard/main/widgets/kpi")));
        assertTrue(DynamiaSecurityConfig.isApiRequest(request("/api/things")));
    }
}
