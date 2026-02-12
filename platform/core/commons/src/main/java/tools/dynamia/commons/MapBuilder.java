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
import java.util.HashMap;
import java.util.Map;


/**
 * Utility class for building and creating {@link Map} instances in a fluent and convenient way.
 * <p>
 * The {@code MapBuilder} class provides methods to construct maps with key-value pairs, supporting both generic and static usage for common scenarios.
 * <p>
 * Typical use cases include simplifying map creation for configuration, parameters, or data transfer objects.
 * <p>
 * Example usage:
 * <pre>
 *     Map<String, Object> map = MapBuilder.put("key1", "value1", "key2", 123);
 *     Map<String, Serializable> serializableMap = MapBuilder.serializable("key1", "value1");
 *     Map<String, Object> singleMap = MapBuilder.put("key", "value");
 *     Map<String, Object> fluentMap = new MapBuilder<String, Object>().put("a", 1).put("b", 2).build();
 * </pre>
 * <p>
 * Thread safety: This class is not thread-safe.
 *
 * @param <K> the type of keys maintained by the map
 * @param <V> the type of mapped values
 * @author Mario A. Serrano Leones
 */
public class MapBuilder<K, V> {

    /**
     * Internal map instance used for building.
     */
    private final Map<K, V> map = new HashMap<>();

    /**
     * Adds a key-value pair to the builder's map.
     * <p>
     * Allows fluent chaining of multiple put operations.
     *
     * @param key   the key to add
     * @param value the value to associate with the key
     * @return this {@code MapBuilder} instance for chaining
     */
    public MapBuilder<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    /**
     * Returns the built map containing all key-value pairs added.
     * <p>
     * The returned map is the internal {@link HashMap} instance used by the builder.
     *
     * @return the constructed map
     */
    public Map<K, V> build() {
        return map;
    }

    /**
     * Creates a new map with a single key-value pair.
     * <p>
     * Useful for quick map creation without using the builder instance.
     *
     * @param key   the key to add
     * @param value the value to associate with the key
     * @return a new map containing the specified key-value pair
     */
    public static Map<String, Object> put(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    /**
     * Creates a new map with a single key-value pair, where the value is {@link Serializable}.
     * <p>
     * Useful for scenarios requiring serializable map values.
     *
     * @param key   the key to add
     * @param value the serializable value to associate with the key
     * @return a new map containing the specified key-value pair
     */
    public static Map<String, Serializable> serializable(String key, Serializable value) {
        Map<String, Serializable> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    /**
     * Creates a new map from a sequence of key-value pairs.
     * <p>
     * The arguments must be provided in pairs: key1, value1, key2, value2, ...
     * Keys are converted to strings using {@code toString()}.
     *
     * @param keyvalue an even-length sequence of key-value pairs
     * @return a new map containing the specified pairs
     * @throws RuntimeException if the number of arguments is not even
     */
    public static Map<String, Object> put(Object... keyvalue) {
        if (keyvalue.length % 2 != 0) {
            throw new RuntimeException("Invalid number of key values pair");
        }
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keyvalue.length; i = i + 2) {
            map.put(keyvalue[i].toString(), keyvalue[i + 1]);
        }
        return map;
    }

    /**
     * Creates a new map from a sequence of key-value pairs, where values are {@link Serializable}.
     * <p>
     * The arguments must be provided in pairs: key1, value1, key2, value2, ...
     * Keys are converted to strings using {@code toString()}.
     *
     * @param keyvalue an even-length sequence of serializable key-value pairs
     * @return a new map containing the specified pairs
     * @throws RuntimeException if the number of arguments is not even
     */
    public static Map<String, Serializable> serializable(Serializable... keyvalue) {
        if (keyvalue.length % 2 != 0) {
            throw new RuntimeException("Invalid number of key values pair");
        }
        Map<String, Serializable> map = new HashMap<>();
        for (int i = 0; i < keyvalue.length; i = i + 2) {
            map.put(keyvalue[i].toString(), keyvalue[i + 1]);
        }
        return map;
    }

    /**
     * Checks if the builder's map contains the specified key.
     *
     * @param key the key to check
     * @return true if the key exists, false otherwise
     */
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    /**
     * Checks if the builder's map contains the specified value.
     *
     * @param value the value to check
     * @return true if the value exists, false otherwise
     */
    public boolean containsValue(V value) {
        return map.containsValue(value);
    }

    /**
     * Removes the specified key from the builder's map.
     *
     * @param key the key to remove
     * @return the previous value associated with the key, or null if not present
     */
    public V remove(K key) {
        return map.remove(key);
    }

    /**
     * Returns the number of key-value pairs in the builder's map.
     *
     * @return the size of the map
     */
    public int size() {
        return map.size();
    }

    /**
     * Removes all key-value pairs from the builder's map.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Checks if the builder's map is empty.
     *
     * @return true if the map is empty, false otherwise
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Adds all key-value pairs from another map to the builder's map.
     *
     * @param other the map whose pairs are to be added
     * @return this {@code MapBuilder} instance for chaining
     */
    public MapBuilder<K, V> putAll(Map<? extends K, ? extends V> other) {
        map.putAll(other);
        return this;
    }

    /**
     * Returns an empty map.
     *
     * @return an empty map
     */
    public static <K, V> Map<K, V> empty() {
        return new HashMap<>();
    }

    /**
     * Creates a map with a single key-value pair (generic version).
     *
     * @param key the key to add
     * @param value the value to associate with the key
     * @return a new map containing the specified key-value pair
     */
    public static <K, V> Map<K, V> of(K key, V value) {
        Map<K, V> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    /**
     * Returns a copy of the given map.
     *
     * @param original the map to copy
     * @return a new map containing the same key-value pairs as the original
     */
    public static <K, V> Map<K, V> copy(Map<K, V> original) {
        return new HashMap<>(original);
    }
}
