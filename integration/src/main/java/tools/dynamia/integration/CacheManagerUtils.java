package tools.dynamia.integration;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;

import java.util.concurrent.Callable;

/**
 * Simple class to handle @{@link org.springframework.cache.CacheManager}
 */
public abstract class CacheManagerUtils {

    private static final LoggingService LOGGER = new SLF4JLoggingService(CacheManagerUtils.class);

    /**
     * Clear Cache
     *
     */
    public static boolean clearCache(String cacheName) {
        var cache = getCache(cacheName);
        if (cache != null) {
            cache.clear();
            return true;
        }
        return false;
    }

    /**
     * Clear All cache instances
     *
     */
    public static boolean clearAllCaches() {
        var mgr = Containers.get().findObject(CacheManager.class);
        if (mgr != null) {
            mgr.getCacheNames().forEach(CacheManagerUtils::clearCache);
            return true;
        } else {
            LOGGER.warn("No CacheManager instance found");
        }
        return false;
    }

    /**
     * Evict key from cache
     *
     */
    public static boolean evict(String cacheName, Object key) {
        var cache = getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            return true;
        }
        return false;
    }

    /**
     * Find {@link Cache} by name
     *
     */
    public static Cache getCache(String name) {
        if (name == null || name.isBlank()) {
            LOGGER.warn("Invalid cache name, is blank or null");
            return null;
        }

        var mgr = Containers.get().findObject(CacheManager.class);
        if (mgr != null) {
            var cache = mgr.getCache(name);
            if (cache == null) {
                LOGGER.warn("Cache with name [" + name + "] not found. Using cache manager: " + mgr.getClass().getSimpleName());
            }
            return cache;
        } else {
            LOGGER.warn("No CacheManager instance found. Make sure Spring Cache is enabled");
        }
        return null;
    }

    /**
     * Get Valye from cache
     *
     */
    public static <T> T getValue(String cacheName, Object key, Class<T> type) {
        var cache = getCache(cacheName);
        if (cache != null) {
            return cache.get(key, type);
        }
        return null;
    }

    /**
     * Get value from cache
     *
     */
    public static Cache.ValueWrapper getValue(String cacheName, Object key) {
        var cache = getCache(cacheName);
        if (cache != null) {
            return cache.get(key);
        }
        return null;
    }

    /**
     * Get value from cache. If not present call valueLoader
     *
     */
    public static <T> T getValue(String cacheName, Object key, Callable<T> valueLoader) {
        var cache = getCache(cacheName);
        if (cache != null) {
            return cache.get(key, valueLoader);
        }
        return null;
    }

    /**
     * Put a value in the cache
     *
     */
    public static boolean put(String cacheName, Object key, Object value) {
        var cache = getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
            return true;
        }
        return false;
    }


}
