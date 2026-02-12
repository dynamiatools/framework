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

import java.util.Map;

/**
 * Interface to allow POJO classes to be converted to a {@link Map} representation.
 * <p>
 * Implementing this interface enables automatic conversion of an object's properties to a map, typically for serialization,
 * logging, debugging, or dynamic property access. The default implementation uses {@link ObjectOperations#getValuesMaps(Object)}
 * to extract all standard properties and their values.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * public class Person implements Mappable {
 *     private String name;
 *     private int age;
 *     // getters and setters
 * }
 * // ...
 * Person p = new Person();
 * Map<String, Object> map = p.toMap();
 * }
 * </pre>
 *
 * @author Ing. Mario Serrano
 */
public interface Mappable {

    /**
     * Converts the current object to a {@link Map} of property names and values.
     * <p>
     * Uses {@link ObjectOperations#getValuesMaps(Object)} to extract all standard properties.
     *
     * @return a map containing property names as keys and their corresponding values
     */
    default Map<String, Object> toMap() {
        return ObjectOperations.getValuesMaps(this);
    }

}
