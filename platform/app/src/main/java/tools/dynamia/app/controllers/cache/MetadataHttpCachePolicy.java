package tools.dynamia.app.controllers.cache;

import java.util.Locale;

/**
 * Decides how the clients may cache what {@code /apps/api/metadata/**} serves (view descriptors, entity metadata and
 * navigation). Those responses are practically static, but not always: texts depend on the request locale and an
 * application may build descriptors dynamically or vary them per user. The framework therefore does not impose a
 * policy: the application registers a bean of this type, or enables the included one with
 * {@code dynamia.app.metadata.http-cache=etag}. Without either, nothing is added to the responses.
 *
 * <pre>{@code
 * @Component
 * class VersionedMetadataCache implements MetadataHttpCachePolicy {
 *     public MetadataCacheDirectives directives(String path, Locale locale, byte[] content) {
 *         return new MetadataCacheDirectives("\"" + version + "-" + locale + "\"", "private, no-cache", "Accept-Language");
 *     }
 * }
 * }</pre>
 */
public interface MetadataHttpCachePolicy {

    /**
     * Cache directives for one response.
     *
     * @param path    the request path relative to {@code /apps/api/metadata}, e.g. {@code /views/clientesWidget}
     * @param locale  the locale the response was produced with ({@code Messages.getDefaultLocale()})
     * @param content the serialized JSON body
     * @return the directives, or {@code null} to leave the response untouched
     */
    MetadataCacheDirectives directives(String path, Locale locale, byte[] content);
}
