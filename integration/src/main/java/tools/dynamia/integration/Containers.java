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
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * This class works like a Facade for all kind of object containers like Spring,
 * SLI, etc.
 *
 * @author Mario A. Serrano Leones
 */
public class Containers {

    /**
     * The object containers.
     */
    private Set<ObjectContainer> objectContainers;

    /**
     * The instance.
     */
    private static Containers instance;

    private static final LoggingService logger = new SLF4JLoggingService(Containers.class);

    /**
     * Gets the.
     *
     * @return the containers
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
     * Find object.
     *
     * @param <T>  the generic type
     * @param name the name
     * @param type the type
     * @return the t
     */
    public <T> T findObject(String name, Class<T> type) {
        if (objectContainers == null) {
            return null;
        }
        for (ObjectContainer oc : objectContainers) {
            T object = oc.getObject(name, type);
            if (object != null) {
                return object;
            }
        }
        return null;
    }

    /**
     * Find object.
     *
     * @param name the name
     * @return the object
     */
    public Object findObject(String name) {
        if (objectContainers == null) {
            return null;
        }
        for (ObjectContainer oc : objectContainers) {
            Object object = oc.getObject(name);
            if (object != null) {
                return object;
            }
        }
        return null;
    }

    /**
     * Find object.
     *
     * @param <T>  the generic type
     * @param type the type
     * @return the t
     */
    public <T> T findObject(Class<T> type) {
        if (objectContainers == null) {
            return null;
        }
        for (ObjectContainer oc : objectContainers) {
            T object = oc.getObject(type);
            if (object != null) {
                return object;
            }
        }
        return null;
    }

    public <T> T findObject(Class<T> type, ObjectMatcher<T> matcher) {
        if (objectContainers == null) {
            return null;
        }
        return findObjects(type).stream().filter(matcher::match).findFirst().orElse(null);
    }

    /**
     * Find objects.
     *
     * @param <T>  the generic type
     * @param type the type
     * @return the collection
     */
    public <T> Collection<T> findObjects(Class<T> type) {
        return findObjects(type, null);
    }

    /**
     * Find objects.
     *
     * @param <T>     the generic type
     * @param type    the type
     * @param matcher the matcher
     * @return the collection
     */
    public <T> Collection<T> findObjects(Class<T> type, ObjectMatcher<T> matcher) {
        List<T> objects = new ArrayList<>();
        if (objectContainers != null && !objectContainers.isEmpty()) {
            for (ObjectContainer oc : objectContainers) {
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
     * Manually install a new ObjectContainer.
     *
     * @param obj the obj
     */
    public void installObjectContainer(ObjectContainer obj) {
        logger.info("Installing Object Container: " + obj + "  " + obj.getClass().toString());
        if (objectContainers == null) {
            objectContainers = new HashSet<>();
        }
        objectContainers.add(obj);
    }

    /**
     * Gets the installed containers.
     *
     * @return the installed containers
     */
    public Set<ObjectContainer> getInstalledContainers() {
        return objectContainers;
    }

    /**
     * Removes the all containers.
     */
    public void removeAllContainers() {
        if (objectContainers != null) {
            objectContainers.clear();
            objectContainers = null;
        }
    }

}
