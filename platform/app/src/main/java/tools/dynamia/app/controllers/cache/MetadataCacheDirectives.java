package tools.dynamia.app.controllers.cache;

/**
 * HTTP cache headers for one metadata response.
 *
 * @param eTag         the validator, quoted ({@code "abc"}); when present a matching {@code If-None-Match} is answered with
 *                     {@code 304 Not Modified} and no body. May be {@code null}.
 * @param cacheControl the {@code Cache-Control} value, or {@code null} to not set it
 * @param vary         the {@code Vary} header names, comma separated, or {@code null} to not set it
 */
public record MetadataCacheDirectives(String eTag, String cacheControl, String vary) {
}
