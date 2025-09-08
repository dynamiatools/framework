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
 * JavaModuleBuilder is a fluent builder class for constructing {@link Module} instances in a modular navigation system. It provides a convenient API to add groups and pages, set module properties, and configure navigation structure programmatically using Java code.
 * </p>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * JavaModuleBuilder builder = new JavaModuleBuilder("books", "Books", "fa-book", 1);
 * builder.addGroup("main", "Main Group")
 *        .addPage("list", "Book List", "/books/list", "fa-list", 1)
 *        .addPage("detail", "Book Detail", "/books/detail", "fa-info", 2);
 * Module booksModule = builder.build();
 * </pre>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Fluent API for adding groups and pages</li>
 *   <li>Supports setting module icon and position</li>
 *   <li>Allows chaining of group and page additions</li>
 *   <li>Implements {@link ModuleBuilder} for integration with navigation frameworks</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * This class is not thread-safe. If used in concurrent environments, external synchronization is required.
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
public class JavaModuleBuilder implements ModuleBuilder {

    private final Module module;
    private PageGroup currentGroup;

    /**
     * Creates a builder with the given module name (used as both id and name).
     *
     * @param name the module name and id
     */
    public JavaModuleBuilder(String name) {
        this(name, name);
    }

    /**
     * Creates a builder with the given module id and name.
     *
     * @param id the module id
     * @param name the module name
     */
    public JavaModuleBuilder(String id, String name) {
        this(id, name, null);
    }

    /**
     * Creates a builder with the given module id, name, and icon.
     *
     * @param id the module id
     * @param name the module name
     * @param icon the module icon
     */
    public JavaModuleBuilder(String id, String name, String icon) {
        this(id, name, icon, 0);
    }

    /**
     * Creates a builder with the given module id, name, icon, and position.
     *
     * @param id the module id
     * @param name the module name
     * @param icon the module icon
     * @param position the module position in navigation
     */
    public JavaModuleBuilder(String id, String name, String icon, double position) {
        module = new Module(id, name);
        module.setIcon(icon);
        module.setPosition(position);
    }

    /**
     * Adds a new page group to the module using the same value for id and name.
     *
     * @param name the group name and id
     * @return this builder instance for chaining
     */
    public JavaModuleBuilder addGroup(String name) {
        return addGroup(name.toLowerCase(), name);
    }

    /**
     * Adds a new page group to the module with the specified id and name.
     *
     * @param id the group id
     * @param name the group name
     * @return this builder instance for chaining
     */
    public JavaModuleBuilder addGroup(String id, String name) {
        return addGroup(id, name, 0);
    }

    /**
     * Adds a new page group to the module with the specified id, name, and position.
     *
     * @param id the group id
     * @param name the group name
     * @param position the group's position in navigation
     * @return this builder instance for chaining
     */
    public JavaModuleBuilder addGroup(String id, String name, double position) {
        currentGroup = new PageGroup(id, name);
        currentGroup.setPosition(position);
        module.addPageGroup(currentGroup);
        return this;
    }

    /**
     * Adds a page to the current group.
     *
     * @param page the {@link Page} to add
     * @return this builder instance for chaining
     */
    public JavaModuleBuilder addPage(Page page) {
        currentGroup.addPage(page);
        return this;
    }

    /**
     * Adds a page to the current group using the same value for id and name.
     *
     * @param name the page name and id
     * @param path the page path
     * @return this builder instance for chaining
     */
    public JavaModuleBuilder addPage(String name, String path) {
        return addPage(name.toLowerCase(), name, path);
    }

    /**
     * Adds a page to the current group with the specified id, name, and path.
     *
     * @param id the page id
     * @param name the page name
     * @param path the page path
     * @return this builder instance for chaining
     */
    public JavaModuleBuilder addPage(String id, String name, String path) {
        return addPage(id, name, path, null);
    }

    /**
     * Adds a page to the current group with the specified id, name, path, and icon.
     *
     * @param id the page id
     * @param name the page name
     * @param path the page path
     * @param icon the page icon
     * @return this builder instance for chaining
     */
    public JavaModuleBuilder addPage(String id, String name, String path, String icon) {
        return addPage(id, name, path, icon, 0);
    }

    /**
     * Adds a page to the current group with the specified id, name, path, icon, and position.
     *
     * @param id the page id
     * @param name the page name
     * @param path the page path
     * @param icon the page icon
     * @param position the page position in navigation
     * @return this builder instance for chaining
     */
    public JavaModuleBuilder addPage(String id, String name, String path, String icon, double position) {
        Page page = new Page(id, name, path);
        page.setIcon(icon);
        page.setPosition(position);
        return addPage(page);
    }

    /**
     * Builds and returns the configured {@link Module} instance.
     *
     * @return the constructed {@link Module}
     */
    @Override
    public Module build() {
        return module;
    }
}
