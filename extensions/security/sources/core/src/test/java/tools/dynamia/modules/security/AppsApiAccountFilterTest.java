package tools.dynamia.modules.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.modules.security.domain.User;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppsApiAccountFilterTest {

    private final List<Long> boundAccounts = new ArrayList<>();
    private final List<Boolean> requestInScope = new ArrayList<>();

    private final AccountServiceAPI accountService = (AccountServiceAPI) Proxy.newProxyInstance(
            getClass().getClassLoader(), new Class<?>[]{AccountServiceAPI.class},
            (proxy, method, args) -> {
                if (method.getName().equals("setCurrentAccount")) {
                    boundAccounts.add((Long) args[0]);
                    requestInScope.add(RequestContextHolder.getRequestAttributes() != null);
                }
                return method.getReturnType() == boolean.class ? Boolean.FALSE : null;
            });

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    private static User user(Long accountId) {
        var user = new User();
        user.setAccountId(accountId);
        return user;
    }

    private MockFilterChain run() throws Exception {
        var chain = new MockFilterChain();
        new AppsApiAccountFilter(accountService).doFilter(
                new MockHttpServletRequest("GET", "/apps/api/reports/catalog"), new MockHttpServletResponse(), chain);
        return chain;
    }

    @Test
    void bindsTheAccountOfTheSessionUser() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user(7L), "n/a", List.of()));

        var chain = run();

        assertEquals(List.of(7L), boundAccounts);
        assertEquals(List.of(true), requestInScope, "the account service needs the request in scope");
        assertNotNull(chain.getRequest(), "the request continues down the chain");
        assertNull(RequestContextHolder.getRequestAttributes(), "a scope opened by the filter is closed by it");
    }

    @Test
    void doesNothingWithoutAuthentication() throws Exception {
        var chain = run();

        assertTrue(boundAccounts.isEmpty());
        assertNotNull(chain.getRequest());
    }

    @Test
    void ignoresPrincipalsThatAreNotUsersOrHaveNoAccount() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("ana", "n/a", List.of()));
        run();

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user(null), "n/a", List.of()));
        run();

        assertTrue(boundAccounts.isEmpty());
    }
}
