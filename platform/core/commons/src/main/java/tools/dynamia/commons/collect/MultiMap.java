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
package tools.dynamia.commons.collect;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * <p>
 * MultiMap is a generic interface representing a mapping from keys to multiple values. Unlike a standard {@link java.util.Map},
 * each key in a MultiMap can be associated with a collection of values, allowing efficient grouping and retrieval of related data.
 * </p>
 *
 * <p>
 * Typical use cases include grouping items by category, indexing, or representing relationships where a key may have multiple associated values.
 * </p>
 *
 * <p>
 * Implementations may vary in the type of collection used for values (e.g., {@link java.util.List}, {@link java.util.Set}).
 * </p>
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @author Mario A. Serrano Leones
 * @since 2023
 */
public interface MultiMap<K, V> {

    /**
     * Associates the specified value with the specified key in this multi-map.
     * If the key already exists, the value is added to its collection.
     *
     * @param key the key with which the specified value is to be associated
     * @param value the value to be associated with the key
     * @return the value added
     */
    V put(K key, V value);

    /**
     * Associates multiple values with the specified key in this multi-map.
     * All provided values are added to the collection for the key.
     *
     * @param key the key with which the specified values are to be associated
     * @param value the first value to be associated
     * @param values additional values to be associated
     */
    void put(K key, V value, V... values);

    /**
     * Associates all values in the given collection with the specified key.
     *
     * @param key the key with which the specified values are to be associated
     * @param values the collection of values to be associated
     */
    void putAll(K key, Collection<V> values);

    /**
     * Returns the collection of values associated with the specified key.
     * If the key does not exist, returns an empty collection (never null).
     *
     * @param key the key whose associated values are to be returned
     * @return the collection of values associated with the key, or an empty collection if none
     */
    Collection<V> get(K key);

    /**
     * Returns true if this multi-map contains a mapping for the specified key.
     *
     * @param key the key whose presence is to be tested
     * @return true if this multi-map contains a mapping for the key
     */
    boolean containsKey(K key);

    /**
     * Removes the mapping for the specified key from this multi-map if present.
     * Returns the collection of values that were associated with the key, or an empty collection if the key was not present.
     *
     * @param key the key whose mapping is to be removed
     * @return the collection of values previously associated with the key, or an empty collection if none
     */
    Collection<V> remove(K key);

    /**
     * Removes all mappings from this multi-map, leaving it empty.
     */
    void clear();

    /**
     * Returns a set view of the keys contained in this multi-map.
     *
     * @return a set of the keys contained in this multi-map
     */
    Set<K> keySet();

    /**
     * Returns the key associated with the specified value, or null if not found.
     * If multiple keys map to the value, the implementation may return any one of them.
     *
     * @param value the value whose associated key is to be returned
     * @return the key associated with the value, or null if not found
     */
    K getKey(V value);

    /**
     * Returns true if this multi-map contains no key-value mappings.
     *
     * @return true if this multi-map contains no key-value mappings
     */
    boolean isEmpty();

    /**
     * Performs the given action for each key and its associated collection of values in this multi-map.
     *
     * @param action the action to be performed for each entry
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
