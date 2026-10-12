package tools.dynamia.modules.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppsApiSameOriginAuthorizationManagerTest {

    private final AppsApiSameOriginAuthorizationManager manager = new AppsApiSameOriginAuthorizationManager();

    private static Authentication user() {
        return new UsernamePasswordAuthenticationToken("ana", "n/a", List.of());
    }

    private static MockHttpServletRequest post() {
        var request = new MockHttpServletRequest("POST", "/apps/api/reports/1/run");
        request.setServerName("erp.example.com");
        return request;
    }

    private boolean granted(Authentication authentication, MockHttpServletRequest request) {
        AuthorizationDecision decision = manager.authorize(() -> authentication, new RequestAuthorizationContext(request));
        return decision.isGranted();
    }

    @Test
    void sameOriginSessionRequestIsGranted() {
        var request = post();
        request.addHeader("Origin", "https://erp.example.com");

        assertTrue(granted(user(), request));
    }

    @Test
    void crossOriginSessionRequestIsDenied() {
        var request = post();
        request.addHeader("Origin", "https://evil.example.org");

        assertFalse(granted(user(), request));
    }

    @Test
    void withoutOriginSecFetchSiteDecides() {
        var sameSite = post();
        sameSite.addHeader("Sec-Fetch-Site", "same-origin");
        assertTrue(granted(user(), sameSite));

        var crossSite = post();
        crossSite.addHeader("Sec-Fetch-Site", "cross-site");
        assertFalse(granted(user(), crossSite));
    }

    @Test
    void withoutAnyOriginHeaderItFailsClosed() {
        assertFalse(granted(user(), post()));
    }

    @Test
    void bearerRequestsAreExemptFromTheOriginCheck() {
        var request = post();
        request.addHeader("Authorization", "Bearer abc.def.ghi");

        assertTrue(granted(user(), request));
    }

    @Test
    void aBearerHeaderNeverReplacesAuthentication() {
        var request = post();
        request.addHeader("Authorization", "Bearer abc.def.ghi");
        var anonymous = new AnonymousAuthenticationToken("key", "guest", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

        assertFalse(granted(anonymous, request));
        assertFalse(granted(null, request));
    }

    @Test
    void otherAuthorizationSchemesAreNotExempt() {
        var request = post();
        request.addHeader("Authorization", "Basic YW5hOnNlY3JldA==");

        assertFalse(granted(user(), request));
    }

    @Test
    void anEmptyBearerIsNotExempt() {
        var request = post();
        request.addHeader("Authorization", "Bearer ");

        assertFalse(granted(user(), request));
    }
}
