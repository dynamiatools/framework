/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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




package navigation.builders;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Menubar;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import tools.dynamia.navigation.*;
import tools.dynamia.navigation.Module;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class Menu implements NavigationViewBuilder<Menubar> {

    private final transient Menubar menubar;
    private final transient Map<Module, org.zkoss.zul.Menu> modulesContent = new HashMap<>();
    private final transient Map<PageGroup, org.zkoss.zul.Menu> pgContent = new HashMap<>();

    public Menu() {
        menubar = new Menubar();
        menubar.setWidth("100%");
        menubar.setAutodrop(true);
        menubar.setSclass("menuNavBuilder");
    }

    @Override
    public Menubar getNavigationView() {
        return menubar;
    }

    @Override
    public void createModuleView(Module module) {

        if (module.getProperty("submenus") != Boolean.FALSE) {

            org.zkoss.zul.Menu menu = new org.zkoss.zul.Menu(module.getName());
            menu.setLabel(module.getName());
            menu.setImage(module.getIcon());
            menu.setParent(menubar);
            new Menupopup().setParent(menu);

            modulesContent.put(module, menu);
        }
    }

    @Override
    public void createPageGroupView(PageGroup pageGroup) {
        boolean submenus = true;
        if (pageGroup.getParentModule() != null && pageGroup.getParentModule().getProperty("submenus") == Boolean.FALSE) {
            submenus = false;
        }
        if (submenus) {
            org.zkoss.zul.Menu menuPg = new org.zkoss.zul.Menu(pageGroup.getName());
            new Menupopup().setParent(menuPg);
            org.zkoss.zul.Menu menu = null;
            if (pageGroup.getParentModule() != null) {
                menu = modulesContent.get(pageGroup.getParentModule());
            } else {
                menu = pgContent.get(pageGroup.getParentGroup());
            }

            menuPg.setParent(menu.getMenupopup());

            pgContent.put(pageGroup, menuPg);
        }
    }

    @Override
    public void createPageView(Page page) {
        org.zkoss.zul.Menu menu = null;
        if (page.getPageGroup().getParentModule() != null) {
            menu = modulesContent.get(page.getPageGroup().getParentModule());
        } else {
            menu = pgContent.get(page.getPageGroup().getParentGroup());
        }

        org.zkoss.zul.Menu menuPg = pgContent.get(page.getPageGroup());

        Menuitem menuitem = new Menuitem(page.getName());
        menuitem.getAttributes().put("page", page);
        menuitem.addEventListener(Events.ON_CLICK, evt -> NavigationManager.getCurrent().setCurrentPage(page));
        if (menuPg != null) {
            menuitem.setParent(menuPg.getMenupopup());
        } else if (menu != null) {
            menuitem.setParent(menu.getMenupopup());
        } else {
            menuitem.setParent(menubar);
        }

    }
}
