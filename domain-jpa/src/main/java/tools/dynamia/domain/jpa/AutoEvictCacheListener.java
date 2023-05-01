package tools.dynamia.domain.jpa;

import tools.dynamia.domain.AbstractEntity;
import tools.dynamia.domain.AutoEvictEntityCacheCrudListener;

import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

/**
 * Auxiliary helper listener to auto evict cache for entity that are not detected by {@link AutoEvictEntityCacheCrudListener}.
 * You need to register directly in the JPA entity using {@link javax.persistence.EntityListeners} annotation.
 */
public class AutoEvictCacheListener {

    @PostUpdate
    @PostRemove
    public void clearCache(AbstractEntity object) {
        AutoEvictEntityCacheCrudListener.clearEntityCache(object);

    }
}
