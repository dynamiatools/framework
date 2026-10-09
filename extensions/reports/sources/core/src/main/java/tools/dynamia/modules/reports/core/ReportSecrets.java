package tools.dynamia.modules.reports.core;

import tools.dynamia.commons.logger.LoggingService;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Encrypts the secrets stored by the reports module (datasource passwords) with AES-256-GCM. The key is derived from
 * {@code dynamia.reports.encryption-key}. Encrypted values are prefixed with {@value #PREFIX}; values without the
 * prefix are plain text written before encryption was enabled and are still readable, so enabling the key needs no
 * data migration (rows are encrypted the next time they are saved).
 * <p>
 * Without a configured key secrets are stored as plain text and a warning is logged once.
 */
public final class ReportSecrets {

    public static final String PREFIX = "enc:v1:";
    private static final LoggingService LOGGER = LoggingService.get(ReportSecrets.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static volatile SecretKeySpec key;
    private static volatile boolean warned;

    private ReportSecrets() {
    }

    /**
     * Sets the encryption key. A blank key disables encryption.
     */
    public static void configure(String secret) {
        if (secret == null || secret.isBlank()) {
            key = null;
            return;
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
            key = new SecretKeySpec(digest, "AES");
        } catch (Exception e) {
            throw new ReportsException("Cannot configure reports encryption key", e);
        }
    }

    public static boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }

    public static String encrypt(String plain) {
        if (plain == null || plain.isEmpty() || isEncrypted(plain)) {
            return plain;
        }
        var currentKey = key;
        if (currentKey == null) {
            if (!warned) {
                warned = true;
                LOGGER.warn("Property dynamia.reports.encryption-key is not set: report datasource passwords are stored as plain text");
            }
            return plain;
        }
        try {
            byte[] iv = new byte[12];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, currentKey, new GCMParameterSpec(128, iv));
            byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] all = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, all, 0, iv.length);
            System.arraycopy(encrypted, 0, all, iv.length, encrypted.length);
            return PREFIX + Base64.getEncoder().encodeToString(all);
        } catch (Exception e) {
            throw new ReportsException("Cannot encrypt secret", e);
        }
    }

    public static String decrypt(String value) {
        if (!isEncrypted(value)) {
            return value;
        }
        var currentKey = key;
        if (currentKey == null) {
            throw new ReportsException("Encrypted datasource password found but dynamia.reports.encryption-key is not set");
        }
        try {
            byte[] all = Base64.getDecoder().decode(value.substring(PREFIX.length()));
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, currentKey, new GCMParameterSpec(128, all, 0, 12));
            return new String(cipher.doFinal(all, 12, all.length - 12), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ReportsException("Cannot decrypt secret, is dynamia.reports.encryption-key the one used to save it?", e);
        }
    }
}
