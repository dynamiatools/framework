/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.actions.flow;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import tools.dynamia.commons.StringPojoParser;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.commons.logger.LoggingService;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

/**
 * Signs and verifies {@code resumeToken}s: an opaque, HMAC-signed, self-contained continuation for a
 * {@link tools.dynamia.actions.FlowRemoteAction} flow (see
 * {@code docs/design/SERVER_DRIVEN_ACTION_FLOWS.md} §4).
 * <p>
 * The token is {@code base64url(json-payload) + "." + base64url(HMAC-SHA256 signature)}. Signing prevents
 * a client from forging or replaying a tampered token (e.g. jumping straight to a fabricated result, or
 * altering accumulated ids); it deliberately does not provide secrecy — the payload is visible to whoever
 * holds the token, by design (see the design doc for why this is an accepted trade-off, not a gap).
 * <p>
 * The signing key is read from {@code dynamia.actions.flow.secret} (falls back to a random per-JVM secret
 * with a warning, same defensive pattern as {@code JWTServiceImpl} in the security extension — this module
 * cannot depend on that extension, so it manages its own independent secret). The token TTL is read from
 * {@code dynamia.actions.flow.token-ttl} (an ISO-8601 duration, e.g. {@code PT10M}), defaulting to 10 minutes.
 *
 * @author Mario A. Serrano Leones
 */
@Component
public class FlowTokenSigner {

    private static final LoggingService LOGGER = LoggingService.get(FlowTokenSigner.class);
    public static final String SECRET_PROPERTY = "dynamia.actions.flow.secret";
    public static final String TTL_PROPERTY = "dynamia.actions.flow.token-ttl";
    public static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

    private final SecretKey secretKey;
    private final Duration ttl;

    public FlowTokenSigner(Environment environment) {
        String secret = environment != null ? environment.getProperty(SECRET_PROPERTY) : null;
        if (secret == null || secret.length() < 32) {
            LOGGER.warn(SECRET_PROPERTY + " is not set or too short (min 32 chars). Using a temporal, " +
                    "per-JVM secret for action flow tokens — flows won't resume across a restart/rolling deploy.");
            secret = StringUtils.randomString() + StringUtils.randomString();
        }
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        String ttlProperty = environment != null ? environment.getProperty(TTL_PROPERTY) : null;
        this.ttl = ttlProperty != null ? Duration.parse(ttlProperty) : DEFAULT_TTL;
    }

    /** Signs {@code payload}, producing an opaque token to hand to the client as {@code resumeToken}. */
    public String sign(FlowTokenPayload payload) {
        String json = StringPojoParser.convertMapToJson(payload.toMap());
        String encodedPayload = base64Encode(json.getBytes(StandardCharsets.UTF_8));
        String signature = hmac(encodedPayload);
        return encodedPayload + "." + signature;
    }

    /**
     * Verifies {@code token}'s signature and expiry, and that it was issued for {@code expectedActionId}.
     *
     * @throws FlowTokenException if the token is malformed, tampered with, expired, or belongs to a
     *                            different action
     */
    public FlowTokenPayload verify(String token, String expectedActionId) {
        String[] parts = token != null ? token.split("\\.", 2) : new String[0];
        if (parts.length != 2) {
            throw new FlowTokenException("Malformed flow token");
        }

        String expectedSignature = hmac(parts[0]);
        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                parts[1].getBytes(StandardCharsets.UTF_8))) {
            throw new FlowTokenException("Invalid flow token signature");
        }

        String json = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        Map<String, Object> map = StringPojoParser.parseJsonToMap(json);
        FlowTokenPayload payload = FlowTokenPayload.fromMap(map);

        if (payload.expiresAt() < Instant.now().toEpochMilli()) {
            throw new FlowTokenException("Flow token expired");
        }
        if (!Objects.equals(payload.actionId(), expectedActionId)) {
            throw new FlowTokenException("Flow token does not match action");
        }
        return payload;
    }

    /** TTL applied to every newly signed token. */
    public Duration ttl() {
        return ttl;
    }

    private String hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            return base64Encode(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to sign action flow token", e);
        }
    }

    private static String base64Encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
