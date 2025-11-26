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

import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This class acts as a facade for managing various object containers, such as those for Spring, SLI, and others.
 * It provides a unified interface to find and retrieve objects from registered containers.
 *
 * @author Mario A. Serrano Leones
 */
public class Containers {

    /**
     * The object containers.
     */
    private Map<String, ObjectContainer> objectContainers = new ConcurrentHashMap<>();

    /**
     * The instance.
     */
    private static Containers instance;

    private static final LoggingService logger = new SLF4JLoggingService(Containers.class);

    /**
     * Returns the singleton instance of the Containers facade.
     *
     * @return the singleton instance
     */
    public static Containers get() {
        if (instance == null) {
            instance = new Containers();
        }
        return instance;
    }

    /**
     * Instantiates a new containers.
     */
    private Containers() {
        //empty
    }

    /**
     * Searches for an object by name and type across all registered containers.
     * Returns the first match found, or null if no matching object is found.
     *
     * @param <T> the type of the object
     * @param name the name of the object
     * @param type the class type of the object
     * @return the object instance, or null if not found
     */
    public <T> T findObject(String name, Class<T> type) {
        if (objectContainers == null || objectContainers.isEmpty()) {
            return null;
        }
        for (ObjectContainer oc : objectContainers.values()) {
            T object = oc.getObject(name, type);
            if (object != null) {
                return object;
            }
        }
        return null;
    }

    /**
     * Searches for an object by name across all registered containers.
     * Returns the first match found, or null if no matching object is found.
     *
     * @param name the name of the object
     * @return the object instance, or null if not found
     */
    public Object findObject(String name) {
        if (objectContainers == null) {
            return null;
        }
        for (ObjectContainer oc : objectContainers.values()) {
            Object object = oc.getObject(name);
            if (object != null) {
                return object;
            }
        }
        return null;
    }

    /**
     * Searches for an object by type across all registered containers.
     * Returns the first match found, or null if no matching object is found.
     * If multiple objects of the same type exist, the behavior is implementation-specific.
     *
     * @param <T> the type of the object
     * @param type the class type of the object
     * @return the object instance, or null if not found
     */
    public <T> T findObject(Class<T> type) {
        if (objectContainers == null) {
            return null;
        }
        for (ObjectContainer oc : objectContainers.values()) {
            T object = oc.getObject(type);
            if (object != null) {
                return object;
            }
        }
        return null;
    }

    /**
     * Searches for an object by type and applies a matcher to filter results.
     * Returns the first matching object, or null if none.
     *
     * @param <T> the type of the object
     * @param type the class type of the object
     * @param matcher the matcher to filter objects
     * @return the first matching object, or null
     */
    public <T> T findObject(Class<T> type, ObjectMatcher<T> matcher) {
        if (objectContainers == null) {
            return null;
        }
        return findObjects(type).stream().filter(matcher::match).findFirst().orElse(null);
    }

    /**
     * Finds all objects of the specified type across all registered containers.
     *
     * @param <T> the type of the objects
     * @param type the class type of the objects
     * @return a collection of objects of the specified type
     */
    public <T> Collection<T> findObjects(Class<T> type) {
        return findObjects(type, null);
    }

    /**
     * Finds all objects of the specified type across all registered containers and applies a matcher to filter them.
     *
     * @param <T> the type of the objects
     * @param type the class type of the objects
     * @param matcher the matcher to filter objects, or null to include all
     * @return a collection of matching objects
     */
    public <T> Collection<T> findObjects(Class<T> type, ObjectMatcher<T> matcher) {
        List<T> objects = new ArrayList<>();
        if (objectContainers != null && !objectContainers.isEmpty()) {
            for (ObjectContainer oc : objectContainers.values()) {
                List<T> result = oc.getObjects(type);
                if (!result.isEmpty()) {
                    objects.addAll(result);
                }
            }
        }
        if (matcher != null) {
            objects = objects.stream().filter(matcher::match).toList();
        }
        return objects;
    }


    /**
     * Manually installs a new ObjectContainer into the facade.
     *
     * @param obj the ObjectContainer to install
     */
    public void installObjectContainer(ObjectContainer obj) {
        logger.info("Installing Object Container: " + obj.getName() + "  = " + obj.getClass());
        objectContainers.put(obj.getName(), obj);
    }

    /**
     * Returns a collection of all installed containers.
     *
     * @return the installed containers
     */
    public Collection<ObjectContainer> getInstalledContainers() {
        return objectContainers.values();
    }

    /**
     * Removes all installed containers from the facade.
     */
    public void removeAllContainers() {
        objectContainers.clear();
    }

    /**
     * Removes an ObjectContainer by its name.
     *
     * @param name the name of the container to remove
     * @return the removed ObjectContainer, or null if not found
     */
    public ObjectContainer removeContainer(String name) {
        return objectContainers.remove(name);
    }

    /**
     * Retrieves an ObjectContainer by its name.
     *
     * @param name the name of the container
     * @return the ObjectContainer, or null if not found
     */
    public ObjectContainer getContainer(String name) {
        return objectContainers.get(name);
    }

}
