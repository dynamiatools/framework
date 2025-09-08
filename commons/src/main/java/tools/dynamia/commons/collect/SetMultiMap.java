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

import java.util.Map.Entry;
import java.util.Set;


/**
 * <p>
 * SetMultiMap is a specialized extension of {@link MultiMap} where each key is associated with a {@link Set} of values, ensuring uniqueness of values per key.
 * This interface provides convenient access to sets of values and supports typical multi-map operations such as retrieval, removal, and entry iteration.
 * </p>
 *
 * <p>
 * Use cases include grouping elements by key with no duplicate values, such as tags, roles, or categories.
 * </p>
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @author Mario A. Serrano Leones
 * @since 2023
 */
public interface SetMultiMap<K, V> extends MultiMap<K, V> {

    /**
     * Returns the set of values associated with the specified key.
     * If the key does not exist, returns an empty set (never null).
     *
     * @param key the key whose associated values are to be returned
     * @return a set of values associated with the key, or an empty set if none
     */
    @Override
    Set<V> get(K key);

    /**
     * Removes the mapping for the specified key from this multi-map if present.
     * Returns the set of values that were associated with the key, or an empty set if the key was not present.
     *
     * @param key the key whose mapping is to be removed
     * @return the set of values previously associated with the key, or an empty set if none
     */
    @Override
    Set<V> remove(K key);

    /**
     * Returns a set view of the mappings contained in this multi-map.
     * Each entry consists of a key and its associated set of values.
     *
     * @return a set of entries, where each entry maps a key to a set of values
     */
    Set<Entry<K, Set<V>>> entrySet();
}
