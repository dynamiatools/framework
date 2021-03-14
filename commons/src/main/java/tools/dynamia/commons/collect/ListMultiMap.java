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

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;


/**
 * The Interface ListMultiMap.
 *
 * @author Mario A. Serrano Leones
 * @param <K> the key type
 * @param <V> the value type
 */
public interface ListMultiMap<K, V> extends MultiMap<K, V> {

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.MultiMap#get(java.lang.Object)
     */
    @Override
    List<V> get(K key);

    /* (non-Javadoc)
     * @see tools.dynamia.commons.collect.MultiMap#remove(java.lang.Object)
     */
    @Override
    List<V> remove(K key);

    /**
     * Entry set.
     *
     * @return the sets the
     */
    Set<Entry<K, List<V>>> entrySet();
}
