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
 * Interface for classes that can generate and handle URL strings.
 * <p>
 * Implement this interface if your class can provide a URL representation of its state or identity.
 * <p>
 * Typical usage:
 * <pre>
 *     public class MyResource implements URLable {
 *         @Override
 *         public String toURL() {
 *             return "https://example.com/resource/" + getId();
 *         }
 *     }
 * </pre>
 * <p>
 * This interface is useful for web applications, RESTful services, or any context where objects need to be referenced or accessed via URLs.
 */
public interface URLable {

    /**
     * Returns a URL string representation of this object.
     * <p>
     * Implementations should generate a valid URL that uniquely identifies or locates the object.
     *
     * @return a URL string representing this object
     */
    String toURL();

    /**
     * Sets or updates the URL for this object.
     * <p>
     * Default implementation does nothing. Override if your class needs to handle or store a URL value.
     *
     * @param url the URL string to set
     */
    default void url(String url) {
        // Default: no-op. Override to handle URL assignment if needed.
    }
}
