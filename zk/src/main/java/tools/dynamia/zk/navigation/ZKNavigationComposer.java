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


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.zhtml.Text;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Label;
import org.zkoss.zul.impl.LabelElement;
import org.zkoss.zul.impl.XulElement;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.Containers;
import tools.dynamia.navigation.*;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.workspace.builders.TabPanel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mario A. Serrano Leones
 */
@Component("navComposer")
@Scope("prototype")
public class ZKNavigationComposer extends SelectorComposer<org.zkoss.zk.ui.Component> {

    /**
     *
     */
    private static final long serialVersionUID = -6459772159875108265L;

    private static final NavigationElement EMPTY = new NavigationElement("", "");
    private LoggingService logger = new SLF4JLoggingService(ZKNavigationComposer.class);

    @Wire("#workspace")
    private XulElement workspace;
    @Wire(".pageTitle")
    private List<org.zkoss.zk.ui.Component> pageTitles;
    @Wire(".pageGroupTitle")
    private List<org.zkoss.zk.ui.Component> pageGroupTitles;
    @Wire(".moduleTitle")
    private List<org.zkoss.zk.ui.Component> moduleTitles;

    private WorkspaceViewBuilder<org.zkoss.zk.ui.Component> workspaceViewBuilder;


    private org.zkoss.zk.ui.Component self;
    private Page desktopCurrentPage;


    public void handleEvent(PageEvent evt) {
        if (evt != null && ZKUtil.isInEventListener()) {
            String name = evt.getName();
            switch (name) {
                case ZKNavigationManager.ON_PAGE_CHANGED -> {
                    desktopCurrentPage = evt.getPage();
                    update(evt.getParams());
                }
                case ZKNavigationManager.ON_PAGE_CLOSED -> {
                    Page page = evt.getPage();
                    getWorkspaceViewBuilder().close(page);
                    navManager().notifyPageClose(page);
                    if (page != null && page.equals(desktopCurrentPage)) {
                        desktopCurrentPage = null;
                    }
                }
                default -> {
                }
            }
        }
    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        super.doAfterCompose(comp);
        this.self = comp;

        navManager().setCurrentDesktop(comp.getDesktop());
        navManager().setCurrentComposer(this);
        buildWorkspace();


        desktopCurrentPage = navManager().getCurrentPage();
        Map<String, Serializable> pageParams = navManager().getCurrentPageParams();
        if (desktopCurrentPage == null) {
            loadDefaultPage();
            pageParams = null;
        }

        this.self.addEventListener("onHash", evt -> processHash(evt.getData()));
        String function = "sendMeHash('" + self.getUuid() + "')";
        Clients.evalJavaScript(function);


        if (desktopCurrentPage != null && !(desktopCurrentPage instanceof ActionPage)) {
            if (pageParams == null) {
                pageParams = new HashMap<>();
            }
            update(pageParams);
        }
    }

    private void loadDefaultPage() {
        var defaultPageProvider = Containers.get().findObject(DefaultPageProvider.class);
        if (defaultPageProvider != null && defaultPageProvider.getPath() != null) {
            try {
                desktopCurrentPage = navManager().findPage(defaultPageProvider.getPath());
                navManager().setRawCurrentPage(desktopCurrentPage);
            } catch (PageNotFoundException e) {
                logger.warn("Default page not found: " + defaultPageProvider.getPath());
            }
        }

        if (desktopCurrentPage == null) {
            String defaultPagePath = ModuleContainer.getInstance().getDefaultPagePath();
            if (defaultPagePath != null) {
                desktopCurrentPage = navManager().findPage(defaultPagePath);
            }
        }
    }

    private static ZKNavigationManager navManager() {
        return ZKNavigationManager.getInstance();
    }

    private void processHash(Object data) {

        if (data != null) {
            try {
                Page page = navManager().findPageByPrettyVirtualPath(data.toString());
                navManager().setCurrentPage(page);
            } catch (Exception e) {
                // ignore
            }
        }

    }

    @Override
    public org.zkoss.zk.ui.Component getSelf() {
        return self;
    }

    void update(Map<String, Serializable> params) {
        if (desktopCurrentPage instanceof ActionPage) {
            ((ActionPage) desktopCurrentPage).execute();
            return;
        }

        if (workspaceViewBuilder != null) {
            workspaceViewBuilder.update(desktopCurrentPage, params);
        }

        if (desktopCurrentPage != null && !desktopCurrentPage.isShowAsPopup()) {
            boolean updateURL = navManager().isAutoSyncClientURL();
            if (params != null && params.get("autoSyncClientURL") == Boolean.FALSE) {
                updateURL = false;
            }

            if (desktopCurrentPage.isTemporal()) {
                updateURL = false;
            }

            if (updateURL) {
                updateClientURL();
            }
            updatePageTitle();
        }
    }

    public void updateClientURL() {
        if (desktopCurrentPage != null) {
            Clients.evalJavaScript("changeHash('" + desktopCurrentPage.getPrettyVirtualPath() + "');");
        }
    }

    private void updatePageTitle() {
        NavigationElement page = EMPTY;
        NavigationElement pageGroup = EMPTY;
        NavigationElement module = EMPTY;

        page = desktopCurrentPage;
        if (desktopCurrentPage.getPageGroup() != null) {
            pageGroup = desktopCurrentPage.getPageGroup();

            if (pageGroup != null) {
                module = desktopCurrentPage.getPageGroup().getParentModule();
            }
        }

        final NavigationElement finalPage = page;
        final NavigationElement finalPageGroup = pageGroup;
        final NavigationElement finalModule = module;
        pageTitles.forEach(c -> setTitleValue(c, finalPage));
        pageGroupTitles.forEach(c -> setTitleValue(c, finalPageGroup));
        moduleTitles.forEach(c -> setTitleValue(c, finalModule));

    }

    private void setTitleValue(org.zkoss.zk.ui.Component titleComp, NavigationElement navigationElement) {
        if (titleComp == null || navigationElement == null) {
            return;
        }

        String text = navigationElement.getName();

        if (titleComp instanceof Label label) {
            label.setValue(text);
        } else if (titleComp instanceof LabelElement element) {
            element.setLabel(text);
        } else {
            titleComp.getChildren().clear();
            titleComp.appendChild(new Text(text));
        }

    }

    private void buildWorkspace() throws ClassNotFoundException {
        if (workspace != null) {
            String builderClass = (String) workspace.getAttribute("builderClass");
            if (builderClass != null && builderClass.startsWith("workspace.builders")) {
                builderClass = "tools.dynamia.zk." + builderClass;
            }
            Class builder = null;
            if (builderClass != null) {
                builder = Class.forName(builderClass);
            } else {
                builder = TabPanel.class;
            }
            //noinspection unchecked
            workspaceViewBuilder = (WorkspaceViewBuilder) BeanUtils.newInstance(builder);
            workspaceViewBuilder.init(workspace);
            workspaceViewBuilder.build(desktopCurrentPage);
        }
    }


    public WorkspaceViewBuilder getWorkspaceViewBuilder() {
        return workspaceViewBuilder;
    }
}
