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
    private final Map<String, Object> attributes;
    private final ModuleContainer container;
    private List<Page> availablesPages = new ArrayList<>();

    private NavigationBuilder currentNavigationBuilder;
    private Map<String, Object> currentPageParams;

    public BaseNavigationManager(ModuleContainer container) {
        this.logger = new SLF4JLoggingService(BaseNavigationManager.class);
        this.attributes = new HashMap<>();
        this.container = container;
    }

    @Override
    public Module getActiveModule() {
        if (activeModule == null && container.getModuleCount() == 1) {
            activeModule = container.getFirstModule();
        }
        return activeModule;
    }

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

    @Override
    public Collection<PageGroup> getPageGroups() {
        if (getActiveModule() == null) {
            return null;
        }
        return getActiveModule().getPageGroups();
    }

    @Override
    public Page getCurrentPage() {

        if (currentPage == null) {
            currentPageParams = null;
            logger.debug("Selected Page is null, default page returned");
        }

        return currentPage;
    }

    @Override
    public Map<String, Object> getCurrentPageAttributes() {
        return attributes;
    }

    @Override
    public boolean setCurrentPage(Page newPage) {
        return setCurrentPage(newPage, null);
    }

    @Override
    public boolean setCurrentPage(Page newPage, Map<String, Object> params) {
        if (lastPage != null && lastPage.equals(newPage)) {
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

    @Override
    public PageGroup getCurrentPageGroup() {
        return currentPageGroup;
    }

    public Page getLastPage() {
        return lastPage;
    }

    @Override
    public void navigateTo(String path) {
        navigateTo(path, null);
    }

    @Override
    public void navigateTo(String path, Map<String, Object> params) {
        if (!path.contains("/")) {
            setActiveModule(container.getModuleById(path));
        } else {
            Page page = findPage(path);
            setCurrentPage(page, params);
        }
    }

    @Override
    public void refresh() {
        var page = currentPage;
        reload();
        setCurrentPage(page);
    }

    @Override
    public Page findPage(String path) {
        return container.findPage(path);
    }

    @Override
    public Page findPageByPrettyVirtualPath(String prettyPath) {
        return container.findPageByPrettyVirtualPath(prettyPath);
    }

    @Override
    public void closeCurrentPage() {
        if (getCurrentPage() != null) {
            closePage(getCurrentPage());
        }
    }

    @Override
    public List<Page> findPagesByName(String name) {
        return container.findPagesByName(name);
    }


    protected void fireOnPageLoad(Page page, Map<String, Object> params) {
        if (isValidPage(page)) {
            if (page.getOnOpenCallback() != null) {
                page.getOnOpenCallback().doSomething();
            }

            for (NavigationListener listener : Containers.get().findObjects(NavigationListener.class)) {
                listener.onPageLoad(new PageEvent(ON_PAGE_LOAD, page, page, params));
            }
        }
    }

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

    @Override
    public void sendEvent(String name, Object data) {
        sendEvent(new PageEvent(name, getCurrentPage(), data));
    }

    @Override
    public void sendEvent(String name, Page page, Object data) {
        sendEvent(new PageEvent(name, page, data));
    }

    private boolean isValidPage(Page page) {
        return page != null;
    }

    @Override
    public List<Page> getAvailablesPages() {
        return availablesPages;
    }

    public void setAvailablesPages(List<Page> availablesPages) {
        this.availablesPages = availablesPages;
    }

    @Override
    public void addAvailablePage(Page page) {
        availablesPages.add(page);
    }

    @Override
    public NavigationElement findElement(String path) {
        return container.findElement(path);
    }

    public void setRawCurrentPage(Page page) {
        this.currentPage = page;
        this.lastPage = null;
    }

    @Override
    public void reload() {
        setRawCurrentPage(null);
    }

    @Override
    public NavigationBuilder getCurrentNavigationBuilder() {
        return currentNavigationBuilder;
    }

    @Override
    public void setCurrentNavigationBuilder(NavigationBuilder currentNavigationBuilder) {
        this.currentNavigationBuilder = currentNavigationBuilder;
    }

    public Map<String, Object> getCurrentPageParams() {
        return currentPageParams;
    }
}
