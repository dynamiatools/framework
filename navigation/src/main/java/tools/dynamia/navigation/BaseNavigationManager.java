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

import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.Containers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * BaseNavigationManager is an abstract implementation of the {@link NavigationManager} interface, providing core navigation logic for modular applications. It manages the current active module, page groups, pages, navigation events, and attributes, and serves as a foundation for concrete navigation manager implementations.
 * </p>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Manages active module, current page, and page group</li>
 *   <li>Handles navigation events: page load, unload, and close</li>
 *   <li>Supports navigation by path, page, and module</li>
 *   <li>Stores page attributes and parameters</li>
 *   <li>Integrates with {@link ModuleContainer} for page/module lookup</li>
 *   <li>Provides event firing for {@link NavigationListener}s</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * // Example subclass
 * public class MyNavigationManager extends BaseNavigationManager {
 *     public MyNavigationManager(ModuleContainer container) {
 *         super(container);
 *     }
 * }
 *
 * // Usage
 * NavigationManager manager = new MyNavigationManager(moduleContainer);
 * manager.navigateTo("books/list");
 * Page currentPage = manager.getCurrentPage();
 * </pre>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * This class is not thread-safe by default. Synchronize externally if accessed concurrently.
 * </p>
 *
 * @author Ing. Mario Serrano Leones
 */
public abstract class BaseNavigationManager implements Serializable, NavigationManager {

    private static final long serialVersionUID = -2002282546215982419L;
    public static final String ON_PAGE_CLOSE = "onPageClose";
    public static final String ON_PAGE_UNLOAD = "onPageUnload";
    public static final String ON_PAGE_LOAD = "onPageLoad";
    private final LoggingService logger;
    private Module activeModule;
    private PageGroup currentPageGroup;
    private Page lastPage;
    private Page currentPage;
    private final Map<String, Serializable> attributes;
    private final ModuleContainer container;
    private List<Page> availablesPages = new ArrayList<>();

    private NavigationBuilder currentNavigationBuilder;
    private Map<String, Serializable> currentPageParams;

    /**
     * Creates a new BaseNavigationManager with the given {@link ModuleContainer}.
     *
     * @param container the module container used for page and module lookup
     */
    public BaseNavigationManager(ModuleContainer container) {
        this.logger = new SLF4JLoggingService(BaseNavigationManager.class);
        this.attributes = new HashMap<>();
        this.container = container;
    }

    /**
     * Returns the current active module. If only one module exists, it is set as active.
     *
     * @return the current {@link Module}
     */
    @Override
    public Module getActiveModule() {
        if (activeModule == null && container.getModuleCount() == 1) {
            activeModule = container.getFirstModule();
        }
        return activeModule;
    }

    /**
     * Sets the active module and navigates to its main page if available.
     *
     * @param activeModule the module to set as active
     */
    @Override
    public void setActiveModule(Module activeModule) {
        this.activeModule = activeModule;
        if (activeModule != null) {
            Page mainPage = activeModule.getMainPage();
            if (mainPage != null) {
                setCurrentPage(mainPage);
            }
            logger.debug("Current WebModule " + activeModule.getName());
        }
    }

    /**
     * Returns all page groups of the active module.
     *
     * @return a collection of {@link PageGroup}
     */
    @Override
    public Collection<PageGroup> getPageGroups() {
        if (getActiveModule() == null) {
            return null;
        }
        return getActiveModule().getPageGroups();
    }

    /**
     * Returns the current page. If no page is selected, returns null.
     *
     * @return the current {@link Page}
     */
    @Override
    public Page getCurrentPage() {

        if (currentPage == null) {
            currentPageParams = null;
            logger.debug("Selected Page is null, default page returned");
        }

        return currentPage;
    }

    /**
     * Returns the attributes for the current page. These are cleared when the page changes.
     *
     * @return a map of page attributes
     */
    @Override
    public Map<String, Serializable> getCurrentPageAttributes() {
        return attributes;
    }

    /**
     * Sets the current page without parameters.
     *
     * @param newPage the page to set as current
     * @return true if the page was set successfully
     */
    @Override
    public boolean setCurrentPage(Page newPage) {
        return setCurrentPage(newPage, null);
    }

    /**
     * Sets the current page with parameters. Fires unload/load events and updates attributes.
     *
     * @param newPage the page to set as current
     * @param params parameters for the page
     * @return true if the page was set successfully
     */
    @Override
    public boolean setCurrentPage(Page newPage, Map<String, Serializable> params) {
        if (currentPage != null && currentPage.equals(newPage)) {
            //already in that page
            return false;
        }

        if (newPage != null && NavigationRestrictions.allowAccess(newPage)) {

            lastPage = currentPage;
            fireOnPageUnload(lastPage);
            this.currentPage = newPage;
            this.currentPageGroup = newPage.getPageGroup();
            if (currentPageGroup != null) {
                this.activeModule = currentPageGroup.getParentModule();
            }
            this.currentPageParams = params;
            this.attributes.clear();
            fireOnPageLoad(newPage, params);

            logger.debug("Current Page " + newPage.getId());
            return true;
        }
        return false;
    }

    /**
     * Returns the current page group.
     *
     * @return the current {@link PageGroup}
     */
    @Override
    public PageGroup getCurrentPageGroup() {
        return currentPageGroup;
    }

    /**
     * Returns the last visited page.
     *
     * @return the last {@link Page}
     */
    public Page getLastPage() {
        return lastPage;
    }

    /**
     * Navigates to a page or module by path. If path contains '/', navigates to page; otherwise, sets active module.
     *
     * @param path the navigation path
     */
    @Override
    public void navigateTo(String path) {
        navigateTo(path, null);
    }

    /**
     * Navigates to a page by path with parameters.
     *
     * @param path the navigation path
     * @param params parameters for the page
     */
    @Override
    public void navigateTo(String path, Map<String, Serializable> params) {
        if (!path.contains("/")) {
            setActiveModule(container.getModuleById(path));
        } else {
            Page page = findPage(path);
            setCurrentPage(page, params);
        }
    }

    /**
     * Refreshes the current page by reloading and resetting it.
     */
    @Override
    public void refresh() {
        var page = currentPage;
        reload();
        setCurrentPage(page);
    }

    /**
     * Finds a page by its path using the module container.
     *
     * @param path the page path
     * @return the {@link Page} found, or null if not found
     */
    @Override
    public Page findPage(String path) {
        return container.findPage(path);
    }

    /**
     * Finds a page by its pretty virtual path using the module container.
     *
     * @param prettyPath the pretty virtual path
     * @return the {@link Page} found, or null if not found
     */
    @Override
    public Page findPageByPrettyVirtualPath(String prettyPath) {
        return container.findPageByPrettyVirtualPath(prettyPath);
    }

    /**
     * Closes the current page if one is selected.
     */
    @Override
    public void closeCurrentPage() {
        if (getCurrentPage() != null) {
            closePage(getCurrentPage());
        }
    }

    /**
     * Finds pages by their name using the module container.
     *
     * @param name the page name
     * @return a list of {@link Page} objects matching the name
     */
    @Override
    public List<Page> findPagesByName(String name) {
        return container.findPagesByName(name);
    }


    /**
     * Fires the onPageLoad event for the given page and parameters, notifying listeners and callbacks.
     *
     * @param page the page being loaded
     * @param params parameters for the page
     */
    protected void fireOnPageLoad(Page page, Map<String, Serializable> params) {
        if (isValidPage(page)) {
            if (page.getOnOpenCallback() != null) {
                page.getOnOpenCallback().doSomething();
            }

            for (NavigationListener listener : Containers.get().findObjects(NavigationListener.class)) {
                listener.onPageLoad(new PageEvent(ON_PAGE_LOAD, page, page, params));
            }
        }
    }

    /**
     * Fires the onPageUnload event for the given page, notifying listeners and callbacks.
     *
     * @param page the page being unloaded
     */
    protected void fireOnPageUnload(Page page) {
        if (isValidPage(page)) {
            if (page.getOnUnloadCallback() != null) {
                page.getOnUnloadCallback().doSomething();
            }

            for (NavigationListener listener : Containers.get().findObjects(NavigationListener.class)) {
                listener.onPageUnload(new PageEvent(ON_PAGE_UNLOAD, page, null));
            }
        }
    }

    /**
     * Fires the onPageClose event for the given page, notifying listeners and callbacks.
     *
     * @param page the page being closed
     */
    protected void fireOnPageClose(Page page) {
        if (isValidPage(page)) {
            if (page.getOnCloseCallback() != null) {
                page.getOnCloseCallback().doSomething();
            }
            for (NavigationListener listener : Containers.get().findObjects(NavigationListener.class)) {
                listener.onPageClose(new PageEvent(ON_PAGE_CLOSE, page, null));
            }
        }
    }

    /**
     * Sends a custom event to the current page.
     *
     * @param name the event name
     * @param data the event data
     */
    @Override
    public void sendEvent(String name, Object data) {
        sendEvent(new PageEvent(name, getCurrentPage(), data));
    }

    /**
     * Sends a custom event to a specific page.
     *
     * @param name the event name
     * @param page the target page
     * @param data the event data
     */
    @Override
    public void sendEvent(String name, Page page, Object data) {
        sendEvent(new PageEvent(name, page, data));
    }

    /**
     * Checks if the given page is valid (not null).
     *
     * @param page the page to check
     * @return true if the page is valid, false otherwise
     */
    private boolean isValidPage(Page page) {
        return page != null;
    }

    /**
     * Returns the list of available pages.
     *
     * @return a list of {@link Page}
     */
    @Override
    public List<Page> getAvailablesPages() {
        return availablesPages;
    }

    /**
     * Sets the list of available pages.
     *
     * @param availablesPages the list of available pages
     */
    public void setAvailablesPages(List<Page> availablesPages) {
        this.availablesPages = availablesPages;
    }

    /**
     * Adds a page to the list of available pages.
     *
     * @param page the page to add
     */
    @Override
    public void addAvailablePage(Page page) {
        availablesPages.add(page);
    }

    /**
     * Finds a navigation element by its path using the module container.
     *
     * @param path the navigation element path
     * @return the {@link NavigationElement} found, or null if not found
     */
    @Override
    public NavigationElement findElement(String path) {
        return container.findElement(path);
    }

    /**
     * Sets the current page directly, bypassing navigation logic. Intended for internal use.
     *
     * @param page the page to set as current
     */
    public void setRawCurrentPage(Page page) {
        this.currentPage = page;
    }

    /**
     * Reloads the navigation manager, clearing last and current page references.
     */
    @Override
    public void reload() {
        this.lastPage = null;
        this.currentPage = null;
    }

    /**
     * Returns the current navigation builder.
     *
     * @return the {@link NavigationBuilder}
     */
    @Override
    public NavigationBuilder getCurrentNavigationBuilder() {
        return currentNavigationBuilder;
    }

    /**
     * Sets the current navigation builder.
     *
     * @param currentNavigationBuilder the navigation builder to set
     */
    @Override
    public void setCurrentNavigationBuilder(NavigationBuilder currentNavigationBuilder) {
        this.currentNavigationBuilder = currentNavigationBuilder;
    }

    /**
     * Returns the parameters for the current page, if any.
     *
     * @return a map of current page parameters
     */
    public Map<String, Serializable> getCurrentPageParams() {
        return currentPageParams;
    }

    /**
     * Sets the last visited page.
     *
     * @param lastPage the page to set as last visited
     */
    public void setLastPage(Page lastPage) {
        this.lastPage = lastPage;
    }
}
