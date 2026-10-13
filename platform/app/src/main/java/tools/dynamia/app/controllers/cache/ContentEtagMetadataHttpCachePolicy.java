package tools.dynamia.app.controllers.cache;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

/**
 * Included policy, enabled with {@code dynamia.app.metadata.http-cache=etag}: a strong ETag derived from the content
 * and the locale, {@code Cache-Control: private, no-cache} and {@code Vary: Accept-Language}.
 * <p>
 * {@code no-cache} makes the client revalidate on every use, which costs a {@code 304} with no body instead of the
 * whole descriptor, and never serves a stale one after a deployment or a descriptor hot-reload: the ETag is the hash of
 * what would be sent, so it changes with it.
 */
public class ContentEtagMetadataHttpCachePolicy implements MetadataHttpCachePolicy {

    @Override
    public MetadataCacheDirectives directives(String path, Locale locale, byte[] content) {
        MessageDigest digest = sha256();
        digest.update(locale.toLanguageTag().getBytes(StandardCharsets.UTF_8));
        digest.update((byte) 0);
        digest.update(content);
        String eTag = "\"" + HexFormat.of().formatHex(digest.digest(), 0, 16) + "\"";
        return new MetadataCacheDirectives(eTag, "private, no-cache", "Accept-Language");
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
