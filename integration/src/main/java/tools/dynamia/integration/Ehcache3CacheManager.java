package tools.dynamia.integration;

import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Very simple CacheManager for Ehcache 3. Delegate all cache creation to native {@link org.ehcache.CacheManager}
 * By default keys type are String and values type are Object.class
 */
public class Ehcache3CacheManager extends AbstractCacheManager {

    private org.ehcache.CacheManager nativeCacheManager;
    private boolean autocreate = true;
    private CacheConfiguration defaultConfiguration;
    private Class<String> defaultKeyType = String.class;
    private Class<Object> defaultValueType = Object.class;
    private int defaultPoolHeapEntries = 100;

    public Ehcache3CacheManager() {
        initDefaultConfiguration();
    }

    public Ehcache3CacheManager(org.ehcache.CacheManager nativeCacheManager) {
        this.nativeCacheManager = nativeCacheManager;
        initDefaultConfiguration();
    }

    public Ehcache3CacheManager(org.ehcache.CacheManager nativeCacheManager, CacheConfiguration defaultConfiguration) {
        this.nativeCacheManager = nativeCacheManager;
        this.defaultConfiguration = defaultConfiguration;
    }


    protected void initDefaultConfiguration() {
        defaultConfiguration = CacheConfigurationBuilder.newCacheConfigurationBuilder(defaultKeyType, defaultValueType,
                ResourcePoolsBuilder.heap(defaultPoolHeapEntries)).build();
    }


    @Override
    protected Cache getMissingCache(String name) {
        var cache = getNativeCacheManager().createCache(name, getDefaultConfiguration());
        return new Ehcache3Cache(cache, name);
    }

    protected Collection<Cache> loadCaches() {
        var cacheManager = getNativeCacheManager();
        Assert.state(cacheManager != null, "No CacheManager set");

        Collection<Cache> caches = new LinkedHashSet<>();
        for (String cacheName : cacheManager.getRuntimeConfiguration().getCacheConfigurations().keySet()) {
            var cache = cacheManager.getCache(cacheName, defaultKeyType, defaultValueType);
            caches.add(new Ehcache3Cache(cache, cacheName));
        }
        return caches;
    }


    public org.ehcache.CacheManager getNativeCacheManager() {
        if (nativeCacheManager == null) {
            nativeCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                    .withCache("defaultCache", getDefaultConfiguration())
                    .build(true);
        }
        return nativeCacheManager;
    }

    public void setNativeCacheManager(org.ehcache.CacheManager nativeCacheManager) {
        this.nativeCacheManager = nativeCacheManager;
    }

    public void setDefaultConfiguration(CacheConfiguration defaultConfiguration) {
        this.defaultConfiguration = defaultConfiguration;
    }

    public boolean isAutocreate() {
        return autocreate;
    }

    public void setAutocreate(boolean autocreate) {
        this.autocreate = autocreate;
    }

    public CacheConfiguration getDefaultConfiguration() {
        return defaultConfiguration;
    }

    public Class<String> getDefaultKeyType() {
        return defaultKeyType;
    }

    public void setDefaultKeyType(Class<String> defaultKeyType) {
        this.defaultKeyType = defaultKeyType;
    }

    public Class<Object> getDefaultValueType() {
        return defaultValueType;
    }

    public void setDefaultValueType(Class<Object> defaultValueType) {
        this.defaultValueType = defaultValueType;
    }
}
