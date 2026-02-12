package tools.dynamia.modules.security.services.impl;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;
import tools.dynamia.commons.SimpleCache;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.commons.logger.Loggable;
import tools.dynamia.modules.security.DynamiaSecurityConfigurationProperties;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class JWTServiceImpl implements tools.dynamia.modules.security.services.JWTService, Loggable {

    public static final int EXPIRATION_MARGIN = 30;
    public static final int CACHE_SIZE = 1000;
    private static final SimpleCache<String, SignedJWT> tokenCache = new SimpleCache<>();
    public static final String ROLES = "roles";

    private final SecretKey secretKey;
    private final DynamiaSecurityConfigurationProperties configProperties;

    public JWTServiceImpl(DynamiaSecurityConfigurationProperties configProperties) {

        String secret = configProperties.getJwtSecretKey();
        if (secret == null || secret.length() < 32) {
            logWarn("JWT_SECRET environment variable is not set or too short. Using temporal secret for JWT signing.");
            secret = StringUtils.randomString() + StringUtils.randomString(); // simple way to ensure at least 32 chars
        }
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.configProperties = configProperties;
    }


    @Override
    public String generateToken(String username, List<String> roles, Map<String, Object> customClaims, Duration expiration) {
        try {
            JWSSigner signer = new MACSigner(secretKey);

            var claimsBuilder = new JWTClaimsSet.Builder()
                    .subject(username)
                    .issueTime(new Date())
                    .expirationTime(Date.from(Instant.now().plus(expiration)))
                    .issuer(configProperties.getIssuer());

            if (roles != null && !roles.isEmpty()) {
                claimsBuilder.claim(ROLES, roles);
            }

            if (customClaims != null && !customClaims.isEmpty()) {
                customClaims.forEach((name, value) -> {
                    if (value != null) {
                        claimsBuilder.claim(name, value);
                    }
                });
            }

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claimsBuilder.build()
            );

            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Error issuing jwt", e);
        }
    }

    @Override
    public String extractUsername(String token) {
        try {
            return parseToken(token).getJWTClaimsSet().getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<String> extractRoles(String token) {
        try {
            Object roles = parseToken(token).getJWTClaimsSet().getClaim(ROLES);
            if (roles instanceof List<?> list) {
                return list.stream().map(Object::toString).toList();
            }
            return List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    private static SignedJWT parseToken(String token) {
        var result = tokenCache.getOrLoad(token, t -> {
            try {
                return SignedJWT.parse(token);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });

        if (tokenCache.keySet().size() >= CACHE_SIZE) {
            tokenCache.clear();
        }

        return result;
    }

    @Override
    public <T> T extractClaim(String token, String claimName, Class<T> claimType) {
        try {
            Object claim = parseToken(token).getJWTClaimsSet().getClaim(claimName);
            if (claimType.isInstance(claim)) {
                return claimType.cast(claim);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean isTokenValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            SignedJWT signedJWT = parseToken(token);
            JWSVerifier verifier = new com.nimbusds.jose.crypto.MACVerifier(secretKey);

            Date exp = signedJWT.getJWTClaimsSet().getExpirationTime();
            return signedJWT.verify(verifier)
                    && exp != null
                    && exp.toInstant().isAfter(Instant.now().minusSeconds(EXPIRATION_MARGIN));

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean shouldRefresh(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            SignedJWT signedJWT = parseToken(token);
            Date iat = signedJWT.getJWTClaimsSet().getIssueTime();
            Date exp = signedJWT.getJWTClaimsSet().getExpirationTime();

            if (iat == null || exp == null) {
                return false;
            }

            Instant now = Instant.now();
            long totalSeconds = Duration.between(iat.toInstant(), exp.toInstant()).getSeconds();
            long remainingSeconds = Duration.between(now, exp.toInstant()).getSeconds();

            // Refrescar si queda menos del 10 % de vida
            return remainingSeconds < totalSeconds * 0.1;

        } catch (Exception e) {
            return false;
        }
    }



    @Override
    public Map<String, Object> getClaims(String token) {
        try {
            return parseToken(token).getJWTClaimsSet().getClaims();
        } catch (Exception e) {
            return null;
        }
    }
}
