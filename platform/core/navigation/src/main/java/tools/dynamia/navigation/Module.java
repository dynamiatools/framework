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

import tools.dynamia.commons.StringUtils;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.ProgressMonitor;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * <p>
 * Represents a navigation module in a modular application. A module is a logical unit or feature set that contains one or more {@link PageGroup} objects and pages, and can be used to organize navigation structure, permissions, and UI elements. Modules can have properties, main pages, default groups, and support localization for their labels and descriptions.
 * </p>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * Module booksModule = new Module("books", "Books", "Manage your book collection");
 * booksModule.addPageGroup(new PageGroup("main", "Main Group"));
 * booksModule.addPage(new Page("list", "Book List"));
 * booksModule.setMainPage(new Page("dashboard", "Dashboard"));
 * </pre>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Supports multiple page groups and pages</li>
 *   <li>Allows setting a main page and default group</li>
 *   <li>Provides property storage for custom attributes</li>
 *   <li>Supports localization via resource bundles</li>
 *   <li>Implements {@link Serializable} and {@link Cloneable}</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * This class is not thread-safe by default. Synchronize externally if accessed concurrently.
 * </p>
 *
 * @author Ing. Mario Serrano Leones
 */
public class Module extends NavigationElement<Module> implements Serializable, Cloneable {

    private static final long serialVersionUID = -817147208762427863L;
    private final List<PageGroup> pageGroups = new ArrayList<>();
    private Page mainPage;
    private PageGroup defaultGroup = new PageGroup();
    private final LoggingService logger = new SLF4JLoggingService();
    private final Map<String, Object> properties = new HashMap<>();
    private Class baseClass;
    private final List<Class> additionalBaseClasses = new ArrayList<>();

    /**
     * Constructs a module with the specified id, name, and description.
     *
     * @param id the unique identifier of the module
     * @param name the display name of the module
     * @param description the description of the module
     */
    public Module(String id, String name, String description) {
        super(id, name, description);
        setPosition(Double.MAX_VALUE);
        defaultGroup.setParentModule(this);
    }

    /**
     * Constructs a module with the specified id and name.
     *
     * @param id the unique identifier of the module
     * @param name the display name of the module
     */
    public Module(String id, String name) {
        this(id, name, null);
    }

    /**
     * Constructs an empty module.
     */
    public Module() {
        this(null, null);
    }

    /**
     * Returns the display name of the module.
     *
     * @return the name of the module
     */
    @Override
    public String getName() {
        return super.getName();
    }

    /**
     * Returns the main page of the module, if set.
     *
     * @return the main {@link Page} of the module, or null if not set
     */
    public Page getMainPage() {
        return mainPage;
    }

    /**
     * Sets the main page of the module.
     *
     * @param mainAction the {@link Page} to set as main
     */
    public void setMainPage(Page mainAction) {
        this.mainPage = mainAction;
    }

    /**
     * Adds a page group to the module.
     *
     * @param pageGroup the {@link PageGroup} to add
     * @return this module instance for chaining
     */
    public Module addPageGroup(PageGroup pageGroup) {
        if (pageGroup == null) {
            logger.warn("Navigation " + this.getName() + " cannot add null PageGroup");
        }

        if (getPageGroupById(pageGroup.getId()) != null) {
            logger.warn("There is a PageGroup with the same ID added in " + getName() + "'s Navigation");
        } else {
            this.pageGroups.add(pageGroup);
            pageGroup.setParentModule(this);
        }
        return this;
    }

    /**
     * Adds multiple page groups to the module.
     *
     * @param groups the page groups to add
     * @return this module instance for chaining
     */
    public Module addPageGroup(PageGroup... groups) {
        if (groups != null) {
            for (PageGroup group : groups) {
                addPageGroup(group);
            }
        }
        return this;
    }

    /**
     * Adds a page to the default group of the module.
     * If the page is marked as main, it is also set as the main page.
     *
     * @param page the {@link Page} to add
     * @return this module instance for chaining
     */
    public Module addPage(Page page) {
        defaultGroup.addPage(page);
        if (page.isMain()) {
            setMainPage(page);
        }
        return this;
    }

    /**
     * Adds multiple pages to the default group of the module.
     *
     * @param pages the pages to add
     * @return this module instance for chaining
     */
    public Module addPage(Page... pages) {
        if (pages != null) {
            for (Page page : pages) {
                addPage(page);
            }
        }
        return this;
    }

    /**
     * Returns the list of page groups in the module.
     *
     * @return the list of {@link PageGroup}
     */
    public List<PageGroup> getPageGroups() {
        return pageGroups;
    }

    /**
     * Returns the default page group of the module.
     *
     * @return the default {@link PageGroup}
     */
    public PageGroup getDefaultPageGroup() {
        return defaultGroup;
    }

    /**
     * Sets the default page group of the module.
     *
     * @param defaultGroup the {@link PageGroup} to set as default
     */
    public void setDefaultGroup(PageGroup defaultGroup) {
        this.defaultGroup = defaultGroup;
        if (this.defaultGroup != null) {
            this.defaultGroup.setParentModule(this);
        }
    }

    /**
     * Returns the page group with the specified id.
     *
     * @param id the id of the page group
     * @return the {@link PageGroup} with the given id, or null if not found
     */
    public PageGroup getPageGroupById(String id) {

        for (PageGroup pg : pageGroups) {
            if (pg.getId().equalsIgnoreCase(id)) {
                return pg;
            }
        }
        return null;
    }

    /**
     * Creates a deep clone of this module, including its main page and base class.
     *
     * @return a cloned {@link Module} instance
     */
    @Override
    public Module clone() {
        Module m = (Module) super.clone();
        m.setMainPage(mainPage.clone());
        m.setBaseClass(baseClass);

        return m;
    }

    /**
     * Returns the first page group in the module, or null if none exist.
     *
     * @return the first {@link PageGroup}, or null
     */
    public PageGroup getFirstPageGroup() {
        return getPageGroups().stream().findFirst().orElse(null);
    }

    /**
     * Returns the first page in the first page group, or null if none exist.
     *
     * @return the first {@link Page}, or null
     */
    public Page getFirstPage() {
        var firstGroup = getFirstPageGroup();
        if (firstGroup != null)
            return firstGroup.getFirstPage();
        else
            return null;

    }

    /**
     * Adds a custom property to the module.
     *
     * @param key the property key
     * @param value the property value
     * @return the previous value associated with the key, or null
     */
    public Object addProperty(String key, Object value) {
        return properties.put(key, value);
    }

    /**
     * Removes a property from the module.
     *
     * @param key the property key
     * @return the removed value, or null if not present
     */
    public Object removeProperty(String key) {
        return properties.remove(key);
    }

    /**
     * Returns the value of a property by key.
     *
     * @param key the property key
     * @return the property value, or null if not present
     */
    public Object getProperty(String key) {
        return properties.get(key);
    }

    /**
     * Returns all custom properties of the module.
     *
     * @return a map of property keys and values
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Reloads the module. Intended for internal use or extension.
     */
    void reload() {
    }


    /**
     * Creates a reference module with the given id. The name is capitalized and marked as reference.
     *
     * @param id the module id
     * @return a reference {@link Module}
     */
    public static Module getRef(String id) {
        Module module = new Module();
        module.setId(id);
        module.setName(StringUtils.capitalize(id));
        module.setReference(true);
        return module;
    }

    /**
     * Returns a localized text for the module using the given locale, suffix, and default value.
     *
     * @param locale the locale to use
     * @param sufix the suffix for the message key
     * @param defaultValue the default value if no localization is found
     * @return the localized text, or defaultValue if not found
     */
    @Override
    protected String getLocalizedText(Locale locale, String sufix, String defaultValue) {
        return findLocalizedTextByKey(locale, msgKey(sufix), defaultValue);
    }

    /**
     * Finds a localized text by key in resource bundles for the given locale.
     *
     * @param locale the locale to use
     * @param key the message key
     * @param defaultValue the default value if not found
     * @return the localized text, or defaultValue if not found
     */
    String findLocalizedTextByKey(Locale locale, String key, String defaultValue) {


        ResourceBundle resourceBundle = findBundle(locale, key);

        if (resourceBundle != null && resourceBundle.containsKey(key)) {

            return resourceBundle.getString(key);
        } else {
            return defaultValue;
        }
    }

    /**
     * Finds a resource bundle for the given locale and key, searching base and additional base classes.
     *
     * @param locale the locale to use
     * @param key the message key
     * @return the {@link ResourceBundle} found, or null if not found
     */
    private ResourceBundle findBundle(Locale locale, String key) {
        ResourceBundle bundle = null;

        if (baseClass != null) {
            try {
                bundle = ResourceBundle.getBundle(baseClass.getName(), locale);
            } catch (MissingResourceException ex) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Cannot find resource bundle for module base class: " + getName());
                }
                bundle = null;
            }

            if ((bundle == null || !bundle.containsKey(key)) && !additionalBaseClasses.isEmpty()) {
                for (Class addbaseclass : additionalBaseClasses) {
                    try {
                        bundle = ResourceBundle.getBundle(addbaseclass.getName(), locale);
                        if (bundle.containsKey(key)) {
                            break;
                        }
                    } catch (MissingResourceException e) {
                        //not bundle found in additional base class
                    }
                }
            }
        }

        return bundle;
    }


    /**
     * Builds a cache key for resource bundles based on class and locale.
     *
     * @param targetClass the class to use
     * @param locale the locale
     * @return the cache key string
     */
    private String buildBundleCacheKey(Class targetClass, Locale locale) {
        return targetClass.getName() + locale.toString();
    }

    /**
     * Returns the base class used for resource bundle lookup and localization.
     *
     * @return the base {@link Class}
     */
    public Class getBaseClass() {
        return baseClass;
    }

    /**
     * Sets the base class for resource bundle lookup and localization.
     *
     * @param baseClass the base {@link Class}
     */
    public void setBaseClass(Class baseClass) {
        this.baseClass = baseClass;
    }

    /**
     * Adds an additional base class for resource bundle lookup.
     *
     * @param baseClass the additional {@link Class}
     */
    public void addBaseClass(Class baseClass) {
        if (baseClass != null && !additionalBaseClasses.contains(baseClass)) {
            additionalBaseClasses.add(baseClass);
        }
    }

    /**
     * Returns true if the module has no page groups and the default group has no pages.
     *
     * @return true if the module is empty, false otherwise
     */
    public boolean isEmpty() {
        return getPageGroups().isEmpty() && getDefaultPageGroup().getPages().isEmpty();
    }

    /**
     * Traverses all pages in all (non-dynamic) groups and subgroups, applying the given action and progress monitor.
     *
     * @param action the action to apply to each page
     * @param monitor the progress monitor
     */
    public void forEachPage(Consumer<Page> action, ProgressMonitor monitor) {
        forEachPage(action, monitor, false);
    }

    /**
     * Traverses all pages in all groups and subgroups, optionally including dynamic groups.
     *
     * @param action the action to apply to each page
     * @param monitor the progress monitor
     * @param includeDynamic true to include dynamic groups, false otherwise
     */
    public void forEachPage(Consumer<Page> action, ProgressMonitor monitor, boolean includeDynamic) {
        Stream<PageGroup> groups = Stream.concat(
                Stream.of(defaultGroup),
                pageGroups.stream()
        );

        groups.filter(grp -> includeDynamic || !grp.isDynamic())
                .forEach(grp -> forEachPageGroup(action, grp, monitor));
    }

    /**
     * Traverses all pages in all (non-dynamic) groups and subgroups, applying the given action.
     *
     * @param action the action to apply to each page
     */
    public void forEachPage(Consumer<Page> action) {
        forEachPage(action, new ProgressMonitor());
    }

    /**
     * Helper method to traverse all pages in a page group and its subgroups.
     *
     * @param action the action to apply to each page
     * @param grp the page group to traverse
     * @param monitor the progress monitor
     */
    private void forEachPageGroup(Consumer<Page> action, PageGroup grp, ProgressMonitor monitor) {

        List<Page> pages = null;
        try {
            pages = grp.getPages();
        } catch (Exception e) {
            logger.error("Error getting pages from group: " + grp.getVirtualPath() + " - " + e.getMessage(), e);
        }

        if (pages != null && !pages.isEmpty()) {
            monitor.reset();
            monitor.setMax(pages.size());
            for (Page page : pages) {
                action.accept(page);
                monitor.increment();
                if (monitor.isStopped()) {
                    break;
                }
            }
        }
        if (grp.getPageGroups() != null && !grp.getPageGroups().isEmpty()) {
            grp.getPageGroups().forEach(subgrp -> forEachPageGroup(action, subgrp, monitor));
        }

    }

    /**
     * Finds a page by its virtual path in all groups and subgroups.
     *
     * @param virtualPath the virtual path of the page
     * @return the {@link Page} found, or null if not found
     */
    public Page findPage(String virtualPath) {

        AtomicReference<Page> reference = new AtomicReference<>();
        try {
            var monitor = new ProgressMonitor();
            forEachPage(page -> {
                if (virtualPath.equals(page.getVirtualPath())) {
                    reference.set(page);
                    monitor.stop();
                }
            }, monitor);
        } catch (Exception e) {

        }
        return reference.get();
    }

    /**
     * Finds a page by its pretty virtual path in all groups and subgroups.
     *
     * @param prettyPath the pretty virtual path of the page
     * @return the {@link Page} found, or null if not found
     */
    public Page findPageByPrettyPath(String prettyPath) {

        AtomicReference<Page> reference = new AtomicReference<>();
        try {
            var monitor = new ProgressMonitor();
            forEachPage(page -> {
                if (prettyPath.equals(page.getPrettyVirtualPath())) {
                    reference.set(page);
                    monitor.stop();
                }
            }, monitor, true);
        } catch (Exception e) {

        }
        return reference.get();
    }

    /**
     * Creates a new {@link JavaModuleBuilder} with the given module name (used as both id and name).
     * <p>
     * Example:
     * <pre>
     * Module booksModule = Module.builder("Books")
     *     .addGroup("main", "Main Group")
     *     .addPage("list", "Book List", "/books/list")
     *     .build();
     * </pre>
     * @param name the module name and id
     * @return a new {@link JavaModuleBuilder}
     */
    public static JavaModuleBuilder builder(String name) {
        return new JavaModuleBuilder(name);
    }

    /**
     * Creates a new {@link JavaModuleBuilder} with the given module id and name.
     * <p>
     * Example:
     * <pre>
     * Module booksModule = Module.builder("books", "Books")
     *     .addGroup("main", "Main Group")
     *     .addPage("list", "Book List", "/books/list")
     *     .build();
     * </pre>
     * @param id the module id
     * @param name the module name
     * @return a new {@link JavaModuleBuilder}
     */
    public static JavaModuleBuilder builder(String id, String name) {
        return new JavaModuleBuilder(id, name);
    }

    /**
     * Creates a new {@link JavaModuleBuilder} with the given module id, name, icon, and position.
     * <p>
     * Example:
     * <pre>
     * Module booksModule = Module.builder("books", "Books", "fa-book", 1)
     *     .addGroup("main", "Main Group")
     *     .addPage("list", "Book List", "/books/list", "fa-list", 1)
     *     .build();
     * </pre>
     * @param id the module id
     * @param name the module name
     * @param icon the module icon
     * @param position the module position in navigation
     * @return a new {@link JavaModuleBuilder}
     */
    public static JavaModuleBuilder builder(String id, String name, String icon, double position) {
        return new JavaModuleBuilder(id, name, icon, position);
    }
}
