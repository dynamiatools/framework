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
 * Very simply cache helper classes. Internally it use a @{@link ConcurrentHashMap} collection to store data. Data is stored in memory.
 *
 * @param <K>
 * @param <V>
 */
public class SimpleCache<K, V> implements Serializable {

    private final Map<K, V> data = new ConcurrentHashMap<>();

    /**
     * Add to Cache
     *
     */
    public void add(K key, V value) {
        data.put(key, value);
    }

    /**
     * Get value from cache
     *
     */
    public V get(K key) {
        return data.get(key);
    }

    /**
     * Get value from cache, if null loader function is called to obtain/create value
     *
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
     * Remove key from cache
     *
     */
    public V remove(K key) {
        return data.remove(key);
    }

    /**
     * Clear all cache
     */
    public void clear() {
        data.clear();
    }

    /**
     * Check if cache is empty
     *
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }

    public Set<K> keySet() {
        return data.keySet();
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return data.entrySet();
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        data.forEach(action);
    }

    @Override
    public String toString() {
        return super.toString() + "\n" + data;
    }
}
