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
package tools.dynamia.zk.navigation;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.Containers;
import tools.dynamia.navigation.BaseNavigationManager;
import tools.dynamia.navigation.ModuleContainer;
import tools.dynamia.navigation.NavigationManagerSession;
import tools.dynamia.navigation.Page;
import tools.dynamia.navigation.PageEvent;
import tools.dynamia.zk.util.ZKUtil;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Mario A. Serrano Leones
 */
@Component("navManager")
@Scope("zk-desktop")
public class ZKNavigationManager extends BaseNavigationManager implements Serializable {

    private static final LoggingService LOGGER = new SLF4JLoggingService(ZKNavigationManager.class);

    /**
     *
     */
    static final String QUEUE_NAME = "PageNavigationQueue";
    static final String ON_PAGE_CHANGED = "onPageChanged";
    static final String ON_PAGE_CLOSED = "onPageClosed";

    private static final long serialVersionUID = 1L;
    private transient ZKNavigationComposer currentComposer;


    public static ZKNavigationManager getInstance() {
        return Containers.get().findObject(ZKNavigationManager.class);
    }

    private transient Desktop currentDesktop;
    private boolean autoSyncClientURL = true;


    @Autowired
    public ZKNavigationManager(ModuleContainer container) {
        super(container);
    }

    @PostConstruct
    public void init() {
        LOGGER.info("Initializing new " + getClass().getSimpleName() + " for desktop " + ZKUtil.getCurrentDesktop());

        NavigationManagerSession.getInstance().updateNavManager(this);

    }

    @Override
    public boolean setCurrentPage(Page newPage, Map<String, Serializable> params) {
        boolean ok = super.setCurrentPage(newPage, params);
        if (ok) {
            notityComposer(new PageEvent(ON_PAGE_CHANGED, newPage, newPage, params));
        }
        return ok;
    }

    @Override
    public void closePage(Page page) {
        if (page != null && page.isClosable()) {
            notityComposer(new PageEvent(ON_PAGE_CLOSED, page));
            setRawCurrentPage(getLastPage());
        }
    }

    private void notityComposer(PageEvent evt) {
        if (currentComposer != null) {
            currentComposer.handleEvent(evt);
        }
    }

    void notifyPageClose(Page page) {
        super.fireOnPageClose(page);
    }

    public Desktop getCurrentDesktop() {
        return currentDesktop;
    }

    public void setCurrentDesktop(Desktop currentDesktop) {
        this.currentDesktop = currentDesktop;
    }

    public boolean isAutoSyncClientURL() {
        return autoSyncClientURL;
    }

    public void setAutoSyncClientURL(boolean autoSyncClientURL) {
        this.autoSyncClientURL = autoSyncClientURL;
    }


    @Override
    public void sendEvent(PageEvent evt) {
        ZKUtil.eventQueuePublish(evt.getPage().getVirtualPath(), new Event("pageEvent", null, evt));
    }

    @Override
    public void onPageEvent(Page targetPage, Consumer<PageEvent> evt) {
        EventQueues.lookup(targetPage.getVirtualPath(), true).subscribe(e -> {
            PageEvent pageEvent = (PageEvent) e.getData();
            if (evt != null && pageEvent != null) {
                evt.accept(pageEvent);
            }
        });
    }

    @Override
    public void clearPageEvents(Page page) {
        if (page != null) {
            String name = page.getVirtualPath();
            var queue = EventQueues.lookup(name, false);
            if (queue != null) {
                queue.close();
            }
        }
    }

    public void setCurrentComposer(ZKNavigationComposer currentComposer) {
        this.currentComposer = currentComposer;
        if (ZKUtil.isInEventListener()) {
            NavigationManagerSession.getInstance().executeQueue();
        }
    }

    public ZKNavigationComposer getCurrentComposer() {
        return currentComposer;
    }


}
