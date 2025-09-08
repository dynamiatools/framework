/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tools.dynamia.commons;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Simple in-memory cache implementation using {@link ConcurrentHashMap}.
 * <p>
 * Provides thread-safe methods to add, retrieve, remove, and clear cached values. Supports lazy loading via loader functions.
 * Useful for caching frequently accessed data in applications.
 * <p>
 * All methods are stateless and thread-safe.
 *
 * @param <K> the type of cache key
 * @param <V> the type of cache value
 * @author Mario A. Serrano Leones
 */
public class SimpleCache<K, V> implements Serializable {

    /**
     * Internal data storage for cache entries.
     */
    private final Map<K, V> data = new ConcurrentHashMap<>();

    /**
     * Adds a value to the cache for the specified key.
     *
     * @param key the cache key
     * @param value the value to cache
     */
    public void add(K key, V value) {
        data.put(key, value);
    }

    /**
     * Retrieves a value from the cache by key.
     *
     * @param key the cache key
     * @return the cached value, or null if not present
     */
    public V get(K key) {
        return data.get(key);
    }

    /**
     * Retrieves a value from the cache by key, loading it with the provided function if not present.
     *
     * @param key the cache key
     * @param loaderFunction the function to load the value if not present
     * @return the cached or loaded value
     */
    public V getOrLoad(K key, Function<K, V> loaderFunction) {
        V value = get(key);
        if (value == null) {
            value = loaderFunction.apply(key);
            if (value != null) {
                add(key, value);
            }
        }
        return value;
    }

    /**
     * Removes a value from the cache by key.
     *
     * @param key the cache key
     * @return the removed value, or null if not present
     */
    public V remove(K key) {
        return data.remove(key);
    }

    /**
     * Clears all entries from the cache.
     */
    public void clear() {
        data.clear();
    }

    /**
     * Checks if the cache is empty.
     *
     * @return true if the cache is empty, false otherwise
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * Returns the set of all cache keys.
     *
     * @return the set of cache keys
     */
    public Set<K> keySet() {
        return data.keySet();
    }

    /**
     * Returns the set of all cache entries.
     *
     * @return the set of cache entries
     */
    public Set<Map.Entry<K, V>> entrySet() {
        return data.entrySet();
    }

    /**
     * Performs the given action for each cache entry.
     *
     * @param action the action to perform
     */
    public void forEach(BiConsumer<? super K, ? super V> action) {
        data.forEach(action);
    }
}
