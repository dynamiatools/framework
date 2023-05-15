package tools.dynamia.integration;

import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;

/**
 * Very simple CacheManager for Ehcache 3. Delegate all cache creation to native {@link org.ehcache.CacheManager}
 * By default keys type are String and values type are Object.class
 */
public class Ehcache3CacheManager implements CacheManager {

    private org.ehcache.CacheManager nativeCacheManager;
    private boolean autocreate = true;
    private CacheConfiguration defaultConfiguration;
    private Class<String> defaultKeyType = String.class;
    private Class<Object> defaultValueType = Object.class;
    private int defaultPoolHeapEntries = 100;

    public Ehcache3CacheManager() {
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
    public Cache getCache(String name) {
        var cache = getNativeCacheManager().getCache(name, defaultKeyType, defaultValueType);
        if (cache == null && autocreate) {
            cache = getNativeCacheManager().createCache(name, defaultConfiguration);
        }
        if (cache != null) {
            return new Ehcache3Cache(cache, name);
        } else {
            return null;
        }
    }

    @Override
    public Collection<String> getCacheNames() {
        return getNativeCacheManager().getRuntimeConfiguration().getCacheConfigurations()
                .keySet();
    }

    public org.ehcache.CacheManager getNativeCacheManager() {
        if (nativeCacheManager == null) {
            nativeCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                    .withCache("defaultCache",
                            CacheConfigurationBuilder.newCacheConfigurationBuilder(defaultKeyType, defaultValueType,
                                            ResourcePoolsBuilder.heap(100))
                                    .build())
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
