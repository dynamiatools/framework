/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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
package tools.dynamia.commons.collect;

import java.util.*;
import java.util.Map.Entry;


/**
 * The Class HashSetMultiMap.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @author Mario A. Serrano Leones
 */
public class HashSetMultiMap<K, V> implements SetMultiMap<K, V> {

    /**
     * The delegate.
     */
    private final Map<K, Set<V>> delegate = new HashMap<>();

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.SetMultiMap#get(java.lang.Object)
     */
    @Override
    public Set<V> get(final K key) {
        return delegate.get(key);
    }

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.SetMultiMap#remove(java.lang.Object)
     */
    @Override
    public Set<V> remove(final K key) {
        return delegate.remove(key);
    }

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.SetMultiMap#entrySet()
     */
    @Override
    public Set<Entry<K, Set<V>>> entrySet() {
        return delegate.entrySet();
    }

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.MultiMap#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public V put(final K key, final V value) {
        Set<V> set = get(key);
        if (set == null) {
            set = new HashSet<>();
            delegate.put(key, set);
        }
        set.add(value);
        return value;
    }

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.MultiMap#put(java.lang.Object, java.lang.Object, java.lang.Object[])
     */
    @SafeVarargs
    @Override
    public final void put(final K key, final V value, V... values) {
        put(key, value);
        for (V newValue : values) {
            put(key, newValue);
        }
    }

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.MultiMap#putAll(java.lang.Object, java.util.Collection)
     */
    @Override
    public void putAll(K key, Collection<V> values) {
        for (V newValue : values) {
            put(key, newValue);
        }
    }

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.MultiMap#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(K key) {
        return delegate.containsKey(key);
    }

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.MultiMap#clear()
     */
    @Override
    public void clear() {
        delegate.clear();
    }

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.MultiMap#keySet()
     */
    @Override
    public Set<K> keySet() {
        return delegate.keySet();
    }

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.MultiMap#getKey(java.lang.Object)
     */
    @Override
    public K getKey(V value) {
        K key = null;
        for (Entry<K, Set<V>> entry : delegate.entrySet()) {
            if (entry.getValue().contains(value)) {
                key = entry.getKey();
            }
        }
        return key;
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }
}
