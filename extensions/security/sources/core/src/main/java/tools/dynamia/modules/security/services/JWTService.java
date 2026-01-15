package tools.dynamia.modules.security.services;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Very simple JWT service interface for generating and validating JWT tokens.
 *
 */
public interface JWTService {


    /**
     * Generates a JWT token for the given username, roles, claims and expiration duration.
     *
     * @param username     subject username
     * @param roles        list of roles
     * @param customClaims additional claims
     * @param expiration   token expiration duration
     * @return generated JWT token
     */
    String generateToken(String username, List<String> roles, Map<String, Object> customClaims, Duration expiration);

    /**
     * Extracts the username from the given JWT token.
     *
     * @param token JWT token
     * @return extracted username
     */
    String extractUsername(String token);

    /**
     * Extracts the roles from the given JWT token.
     *
     * @param token JWT token
     * @return list of roles
     */
    List<String> extractRoles(String token);

    /**
     * Extracts a specific claim from the given JWT token.
     *
     * @param token     JWT token
     * @param claimName name of the claim to extract
     * @param claimType expected type of the claim
     * @param <T>       type of the claim
     * @return extracted claim
     */
    <T> T extractClaim(String token, String claimName, Class<T> claimType);

    /**
     * Validates the given JWT token.
     *
     * @param token JWT token
     * @return true if the token is valid, false otherwise
     */
    boolean isTokenValid(String token);

    /**
     * Determines if the given JWT token should be refreshed. By default this can be based on expiration time
     * if the token is close to expiring return true
     *
     * @param token JWT token
     * @return true if the token should be refreshed, false otherwise
     */
    public boolean shouldRefresh(String token);

    /**
     * Extracts all claims from the given JWT token.
     *
     * @param token JWT token
     * @return map of claims
     */
    Map<String, Object> getClaims(String token);
}
