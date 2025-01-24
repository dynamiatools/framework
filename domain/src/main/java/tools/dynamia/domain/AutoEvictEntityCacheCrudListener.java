package tools.dynamia.domain;

import org.springframework.cache.support.NoOpCache;
import tools.dynamia.domain.util.CrudServiceListenerAdapter;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.CacheManagerUtils;
import tools.dynamia.integration.sterotypes.Listener;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@SuppressWarnings("unchecked")
@Listener
public class AutoEvictEntityCacheCrudListener extends CrudServiceListenerAdapter<Object> {


    private static final Map<Class<?>, TargetEntity> TARGETS = new ConcurrentHashMap<>();


    /**
     * Register a new Entity target to auto clear cache when entity is updated or deleted
     */
    public static <T> void register(String cacheName, Class<T> entityClass) {
        register(cacheName, entityClass, null);

    }

    /**
     * Register a new Entity target to auto clear cache when entity is updated or deleted
     */
    public static <T> void register(String cacheName, Class<T> entityClass, Function<T, List<String>> customKeysGenerator) {
        if (cacheName != null && !cacheName.isBlank() && entityClass != null) {
            TARGETS.put(entityClass, new TargetEntity<>(cacheName, customKeysGenerator));
        }
    }

    @Override
    public void afterUpdate(Object entity) {
        clearEntityCache(entity);
    }

    @Override
    public void afterDelete(Object entity) {
        clearEntityCache(entity);
    }

    @SuppressWarnings("unchecked")
    public static <T> void clearEntityCache(T entity) {
        if (entity != null) {
            var target = TARGETS.get(entity.getClass());
            if (target != null) {
                var cache = CacheManagerUtils.getCache(target.cacheName());
                if (cache != null && !(cache instanceof NoOpCache)) {
                    Serializable id = DomainUtils.findEntityId(entity);
                    String defaultKey = entity.getClass().getSimpleName() + id;
                    cache.evictIfPresent(defaultKey);

                    @SuppressWarnings("unchecked") Function<Object, List<String>> customKeys = target.customKeysGenerator();
                    if (customKeys != null) {
                        var keys = customKeys.apply(entity);
                        if (keys != null && !keys.isEmpty()) {
                            keys.forEach(cache::evictIfPresent);
                        }
                    }
                }
            }
        }
    }


    record TargetEntity<T>(String cacheName, Function<T, List<String>> customKeysGenerator) {
    }


}
