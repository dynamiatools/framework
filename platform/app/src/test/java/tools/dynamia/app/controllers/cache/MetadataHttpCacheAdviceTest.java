package tools.dynamia.app.controllers.cache;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the HTTP cache of the metadata endpoints: nothing changes by default, the included ETag policy validates by
 * content, and only the read-only metadata endpoints take part.
 */
class MetadataHttpCacheAdviceTest {

    private static final String PATH = "/apps/api/metadata";
    private final Map<String, Object> descriptor = Map.of("id", "clientesWidget", "view", "table");

    private MetadataHttpCacheAdvice advice(MetadataHttpCachePolicy custom, String property) {
        var factory = new DefaultListableBeanFactory();
        if (custom != null) {
            factory.registerSingleton("policy", custom);
        }
        var environment = new MockEnvironment();
        if (property != null) {
            environment.setProperty(MetadataHttpCacheAdvice.PROPERTY, property);
        }
        return new MetadataHttpCacheAdvice(factory.getBeanProvider(MetadataHttpCachePolicy.class), factory.getBeanProvider(JsonMapper.class), environment);
    }

    private record Exchange(MockHttpServletRequest request, MockHttpServletResponse response, Object body) {
    }

    private Exchange get(MetadataHttpCacheAdvice advice, String method, String path, String ifNoneMatch, Object body) {
        var request = new MockHttpServletRequest(method, PATH + path);
        if (ifNoneMatch != null) {
            request.addHeader("If-None-Match", ifNoneMatch);
        }
        var response = new MockHttpServletResponse();
        Object result = advice.beforeBodyWrite(body, null, MediaType.APPLICATION_JSON, null,
                new ServletServerHttpRequest(request), new ServletServerHttpResponse(response));
        return new Exchange(request, response, result);
    }

    @Test
    void doesNothingByDefault() {
        var advice = advice(null, null);

        assertFalse(advice.supports(null, null));
        var exchange = get(advice, "GET", "/views/clientesWidget", null, descriptor);
        assertSame(descriptor, exchange.body());
        assertNull(exchange.response().getHeader("ETag"));
        assertNull(exchange.response().getHeader("Cache-Control"));
    }

    @Test
    void etagPropertyEnablesContentValidation() {
        var advice = advice(null, "etag");

        assertTrue(advice.supports(null, null));
        var exchange = get(advice, "GET", "/views/clientesWidget", null, descriptor);

        assertSame(descriptor, exchange.body());
        assertNotNull(exchange.response().getHeader("ETag"));
        assertEquals("private, no-cache", exchange.response().getHeader("Cache-Control"));
        assertEquals("Accept-Language", exchange.response().getHeader("Vary"));
        assertEquals(200, exchange.response().getStatus());
    }

    @Test
    void aMatchingIfNoneMatchAnswers304WithNoBody() {
        var advice = advice(null, "etag");
        String eTag = get(advice, "GET", "/views/clientesWidget", null, descriptor).response().getHeader("ETag");

        var exchange = get(advice, "GET", "/views/clientesWidget", eTag, descriptor);

        assertNull(exchange.body());
        assertEquals(HttpStatus.NOT_MODIFIED.value(), exchange.response().getStatus());
    }

    @Test
    void aChangedDescriptorGetsAnotherEtagAndAStaleValidatorGetsTheBody() {
        var advice = advice(null, "etag");
        String old = get(advice, "GET", "/views/clientesWidget", null, descriptor).response().getHeader("ETag");
        var changed = Map.of("id", "clientesWidget", "view", "table", "fields", List.of("alias"));

        var exchange = get(advice, "GET", "/views/clientesWidget", old, changed);

        assertSame(changed, exchange.body());
        assertEquals(200, exchange.response().getStatus());
        assertNotEquals(old, exchange.response().getHeader("ETag"));
    }

    @Test
    void theLocaleIsPartOfTheValidator() {
        var policy = new ContentEtagMetadataHttpCachePolicy();
        byte[] content = "{}".getBytes();

        var es = policy.directives("/views/x", Locale.forLanguageTag("es-CO"), content).eTag();
        var en = policy.directives("/views/x", Locale.ENGLISH, content).eTag();

        assertNotEquals(es, en);
        assertEquals(es, policy.directives("/views/x", Locale.forLanguageTag("es-CO"), content).eTag());
    }

    @Test
    void onlyTheReadOnlyMetadataEndpointsTakePart() {
        var advice = advice(null, "etag");

        for (String path : List.of("/views/a", "/navigation", "/entities", "/entities/Book", "/entities/by-path", "/entities/Book/views", "/entities/Book/views/table")) {
            assertNotNull(get(advice, "GET", path, null, descriptor).response().getHeader("ETag"), path);
        }
        for (String path : List.of("", "/actions", "/entities/ref/Book/1", "/entities/ref/Book/search", "/entities/Book/action/save")) {
            assertNull(get(advice, "GET", path, null, descriptor).response().getHeader("ETag"), path);
        }
        assertNull(get(advice, "POST", "/views/a", null, descriptor).response().getHeader("ETag"));
    }

    @Test
    void anApplicationPolicyReplacesTheIncludedOne() {
        MetadataHttpCachePolicy custom = (path, locale, content) -> new MetadataCacheDirectives("\"v1\"", "max-age=60", null);
        var advice = advice(custom, "etag");

        var exchange = get(advice, "GET", "/views/a", null, descriptor);

        assertEquals("\"v1\"", exchange.response().getHeader("ETag"));
        assertEquals("max-age=60", exchange.response().getHeader("Cache-Control"));
        assertNull(exchange.response().getHeader("Vary"));
    }

    @Test
    void aPolicyThatReturnsNothingLeavesTheResponseUntouched() {
        var advice = advice((path, locale, content) -> null, null);

        var exchange = get(advice, "GET", "/views/a", null, descriptor);

        assertSame(descriptor, exchange.body());
        assertNull(exchange.response().getHeader("ETag"));
    }
}
