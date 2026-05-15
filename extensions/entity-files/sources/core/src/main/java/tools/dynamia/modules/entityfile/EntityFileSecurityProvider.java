package tools.dynamia.modules.entityfile;

import tools.dynamia.modules.entityfile.domain.EntityFile;

/**
 * Simple security provider interface to control access to entity files.
 * Implement this interface and register as a bean in the application context to control access to entity files.
 * The canAccess method will be called before allowing access to an entity file, and you can implement your custom logic
 * to determine if the access should be granted or denied based on the properties of the EntityFile object.
 */
public interface EntityFileSecurityProvider {

    /**
     *
     * @param entityFile
     * @return
     */
    boolean canAccess(EntityFile entityFile);


}
