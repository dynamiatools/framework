package tools.dynamia.domain.jpa;

import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import tools.dynamia.domain.AbstractEntity;
import tools.dynamia.domain.AutoEvictEntityCacheCrudListener;

/**
 * Auxiliary helper listener to auto evict cache for entity that are not detected by {@link AutoEvictEntityCacheCrudListener}.
 * You need to register directly in the JPA entity using {@link jakarta.persistence.EntityListeners} annotation.
 */
public class AutoEvictCacheListener {

    @PostUpdate
    @PostRemove
    public void clearCache(AbstractEntity object) {
        AutoEvictEntityCacheCrudListener.clearEntityCache(object);

    }
}
