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

/**
 * The Class CollectionWrapper.
 *
 * @author Mario A. Serrano Leones
 * @param <T> the generic type
 */
public class CollectionWrapper<T> {

    /**
     * The collection.
     */
    private Collection<T> collection;
    private String name;
    private String description;
    private Object value;
    private long id = System.nanoTime();

    /**
     * Instantiates a new collection wrapper.
     *
     * @param collection the collection
     */
    public CollectionWrapper(Collection<T> collection) {
        this.collection = collection;
    }

    /**
     * Gets the collection.
     *
     * @return the collection
     */
    public Collection<T> getCollection() {
        return collection;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
