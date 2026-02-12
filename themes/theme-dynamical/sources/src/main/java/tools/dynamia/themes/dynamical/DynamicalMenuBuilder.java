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

package tools.dynamia.themes.dynamical;

import org.zkoss.zhtml.*;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;

import org.zkoss.zul.Menupopup;
import tools.dynamia.commons.Messages;
import tools.dynamia.navigation.Module;
import tools.dynamia.navigation.*;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.zk.util.ZKUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DynamicalMenuBuilder implements NavigationViewBuilder<Component>, Serializable {

    private Ul sidebar;
    private Map<Module, Component> modulesContent = new HashMap<>();
    private Map<PageGroup, Component> pgContent = new HashMap<>();
    private Map<Page, Component> pageContent = new HashMap<>();
    private Menupopup contextMenu;
    private Page selectedPage;
    private Locale locale = Messages.getDefaultLocale();
    private Module firstModule;

    public DynamicalMenuBuilder() {
        sidebar = new Ul();
        sidebar.setSclass("nav sidebar-menu flex-column");
        sidebar.setClientDataAttribute("lte-toggle", "treeview");
        sidebar.setClientDataAttribute("accordion", "false");
        sidebar.setClientAttribute("role","menu");

    }


    @Override
    public Component getNavigationView() {
        return sidebar;
    }

    @Override
    public void createModuleView(Module module) {

        if (firstModule == null) {
            firstModule = module;
        }

        if (module.getProperty("submenus") != Boolean.FALSE) {


            Li menu = new Li();
            menu.setSclass("nav-item");
            menu.setParent(sidebar);

            A a = new A();
            a.setSclass("nav-link");
            a.setDynamicProperty("href", "#");
            a.setParent(menu);
            a.setTitle(module.getLocalizedDescription(locale));

            I icon = new I();
            icon.setParent(a);
            icon.setSclass("nav-icon");

            if (module.getIcon() != null && !module.getIcon().isEmpty()) {
                ZKUtil.configureComponentIcon(module.getLocalizedIcon(locale), icon, IconSize.SMALL);
                icon.setSclass("nav-icon " + icon.getSclass());
            }

            P label = new P();
            label.appendChild(new Text(" " + module.getLocalizedName(locale)));
            label.setParent(a);

            I angle = new I();
            angle.setSclass("nav-arrow fas fa-angle-right");
            angle.setParent(a);

            Ul submenu = new Ul();
            submenu.setSclass("nav nav-treeview");
            submenu.setParent(menu);

            modulesContent.put(module, submenu);
        }
    }

    @Override
    public void createPageGroupView(PageGroup pageGroup) {
        boolean submenus = true;
        if (pageGroup.getParentModule() != null
                && pageGroup.getParentModule().getProperty("submenus") == Boolean.FALSE) {
            submenus = false;
        }
        if (submenus) {
            Ul menuPg = new Ul();
            menuPg.setSclass("nav nav-treeview");

            A pgItem = new A();
            pgItem.setDynamicProperty("href", "#");
            pgItem.setSclass("nav-link");

            I pgIcon = new I();

            pgIcon.setSclass("nav-icon fa fa-plus-square");
            pgIcon.setParent(pgItem);

            P label = new P();
            label.appendChild(new Text(" " + pageGroup.getLocalizedName(locale)));
            label.setParent(pgItem);


            I pgAngle = new I();
            pgAngle.setSclass("nav-arrow fas fa-angle-right");
            pgAngle.setParent(pgItem);

            Li pgLi = new Li();
            pgLi.setSclass("nav-item");
            pgItem.setParent(pgLi);

            Component menu = null;
            if (pageGroup.getParentModule() != null) {
                menu = modulesContent.get(pageGroup.getParentModule());
            } else {
                menu = pgContent.get(pageGroup.getParentGroup());
            }

            pgLi.setParent(menu);
            menuPg.setParent(pgLi);
            pgContent.put(pageGroup, menuPg);
        }
    }

    @Override
    public void createPageView(Page page) {
        Component menu = null;
        if (page.getPageGroup().getParentModule() != null) {
            menu = modulesContent.get(page.getPageGroup().getParentModule());
        } else {
            menu = pgContent.get(page.getPageGroup().getParentGroup());
        }

        Component menuPg = pgContent.get(page.getPageGroup());

        Li pageli = new Li();
        pageli.setSclass("nav-item");

        pageContent.put(page, pageli);

        var pageitem = new org.zkoss.zul.A();
        pageitem.setContext(contextMenu);
        pageitem.setZclass("nav-link");
        pageitem.getAttributes().put("page", page);

        pageitem.addEventListener(Events.ON_CLICK, evt -> {
            try {
                NavigationManager.getCurrent().reload();
                NavigationManager.getCurrent().setCurrentPage(page);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        I pageicon = new I();
        pageicon.setParent(pageitem);
        if (page.getIcon() != null && !page.getIcon().isEmpty()) {
            ZKUtil.configureComponentIcon(page.getLocalizedIcon(locale), pageicon, IconSize.SMALL);
            pageicon.setSclass("nav-icon " + pageicon.getSclass());
        } else {
            pageicon.setSclass("nav-icon far fa-circle");
        }

        P label = new P();
        label.appendChild(new Text(" " + page.getLocalizedName(locale)));
        label.setParent(pageitem);


        pageitem.setParent(pageli);

        Component pageParent = sidebar;
        if (menuPg != null) {
            pageParent = menuPg;
        } else if (menu != null) {
            pageParent = menu;
        }

        pageli.setParent(pageParent);

    }
}
