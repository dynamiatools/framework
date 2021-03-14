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

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * The Interface MultiMap.
 *
 * @author Mario A. Serrano Leones
 * @param <K> the key type
 * @param <V> the value type
 */
public interface MultiMap<K, V> {

    /**
     * Put.
     *
     * @param key the key
     * @param value the value
     * @return the v
     */
    V put(K key, V value);

    /**
     * Put.
     *
     * @param key the key
     * @param value the value
     * @param values the values
     */
    void put(K key, V value, V... values);

    /**
     * Put all.
     *
     * @param key the key
     * @param values the values
     */
    void putAll(K key, Collection<V> values);

    /**
     * Gets the.
     *
     * @param key the key
     * @return the collection
     */
    Collection<V> get(K key);

    /**
     * Contains key.
     *
     * @param key the key
     * @return true, if successful
     */
    boolean containsKey(K key);

    /**
     * Removes the.
     *
     * @param key the key
     * @return the collection
     */
    Collection<V> remove(K key);

    /**
     * Clear.
     */
    void clear();

    /**
     * Key set.
     *
     * @return the sets the
     */
    Set<K> keySet();

    /**
     * Gets the key.
     *
     * @param value the value
     * @return the key
     */
    K getKey(V value);

    /**
     * Check is the multimpa is empty
     * @return
     */
    boolean isEmpty();

    /**
     * Iterate over keys and collections
     *
     * @param action
     */
    default void forEach(BiConsumer<? super K, ? super Collection<V>> action) {
        Objects.requireNonNull(action);
        for (K key : keySet()) {
            Collection<V> v;
            try {
                v = get(key);
            } catch (IllegalStateException ise) {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }
            action.accept(key, v);
        }
    }

}
