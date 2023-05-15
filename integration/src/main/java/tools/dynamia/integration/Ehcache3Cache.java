package tools.dynamia.integration;

import org.springframework.cache.Cache;

import java.util.concurrent.Callable;

/**
 * Basic Ehcache 3 cache delegator to {@link org.ehcache.Cache}
 */
public class Ehcache3Cache implements Cache {

    private final org.ehcache.Cache delegated;
    private final String name;

    public Ehcache3Cache(org.ehcache.Cache delegated, String name) {
        this.delegated = delegated;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return delegated;
    }

    @Override
    public ValueWrapper get(Object key) {
        return () -> delegated.get(key);
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        return (T) delegated.get(key);
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        Object value = get(key);
        if (value == null) {
            try {
                value = valueLoader.call();
                put(key, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return (T) value;
    }

    @Override
    public void put(Object key, Object value) {
        delegated.put(key, value);
    }

    @Override
    public void evict(Object key) {
        delegated.remove(key);
    }

    @Override
    public void clear() {
        delegated.clear();
    }
}
