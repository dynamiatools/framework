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
 * The Interface ObjectContainer.
 *
 * @author Mario A. Serrano Leones
 */
public interface ObjectContainer {

    /**
     * Gets the object.
     *
     * @param <T> the generic type
     * @param name the name
     * @param type the type
     * @return the object
     */
    <T> T getObject(String name, Class<T> type);

    /**
     * Gets the object.
     *
     * @param <T> the generic type
     * @param type the type
     * @return the object
     */
    <T> T getObject(Class<T> type);

    /**
     * Gets the objects.
     *
     * @param <T> the generic type
     * @param type the type
     * @return the objects
     */
    <T> List<T> getObjects(Class<T> type);

    /**
     * Gets the object.
     *
     * @param name the name
     * @return the object
     */
    Object getObject(String name);
}
