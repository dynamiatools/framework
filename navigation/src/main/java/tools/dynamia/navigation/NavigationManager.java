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
 * Control current active module and pages and allow to navigate between pages
 *
 * @author Ing. Mario Serrano Leones
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
     * Return the current active module
     */
    Module getActiveModule();

    /**
     * return the current page from the active module
     */
    Page getCurrentPage();

    /**
     * return the current page's attributes. This attributes keep alive until
     * current page is changed
     *
     * @return map
     */
    Map<String, Serializable> getCurrentPageAttributes();

    /**
     * return the current page gruop from the selected page
     */
    PageGroup getCurrentPageGroup();

    /**
     * return all page groups from activeModule
     */
    Collection<PageGroup> getPageGroups();

    /**
     * set the current page navigation through the virtual path.
     * Module/pageGroup/page
     */
    void navigateTo(String path);

    void navigateTo(String path, Map<String, Serializable> params);

    /**
     * Refresh or reload the current selected page
     */
    void refresh();

    /**
     * set the active module
     */
    void setActiveModule(Module activeModule);

    /**
     * set the current page
     */
    boolean setCurrentPage(Page newPage);

    boolean setCurrentPage(Page newPage, Map<String, Serializable> params);

    /**
     * Find a page object by its path
     */
    Page findPage(String path);

    /**
     * Close the page if active, result may changed depending implementation
     */
    void closePage(Page page);

    /**
     * Close the current page
     */
    void closeCurrentPage();

    Page findPageByPrettyVirtualPath(String prettyPath);

    List<Page> findPagesByName(String name);


    void sendEvent(PageEvent evt);

    void sendEvent(String name, Object data);

    void sendEvent(String name, Page page, Object data);

    void onPageEvent(Page page, Consumer<PageEvent> evt);

    void clearPageEvents(Page page);

    List<Page> getAvailablesPages();

    void addAvailablePage(Page page);

    NavigationElement findElement(String path);

    void reload();


    NavigationBuilder getCurrentNavigationBuilder();

    void setCurrentNavigationBuilder(NavigationBuilder navigationBuilder);
}
