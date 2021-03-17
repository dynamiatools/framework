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

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;


/**
 * The Class ArrayListMultiMap.
 *
 * @author Mario A. Serrano Leones
 * @param <K> the key type
 * @param <V> the value type
 */
public class ArrayListMultiMap<K, V> implements ListMultiMap<K, V>, Serializable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 4476740655051507386L;

    /**
     * The delegate.
     */
    private final Map<K, List<V>> delegate = new HashMap<>();

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.ListMultiMap#get(java.lang.Object)
     */
    @Override
    public List<V> get(final K key) {
        return delegate.get(key);
    }

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.MultiMap#put(java.lang.Object, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public V put(final K key, final V value) {
        List<V> values = get(key);
        if (values == null) {
            values = new ArrayList<>();
            delegate.put(key, values);
        }
        if (value instanceof Collection) {
            final Collection<V> newValues = (Collection<V>) value;
            values.addAll(newValues);
        } else {
            values.add(value);
        }
        return value;
    }

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.MultiMap#put(java.lang.Object, java.lang.Object, java.lang.Object[])
     */
    @SafeVarargs
    @Override
    public final void put(final K key, final V value, final V... values) {
        put(key, value);
        for (V other : values) {
            put(key, other);
        }
    }

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.MultiMap#putAll(java.lang.Object, java.util.Collection)
     */
    @Override
    public void putAll(final K key, final Collection<V> newValues) {
        List<V> values = get(key);
        if (values == null) {
            values = new ArrayList<>();
            delegate.put(key, values);
        }
        values.addAll(newValues);
    }

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.MultiMap#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(final K key) {
        return delegate.containsKey(key);
    }

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.ListMultiMap#remove(java.lang.Object)
     */
    @Override
    public List<V> remove(final K key) {
        return delegate.remove(key);
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
    public K getKey(final V value) {
        K key = null;
        for (Entry<K, List<V>> entry : delegate.entrySet()) {
            if (entry.getValue().contains(value)) {
                key = entry.getKey();
                break;
            }
        }
        return key;
    }

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.ListMultiMap#entrySet()
     */
    @Override
    public Set<Entry<K, List<V>>> entrySet() {
        return delegate.entrySet();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }
}
