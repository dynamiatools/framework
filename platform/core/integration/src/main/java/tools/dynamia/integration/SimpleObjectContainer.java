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
package tools.dynamia.integration;

import tools.dynamia.commons.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A simple implementation of {@link ObjectContainer} that stores objects in a concurrent map.
 * This container provides thread-safe operations for storing and retrieving objects by name or type.
 * Objects are stored with a unique name, and can be retrieved individually or as a list by type.
 */
public class SimpleObjectContainer implements ObjectContainer {

    /**
     * The objects map, storing object instances as keys and their names as values.
     */
    private final Map<String, Object> objects = new ConcurrentHashMap<>();

    private final String name;

    /**
     * Instantiates a new simple object container with a randomly generated name.
     */
    public SimpleObjectContainer() {
        name = StringUtils.randomString();
    }

    /**
     * Instantiates a new simple object container with the specified name.
     *
     * @param name the name of the container
     */
    public SimpleObjectContainer(String name) {
        this.name = name;
    }

    /**
     * Returns the name of this container.
     *
     * @return the container name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Retrieves an object by its name and type.
     * Searches through all objects and returns the one with the matching name and type.
     *
     * @param name the name of the object to retrieve
     * @param type the class type of the object
     * @param <T>  the type of the object
     * @return the object if found, or null if not found
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getObject(String name, Class<T> type) {
        if (name == null || type == null) {
            return null;
        }
        for (Map.Entry<String, Object> entry : objects.entrySet()) {
            if (name.equals(entry.getKey()) && type.isAssignableFrom(entry.getValue().getClass())) {
                return (T) entry.getValue();
            }
        }
        return null;
    }

    /**
     * Retrieves the first object of the specified type.
     * If multiple objects of the same type exist, returns the first one found.
     *
     * @param type the class type of the object
     * @param <T>  the type of the object
     * @return the first object of the specified type, or null if none found
     */
    @Override
    public <T> T getObject(Class<T> type) {
        if (type == null) {
            return null;
        }
        List<T> r = getObjects(type);
        if (!r.isEmpty()) {
            return r.getFirst();
        } else {
            return null;
        }
    }

    /**
     * Retrieves all objects of the specified type.
     * Returns a list of all objects that are assignable to the given type.
     *
     * @param type the class type of the objects
     * @param <T>  the type of the objects
     * @return a list of objects of the specified type, or an empty list if none found
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> getObjects(Class<T> type) {
        if (type == null) {
            return new ArrayList<>();
        }
        List<T> r = new ArrayList<>();
        if (!objects.isEmpty()) {
            for (Object obj : objects.values()) {
                if (type.isAssignableFrom(obj.getClass())) {
                    r.add((T) obj);
                }
            }
        }
        return r;
    }

    /**
     * Retrieves an object by its name.
     *
     * @param name the name of the object
     * @return the object if found, or null if not found
     */
    @Override
    public Object getObject(String name) {
        if (name == null) {
            return null;
        }
        return objects.get(name);
    }

    /**
     * Adds an object to the container with the specified name.
     * If an object with the same name already exists, it will be replaced.
     *
     * @param name   the name to associate with the object
     * @param object the object to add
     * @throws IllegalArgumentException if name or object is null
     */
    public void addObject(String name, Object object) {
        if (name == null || object == null) {
            throw new IllegalArgumentException("Name and object cannot be null");
        }
        objects.put(name, object);
    }

    /**
     * Adds an object to the container using its class simple name as the key.
     * If an object with the same name already exists, it will be replaced.
     *
     * @param object the object to add
     * @throws IllegalArgumentException if object is null
     */
    public void addObject(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }
        addObject(object.getClass().getSimpleName(), object);
    }

    /**
     * Removes an object from the container by its name.
     *
     * @param name the name of the object to remove
     * @return true if an object was removed, false otherwise
     */
    public boolean removeObject(String name) {
        if (name == null) {
            return false;
        }
        return objects.remove(name) != null;
    }

    /**
     * Removes the specified object from the container.
     *
     * @param object the object to remove
     * @return true if the object was removed, false otherwise
     */
    public boolean removeObject(Object object) {
        if (object == null) {
            return false;
        }
        var keyToRemove = objects.entrySet().stream()
                .filter(e -> e.getValue().equals(object))
                .map(Map.Entry::getKey)
                .findFirst();

        if (keyToRemove.isPresent()) {
            objects.remove(keyToRemove.get());
            return true;
        }
        return false;
    }

    /**
     * Checks if the container contains an object with the specified name.
     *
     * @param name the name to check
     * @return true if an object with the name exists, false otherwise
     */
    public boolean containsObject(String name) {
        if (name == null) {
            return false;
        }
        return objects.containsValue(name);
    }

    /**
     * Checks if the container contains the specified object.
     *
     * @param object the object to check
     * @return true if the object is in the container, false otherwise
     */
    public boolean containsObject(Object object) {
        if (object == null) {
            return false;
        }
        return objects.containsKey(object);
    }

    /**
     * Returns the number of objects in the container.
     *
     * @return the number of objects
     */
    public int getObjectsCount() {
        return objects.size();
    }

    /**
     * Checks if the container is empty.
     *
     * @return true if the container contains no objects, false otherwise
     */
    public boolean isEmpty() {
        return objects.isEmpty();
    }

    /**
     * Clears all objects from the container.
     */
    public void clear() {
        objects.clear();
    }

    /**
     * Returns the name of the container as a string representation.
     *
     * @return the container name
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Compares this container to another object for equality.
     * Two containers are equal if they have the same name.
     *
     * @param obj the object to compare
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SimpleObjectContainer that = (SimpleObjectContainer) obj;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    /**
     * Returns the hash code for this container.
     * The hash code is based on the container name.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public Map<String, Object> getObjects() {
        return objects;
    }
}
