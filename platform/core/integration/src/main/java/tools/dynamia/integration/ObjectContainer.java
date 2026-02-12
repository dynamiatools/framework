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

import java.util.List;


/**
 * Interface for object containers that manage and provide access to objects, typically used for dependency injection or service lookup.
 * Implementations include {@link SpringObjectContainer} for Spring-based applications and {@link SimpleObjectContainer} for basic in-memory storage.
 *
 * @author Mario A. Serrano Leones
 */
public interface ObjectContainer {

    /**
     * Returns the name of this object container.
     *
     * @return the name of the container
     */
    String getName();

    /**
     * Retrieves an object by its name and type.
     *
     * @param <T> the type of the object
     * @param name the name of the object
     * @param type the class type of the object
     * @return the object instance, or null if not found
     */
    <T> T getObject(String name, Class<T> type);

    /**
     * Retrieves an object by its type. If multiple objects of the same type exist, the behavior is implementation-specific.
     *
     * @param <T> the type of the object
     * @param type the class type of the object
     * @return the object instance, or null if not found
     */
    <T> T getObject(Class<T> type);

    /**
     * Retrieves all objects of the specified type.
     *
     * @param <T> the type of the objects
     * @param type the class type of the objects
     * @return a list of objects of the specified type, or an empty list if none found
     */
    <T> List<T> getObjects(Class<T> type);

    /**
     * Retrieves an object by its name.
     *
     * @param name the name of the object
     * @return the object instance, or null if not found
     */
    Object getObject(String name);
}
