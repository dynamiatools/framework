package tools.dynamia.app.controllers.cache;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.MethodParameter;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import tools.dynamia.app.controllers.ApplicationMetadataController;
import tools.dynamia.commons.Messages;
import tools.dynamia.commons.StringPojoParser;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

/**
 * Applies the {@link MetadataHttpCachePolicy} to the read-only metadata endpoints of
 * {@link ApplicationMetadataController}: view descriptors, entity metadata and navigation. Nothing else is touched
 * (actions, entity data and executions are never cached).
 * <p>
 * The policy is the application's bean if there is one; otherwise {@link ContentEtagMetadataHttpCachePolicy} when
 * {@code dynamia.app.metadata.http-cache=etag}; otherwise none, and responses are exactly as before.
 */
@RestControllerAdvice(assignableTypes = ApplicationMetadataController.class)
public class MetadataHttpCacheAdvice implements ResponseBodyAdvice<Object> {

    /** Property that enables the included ETag policy. */
    public static final String PROPERTY = "dynamia.app.metadata.http-cache";

    private static final List<String> CACHEABLE = List.of(
            "/views/*", "/navigation", "/entities", "/entities/by-path", "/entities/*", "/entities/*/views", "/entities/*/views/*");

    private final AntPathMatcher matcher = new AntPathMatcher();
    private final MetadataHttpCachePolicy policy;
    private final JsonMapper mapper;

    public MetadataHttpCacheAdvice(ObjectProvider<MetadataHttpCachePolicy> custom, ObjectProvider<JsonMapper> mapper, Environment environment) {
        MetadataHttpCachePolicy configured = custom.getIfAvailable();
        if (configured == null && "etag".equalsIgnoreCase(environment.getProperty(PROPERTY))) {
            configured = new ContentEtagMetadataHttpCachePolicy();
        }
        this.policy = configured;
        this.mapper = mapper.getIfAvailable(StringPojoParser::createJsonMapper);
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return policy != null;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (policy == null || body == null || request.getMethod() != HttpMethod.GET
                || !(request instanceof ServletServerHttpRequest servletRequest)
                || !(response instanceof ServletServerHttpResponse servletResponse)) {
            return body;
        }
        String path = relativePath(servletRequest);
        if (!isCacheable(path)) {
            return body;
        }

        MetadataCacheDirectives directives = policy.directives(path, Messages.getDefaultLocale(), mapper.writeValueAsBytes(body));
        if (directives == null) {
            return body;
        }
        if (directives.cacheControl() != null) {
            response.getHeaders().set("Cache-Control", directives.cacheControl());
        }
        if (directives.vary() != null) {
            response.getHeaders().set("Vary", directives.vary());
        }
        if (directives.eTag() != null) {
            var webRequest = new ServletWebRequest(servletRequest.getServletRequest(), servletResponse.getServletResponse());
            if (webRequest.checkNotModified(directives.eTag())) {
                return null; // 304: the validator matched, no body
            }
        }
        return body;
    }

    private static String relativePath(ServletServerHttpRequest request) {
        String uri = request.getServletRequest().getRequestURI();
        String context = request.getServletRequest().getContextPath();
        String path = context != null && !context.isEmpty() && uri.startsWith(context) ? uri.substring(context.length()) : uri;
        return path.startsWith(ApplicationMetadataController.PATH) ? path.substring(ApplicationMetadataController.PATH.length()) : path;
    }

    private boolean isCacheable(String path) {
        return CACHEABLE.stream().anyMatch(pattern -> matcher.match(pattern, path));
    }
}
