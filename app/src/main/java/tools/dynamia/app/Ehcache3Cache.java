package tools.dynamia.app;

import org.ehcache.Cache;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.lang.Nullable;

import java.util.concurrent.Callable;

/**
 * Basic Ehcache 3 cache delegator to {@link org.ehcache.Cache}
 */
public class Ehcache3Cache extends AbstractValueAdaptingCache {

    private final org.ehcache.Cache delegated;
    private final String name;

    public Ehcache3Cache(boolean allowNullValues, Cache delegated, String name) {
        super(allowNullValues);
        this.delegated = delegated;
        this.name = name;
    }

    public Ehcache3Cache(org.ehcache.Cache delegated, String name) {
        super(true);
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
    protected Object lookup(Object key) {
        return delegated.get(key);
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        var value = delegated.get(key);
        if (value == null) {
            try {
                value = toStoreValue(valueLoader.call());
            } catch (Exception e) {
                throw new ValueRetrievalException(key, valueLoader, e);
            }
        }
        return (T) fromStoreValue(value);

    }

    @Override
    public void put(Object key, @Nullable Object value) {
        this.delegated.put(key, toStoreValue(value));
    }

    @Override
    @Nullable
    public ValueWrapper putIfAbsent(Object key, @Nullable Object value) {
        Object existing = this.delegated.putIfAbsent(key, toStoreValue(value));
        return toValueWrapper(existing);
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
