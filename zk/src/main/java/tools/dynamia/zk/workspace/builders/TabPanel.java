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
package tools.dynamia.zk.workspace.builders;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tabpanels;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.Toolbar;
import org.zkoss.zul.Toolbarbutton;
import tools.dynamia.commons.Messages;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.navigation.NavigationManager;
import tools.dynamia.navigation.Page;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.ui.icons.IconsTheme;
import tools.dynamia.zk.AbstractZKWorkspaceBuilder;
import tools.dynamia.zk.navigation.ZKNavigationManager;
import tools.dynamia.zk.util.ZKUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Mario A. Serrano Leones
 */
public class TabPanel extends AbstractZKWorkspaceBuilder implements Serializable {

    private final LoggingService logger = new SLF4JLoggingService();
    private static final String TAB_PAGE = "tab-page";
    private final Tabbox tabbox;
    private final Locale locale = Messages.getDefaultLocale();

    public TabPanel() {
        tabbox = new Tabbox();
        tabbox.setVflex("1");

        new Tabs().setParent(tabbox);
        new Tabpanels().setParent(tabbox);
        createToolbar();

        tabbox.addEventListener(Events.ON_SELECT, event -> {
            Page page = (Page) tabbox.getSelectedTab().getAttributes().get(TAB_PAGE);
            if (page != null) {
                NavigationManager.getCurrent().setCurrentPage(page);
            }
        });

    }

    @Override
    public void init(Component container) {
        super.init(container);
        tabbox.setParent(container);
    }

    private void createToolbar() {
        Toolbar toolbar = new Toolbar();
        toolbar.setParent(tabbox);
        IconsTheme iconsTheme = IconsTheme.get();
        {
            // Refresh button
            Toolbarbutton btn = new Toolbarbutton();
            btn.setParent(toolbar);
            btn.setTooltiptext(Messages.get(TabPanel.class, "tabRefresh"));
            btn.addEventListener(Events.ON_CLICK, event -> refreshSelectedPage());
            ZKUtil.configureComponentIcon(iconsTheme.getIcon("view-refresh"), btn, IconSize.NORMAL);
        }

        {
            // Close all but selected button
            Toolbarbutton btn = new Toolbarbutton();
            btn.setParent(toolbar);
            btn.setSclass("hidden-xs");
            btn.setTooltiptext(Messages.get(TabPanel.class, "tabCloseOthers"));
            btn.addEventListener(Events.ON_CLICK, event -> closeAllPagesButSelected());
            ZKUtil.configureComponentIcon(iconsTheme.getIcon("window-close"), btn, IconSize.NORMAL);
        }

        {
            // Close all button
            Toolbarbutton btn = new Toolbarbutton();
            btn.setParent(toolbar);
            btn.setSclass("hidden-xs");
            btn.setTooltiptext(Messages.get(TabPanel.class, "tabCloseAll"));
            btn.addEventListener(Events.ON_CLICK, event -> closeAllPages());
            ZKUtil.configureComponentIcon(iconsTheme.getIcon("window-close-all"), btn, IconSize.NORMAL);
        }

    }

    @Override
    public void build(Page page) {
        update(page, null);
    }

    @Override
    public void update(Page page, Map<String, Serializable> params) {
        if (page != null && page.isShowAsPopup()) {
            super.update(page, params);
        } else if (tabbox != null) {
            if (page != null) {
                Tab tab = getTab(page);
                if (tab == null) {
                    super.update(page, params);
                } else {
                    tabbox.setSelectedTab(tab);
                    ZKNavigationManager.getInstance().setRawCurrentPage(page);
                }
            }
        }
    }

    @Override
    public void clearPageContainer(Component pageContainer) {
        if (pageContainer instanceof Tabpanel) {
            pageContainer.getChildren().clear();
        }
    }

    @Override
    public Component getPageContainer(Page page) {
        if (page.isShowAsPopup()) {
            return container;
        } else {
            String label = page.getLocalizedName(locale);

            if (page.getLongName() != null) {
                label = page.getLongName();
            } else if (page.getLongNameSupplier() != null) {
                label = page.getLongNameSupplier().get();
            }

            label = filterPageLabel(page, locale, label);

            Tab tab = new Tab(label);
            tab.getAttributes().put(TAB_PAGE, page);
            tab.setClosable(page.isClosable());
            ZKUtil.configureComponentIcon(page.getIcon(), tab, IconSize.SMALL);

            tab.setSclass("tabpage");
            tab.addSclass("page-" + page.getId());
            tab.setTooltiptext(page.getLocalizedDescription(locale));

            final Tabpanel panel = new Tabpanel();
            panel.setSclass("tabpanel");
            panel.addSclass("page-" + page.getId());
            tabbox.getTabs().appendChild(tab);
            tabbox.getTabpanels().appendChild(panel);

            return panel;
        }
    }

    protected String filterPageLabel(Page page, Locale locale, String label) {
        return label;
    }

    @Override
    protected void postUpdate(Component pageComponent, Page page, Map<String, Serializable> params) {
        if (pageComponent.getParent() instanceof Tabpanel tabpanel) {
            if (tabpanel == null) {
                return;
            }
            Tab tab = tabpanel.getLinkedTab();
            if (tab == null) {
                //tab no longer exists, abort mission
                return;
            }

            if (params != null) {
                if (params.get("PAGE_TITLE") != null) {
                    tab.setLabel(params.get("PAGE_TITLE").toString());
                }
                if (params.get("NEW_PAGE") == Boolean.TRUE) {
                    tab.getAttributes().remove(TAB_PAGE);
                }
            }

            if (page.getAttribute("title") != null) {
                tab.setLabel((String) page.getAttribute("title"));
            }
            tabbox.setSelectedTab(tab);
        }
    }

    private Tab getTab(Page page) {
        for (Object object : tabbox.getTabs().getChildren()) {
            Tab tab = (Tab) object;
            if (tab.getAttributes().containsKey(TAB_PAGE)) {
                Page pageTab = (Page) tab.getAttribute(TAB_PAGE);
                if (page.getVirtualPath().equals(pageTab.getVirtualPath())) {
                    return tab;
                }
            }
        }
        return null;
    }

    public void closeAllPages() {
        try {
            if (tabbox != null) {
                List<Tab> toClose = new ArrayList<>();
                for (Object object : tabbox.getTabs().getChildren()) {
                    Tab tab = (Tab) object;
                    if (tab.isClosable()) {
                        toClose.add(tab);
                    }
                }
                closeTabs(toClose);
            }
        } catch (Exception e) {
            logger.error("Error closing all Pages", e);
        }
    }

    public void closeAllPagesButSelected() {
        try {
            if (tabbox != null) {
                List<Tab> toClose = new ArrayList<>();
                for (Object object : tabbox.getTabs().getChildren()) {
                    Tab tab = (Tab) object;
                    if (tab.isClosable() && !tab.isSelected()) {
                        toClose.add(tab);
                    }
                }
                closeTabs(toClose);
            }
        } catch (Exception e) {
            logger.error("Error closing all Pages but selected", e);
        }
    }

    private void closeTabs(List<Tab> tabs) {
        if (tabs != null) {
            for (Tab tab : tabs) {
                tab.onClose();
            }
        }
    }

    private void refreshSelectedPage() {
        Tab tab = tabbox.getSelectedTab();
        if (tab != null) {
            Page page = (Page) tab.getAttribute(TAB_PAGE);
            Tabpanel panel = tab.getLinkedPanel();
            if (page != null && panel != null) {
                panel.getChildren().clear();
                renderPage(panel, page, null);
            }
        }
    }

    @Override
    public void close(Page page) {
        if (page != null) {
            if (page.isShowAsPopup() && currentWindow != null) {
                currentWindow.detach();
                currentWindow = null;
            } else if (!page.isShowAsPopup()) {
                var tab = getTab(page);
                if (tab != null) {
                    tab.close();
                }
            }
        }
    }
}
