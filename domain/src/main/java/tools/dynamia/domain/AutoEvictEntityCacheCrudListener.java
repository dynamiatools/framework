package tools.dynamia.domain;

import org.springframework.cache.support.NoOpCache;
import tools.dynamia.domain.util.CrudServiceListenerAdapter;
import tools.dynamia.integration.CacheManagerUtils;
import tools.dynamia.integration.sterotypes.Listener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Listener
public class AutoEvictEntityCacheCrudListener extends CrudServiceListenerAdapter<AbstractEntity> {


    private static final Map<Class<? extends AbstractEntity>, TargetEntity> TARGETS = new ConcurrentHashMap<>();


    /**
     * Register a new Entity target to auto clear cache when entity is updated or deleted
     * @param cacheName
     * @param entityClass
     */
    public static <T extends AbstractEntity> void  register(String cacheName, Class<T> entityClass) {
        register(cacheName, entityClass, null);

    }

    /**
     * Register a new Entity target to auto clear cache when entity is updated or deleted
     * @param cacheName
     * @param entityClass
     * @param customKeysGenerator
     */
    public static <T extends AbstractEntity> void register(String cacheName, Class<T> entityClass, Function<T, List<String>> customKeysGenerator) {
        if (cacheName != null && !cacheName.isBlank() && entityClass != null) {
            TARGETS.put(entityClass, new TargetEntity<T>(cacheName, customKeysGenerator));
        }
    }

    @Override
    public void afterUpdate(AbstractEntity entity) {
        clearEntityCache(entity);
    }

    @Override
    public void afterDelete(AbstractEntity entity) {
        clearEntityCache(entity);
    }

    public static <T extends AbstractEntity> void clearEntityCache(T entity) {
        if (entity != null) {
            var target = TARGETS.get(entity.getClass());
            if (target != null) {
                var cache = CacheManagerUtils.getCache(target.getCacheName());
                if (cache != null && !(cache instanceof NoOpCache)) {

                    String defaultKey = entity.getClass().getSimpleName() + entity.getId();
                    cache.evictIfPresent(defaultKey);

                    Function<AbstractEntity,List<String>> customKeys = target.getCustomKeysGenerator();
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


    static class TargetEntity<T extends AbstractEntity> {
        private final String cacheName;
        private final Function<T, List<String>> customKeysGenerator;


        public TargetEntity(String cacheName, Function<T, List<String>> customKeysGenerator) {
            this.cacheName = cacheName;
            this.customKeysGenerator = customKeysGenerator;
        }

        public Function<T, List<String>> getCustomKeysGenerator() {
            return customKeysGenerator;
        }

        public String getCacheName() {
            return cacheName;
        }
    }


}
