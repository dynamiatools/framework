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

/**
 * Interface for classes that provide dynamic properties as key-value pairs.
 * <p>
 * Implementing this interface allows objects to expose custom properties, typically for configuration,
 * metadata, extensibility, or dynamic behavior. Properties are represented as string key-value pairs.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * public class MyConfig implements PropertiesContainer {
 *     private final Map<String, String> properties = new HashMap<>();
 *     @Override
 *     public String getProperty(String name) {
 *         return properties.get(name);
 *     }
 *     @Override
 *     public void addProperty(String name, String value) {
 *         properties.put(name, value);
 *     }
 * }
 * }
 * </pre>
 *
 * @author Mario A. Serrano Leones
 */
public interface PropertiesContainer {

    /**
     * Returns the value of the property with the specified name.
     * <p>
     * If the property does not exist, returns {@code null}.
     *
     * @param name the property name
     * @return the property value, or {@code null} if not found
     */
    String getProperty(String name);

    /**
     * Adds or updates a property with the specified name and value.
     * <p>
     * If the property already exists, its value is updated.
     *
     * @param name the property name
     * @param value the property value
     */
    void addProperty(String name, String value);
}
