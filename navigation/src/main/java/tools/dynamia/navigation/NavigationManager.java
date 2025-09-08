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

import tools.dynamia.commons.Callback;
import tools.dynamia.integration.Containers;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * <p>
 * The <b>NavigationManager</b> interface controls the current active module and pages, allowing navigation between pages in a modular application. It provides methods to manage navigation state, handle page events, and interact with modules, page groups, and navigation elements. This interface is typically used in web applications or modular systems where navigation and page management are required.
 * </p>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * NavigationManager navigationManager = NavigationManager.getCurrent();
 *
 * // Navigate to a page by path
 * navigationManager.navigateTo("books/list");
 *
 * // Navigate to a page with parameters
 * Map<String, Serializable> params = new HashMap<>();
 * params.put("author", "John Doe");
 * navigationManager.navigateTo("books/detail", params);
 *
 * // Get current page and module
 * Page currentPage = navigationManager.getCurrentPage();
 * Module activeModule = navigationManager.getActiveModule();
 *
 * // Refresh current page
 * navigationManager.refresh();
 *
 * // Listen for page events
 * navigationManager.onPageEvent(currentPage, event -> {
 *     System.out.println("Page event: " + event.getName());
 * });
 *
 * // Send a custom event
 * navigationManager.sendEvent("customEvent", currentPage, "Some data");
 * </pre>
 *
 * <h2>Deferred Navigation</h2>
 * <p>
 * You can schedule navigation actions to be executed later, for example during session initialization:
 * </p>
 * <pre>
 * NavigationManager.setPageLater("books/list");
 * NavigationManager.runLater(() -> {
 *     System.out.println("NavigationManager is ready!");
 * });
 * </pre>
 *
 * <h2>Key Methods</h2>
 * <ul>
 *   <li><b>navigateTo(String path)</b>: Navigates to the specified page path.</li>
 *   <li><b>setCurrentPage(Page page)</b>: Sets the current page.</li>
 *   <li><b>getCurrentPageAttributes()</b>: Returns attributes for the current page.</li>
 *   <li><b>sendEvent(String name, Object data)</b>: Sends a custom event to the current page.</li>
 *   <li><b>onPageEvent(Page page, Consumer&lt;PageEvent&gt; evt)</b>: Registers a listener for page events.</li>
 *   <li><b>findPage(String path)</b>: Finds a page by its path.</li>
 *   <li><b>closeCurrentPage()</b>: Closes the current page.</li>
 *   <li><b>reload()</b>: Reloads the navigation structure.</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * Implementations should ensure thread safety if used in concurrent environments.
 * </p>
 *
 * <h2>See Also</h2>
 * <ul>
 *   <li>{@link Module}</li>
 *   <li>{@link Page}</li>
 *   <li>{@link PageGroup}</li>
 *   <li>{@link NavigationElement}</li>
 *   <li>{@link NavigationManagerSession}</li>
 * </ul>
 */
public interface NavigationManager {

    String CURRENT_PAGE_ATTRIBUTE = "NavCurrentPage";
    String CURRENT_PAGE_PARAMS_ATTRIBUTE = "NavCurrentPageParams";

    static NavigationManager getCurrent() {
        return Containers.get().findObject(NavigationManager.class);
    }

    /**
     * Delegate set current {@link Page} using a {@link NavigationManagerSession} when NavigationManager is builded
     *
     * @param page
     */
    static void setPageLater(Page page) {
        setPageLater(page, null);
    }

    /**
     * Delegate set current {@link Page} using a {@link NavigationManagerSession} when NavigationManager is builded
     *
     * @param page
     * @param params
     */
    static void setPageLater(Page page, Map<String, Serializable> params) {
        NavigationManagerSession.getInstance().setPage(page, params);
    }

    static void setPageLater(String path) {
        setPageLater(path, null);
    }

    static void setPageLater(String path, Map<String, Serializable> params) {
        setPageLater(ModuleContainer.getInstance().findPage(path), params);
    }

    /**
     * Delegate callback to run when {@link NavigationManager} are builded. Its store a Queue using {@link NavigationManagerSession}
     *
     * @param callback
     */
    static void runLater(Callback callback) {
        NavigationManagerSession.getInstance().runLater(callback);
    }

    /**
     * Returns the current active module.
     * <p>
     * The active module represents the main context for navigation and page management.
     * </p>
     * @return the current {@link Module}
     */
    Module getActiveModule();

    /**
     * Returns the current page from the active module.
     * <p>
     * The current page is the one currently being displayed or interacted with.
     * </p>
     * @return the current {@link Page}
     */
    Page getCurrentPage();

    /**
     * Returns the current page's attributes.
     * <p>
     * These attributes persist until the current page is changed.
     * </p>
     * @return a map of attributes for the current page
     */
    Map<String, Serializable> getCurrentPageAttributes();

    /**
     * Returns the current page group from the selected page.
     * <p>
     * Page groups are logical groupings of pages within a module.
     * </p>
     * @return the current {@link PageGroup}
     */
    PageGroup getCurrentPageGroup();

    /**
     * Returns all page groups from the active module.
     * @return a collection of {@link PageGroup}
     */
    Collection<PageGroup> getPageGroups();

    /**
     * Navigates to the specified page using its virtual path (Module/pageGroup/page).
     * <p>
     * Example: <code>navigateTo("books/list")</code>
     * </p>
     * @param path the virtual path of the page
     */
    void navigateTo(String path);

    /**
     * Navigates to the specified page with parameters.
     * <p>
     * Example: <code>navigateTo("books/detail", params)</code>
     * </p>
     * @param path the virtual path of the page
     * @param params parameters to pass to the page
     */
    void navigateTo(String path, Map<String, Serializable> params);

    /**
     * Refreshes or reloads the current selected page.
     */
    void refresh();

    /**
     * Sets the active module.
     * @param activeModule the module to set as active
     */
    void setActiveModule(Module activeModule);

    /**
     * Sets the current page.
     * @param newPage the page to set as current
     * @return true if the page was set successfully
     */
    boolean setCurrentPage(Page newPage);

    /**
     * Sets the current page with parameters.
     * @param newPage the page to set as current
     * @param params parameters to pass to the page
     * @return true if the page was set successfully
     */
    boolean setCurrentPage(Page newPage, Map<String, Serializable> params);

    /**
     * Finds a page object by its path.
     * @param path the virtual path of the page
     * @return the {@link Page} found, or null if not found
     */
    Page findPage(String path);

    /**
     * Closes the specified page if active.
     * <p>
     * The result may vary depending on the implementation.
     * </p>
     * @param page the page to close
     */
    void closePage(Page page);

    /**
     * Closes the current page.
     */
    void closeCurrentPage();

    /**
     * Finds a page by its pretty virtual path.
     * @param prettyPath the pretty virtual path
     * @return the {@link Page} found, or null if not found
     */
    Page findPageByPrettyVirtualPath(String prettyPath);

    /**
     * Finds pages by their name.
     * @param name the name of the page(s)
     * @return a list of {@link Page} objects matching the name
     */
    List<Page> findPagesByName(String name);

    /**
     * Sends a page event to listeners.
     * @param evt the {@link PageEvent} to send
     */
    void sendEvent(PageEvent evt);

    /**
     * Sends a custom event to the current page.
     * @param name the event name
     * @param data the event data
     */
    void sendEvent(String name, Object data);

    /**
     * Sends a custom event to a specific page.
     * @param name the event name
     * @param page the target page
     * @param data the event data
     */
    void sendEvent(String name, Page page, Object data);

    /**
     * Registers a listener for page events.
     * @param page the page to listen to
     * @param evt the event consumer
     */
    void onPageEvent(Page page, Consumer<PageEvent> evt);

    /**
     * Clears all page events for the specified page.
     * @param page the page whose events should be cleared
     */
    void clearPageEvents(Page page);

    /**
     * Returns the list of available pages.
     * @return a list of {@link Page}
     */
    List<Page> getAvailablesPages();

    /**
     * Adds a page to the list of available pages.
     * @param page the page to add
     */
    void addAvailablePage(Page page);

    /**
     * Finds a navigation element by its path.
     * @param path the path of the navigation element
     * @return the {@link NavigationElement} found, or null if not found
     */
    NavigationElement findElement(String path);

    /**
     * Reloads the navigation structure.
     */
    void reload();

    /**
     * Returns the current navigation builder.
     * @return the {@link NavigationBuilder}
     */
    NavigationBuilder getCurrentNavigationBuilder();

    /**
     * Sets the current navigation builder.
     * @param navigationBuilder the navigation builder to set
     */
    void setCurrentNavigationBuilder(NavigationBuilder navigationBuilder);
}
