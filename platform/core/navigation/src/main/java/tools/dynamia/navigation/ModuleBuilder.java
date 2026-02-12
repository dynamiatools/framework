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
 * The <b>ModuleBuilder</b> interface defines a contract for building {@link Module} instances in a modular navigation system. Implementations of this interface are responsible for constructing and configuring modules, which represent logical units or feature sets in an application's navigation structure.
 * </p>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * // Example implementation using a lambda expression
 * ModuleBuilder builder = () -> {
 *     Module module = new Module("books", "Books", "Manage your book collection");
 *     module.addPageGroup(new PageGroup("main", "Main Group"));
 *     module.addPage(new Page("list", "Book List"));
 *     return module;
 * };
 * Module booksModule = builder.build();
 * </pre>
 *
 * <h2>Typical Scenarios</h2>
 * <ul>
 *   <li>Dynamic module creation for plugin architectures</li>
 *   <li>Custom configuration of modules in navigation frameworks</li>
 *   <li>Testing navigation logic by providing mock modules</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * Implementations should ensure thread safety if modules are built or accessed concurrently.
 * </p>
 * <p>
 * See  {@link JavaModuleBuilder}
 *
 * @author Mario A. Serrano Leones
 */
public interface ModuleBuilder {

    /**
     * Builds and returns a new {@link Module} instance.
     * <p>
     * This method is called by navigation frameworks or consumers to obtain a fully constructed module for registration or usage.
     * Implementations may return different module instances on each invocation, or reuse a single instance as needed.
     * </p>
     *
     * <h3>Example</h3>
     * <pre>
     * ModuleBuilder builder = () -> new Module("users", "Users", "User management");
     * Module usersModule = builder.build();
     * </pre>
     *
     * @return a new or existing {@link Module} representing a navigation unit
     */
    Module build();
}
