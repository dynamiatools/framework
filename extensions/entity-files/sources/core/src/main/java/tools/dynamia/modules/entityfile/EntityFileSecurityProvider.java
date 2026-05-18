package tools.dynamia.modules.entityfile;

import tools.dynamia.modules.entityfile.domain.EntityFile;

import java.util.Map;

/**
 * Simple security provider interface to control access to entity files.
 * Implement this interface and register as a bean in the application context to control access to entity files.
 * The canAccess method will be called before allowing access to an entity file, and you can implement your custom logic
 * to determine if the access should be granted or denied based on the properties of the EntityFile object.
 */
public interface EntityFileSecurityProvider {

    /**
     * Check if entity file has access
     *
     * @param entityFile entity file
     * @param params     request parameters
     * @param headers    request headers
     * @return allowed
     */
    boolean canAccess(EntityFile entityFile, Map<String, String> params, Map<String, String> headers);

    /**
     * Generate a token for the given entity file. This token can be used to grant temporary access to the file without requiring authentication.
     *
     * @param entityFile entity file
     * @return token
     */
    String generateToken(EntityFile entityFile);


}
