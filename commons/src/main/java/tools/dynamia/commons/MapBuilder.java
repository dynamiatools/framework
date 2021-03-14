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
package tools.dynamia.commons;

import java.util.HashMap;
import java.util.Map;


/**
 * The Class MapBuilder.
 *
 * @author Mario A. Serrano Leones
 * @param <K> key
 * @param <V> value
 */
public class MapBuilder<K, V> {

    /**
     * The map.
     */
    private final Map<K, V> map = new HashMap<>();

    /**
     * Put.
     *
     * @param key the key
     * @param value the value
     * @return the map builder
     */
    public MapBuilder<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    /**
     * Builds the.
     *
     * @return the map
     */
    public Map<K, V> build() {
        return map;
    }

    /**
     * Put.
     *
     * @param key the key
     * @param value the value
     * @return the map
     */
    public static Map<String, Object> put(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    /**
     * Put.
     *
     * @param keyvalue the keyvalue
     * @return the map
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
}
