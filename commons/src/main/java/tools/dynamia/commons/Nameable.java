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
 * Interface for objects that have a name or can provide a name representation.
 * <p>
 * Implementing this interface allows classes to expose a human-readable name, which can be used for display, logging,
 * serialization, or identification purposes. The default implementation returns the result of {@code toString()}.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * public class Product implements Nameable {
 *     private String name;
 *     @Override
 *     public String toName() {
 *         return name;
 *     }
 * }
 * }
 * </pre>
 *
 * @author Mario A. Serrano Leones
 */
public interface Nameable {

    /**
     * Returns the name representation of this object.
     * <p>
     * By default, returns the result of {@code toString()}.
     * Override this method to provide a custom name.
     *
     * @return the name of this object
     */
    default String toName() {
        return toString();
    }

    /**
     * Sets the name of this object.
     * <p>
     * Default implementation does nothing. Override to provide custom logic for setting the name.
     *
     * @param name the name to set
     */
    default void name(String name) {

    }
}
