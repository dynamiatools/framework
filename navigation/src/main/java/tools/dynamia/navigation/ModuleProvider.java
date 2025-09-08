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
package tools.dynamia.navigation;

/**
 * <p>
 * The <b>ModuleProvider</b> interface is a functional contract for creating and supplying navigation modules in a modular application. Implementations of this interface are responsible for instantiating and returning a {@link Module} instance, which represents a logical unit or feature set within the application's navigation structure.
 * </p>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * // Example implementation using a lambda expression
 * ModuleProvider provider = () -> new Module("Books", "books", ...);
 * Module booksModule = provider.getModule();
 * </pre>
 *
 * <h2>Typical Scenarios</h2>
 * <ul>
 *   <li>Dynamic module registration in navigation frameworks</li>
 *   <li>Custom module creation for plugin architectures</li>
 *   <li>Testing navigation logic by providing mock modules</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * Implementations should ensure thread safety if modules are created or accessed concurrently.
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
@FunctionalInterface
public interface ModuleProvider {

    /**
     * Creates and returns a new {@link Module} instance.
     * <p>
     * This method is called by navigation frameworks or consumers to obtain a module for registration or usage.
     * Implementations may return different module instances on each invocation, or reuse a single instance as needed.
     * </p>
     *
     * <h3>Example</h3>
     * <pre>
     * ModuleProvider provider = () -> new Module("Users", "users", ...);
     * Module usersModule = provider.getModule();
     * </pre>
     *
     * @return a new or existing {@link Module} representing a navigation unit
     */
    Module getModule();
}
