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

package tools.dynamia.zk.viewers;

import org.zkoss.zhtml.A;
import org.zkoss.zhtml.I;
import org.zkoss.zhtml.Li;
import org.zkoss.zhtml.Span;
import org.zkoss.zhtml.Text;
import org.zkoss.zhtml.Ul;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Menupopup;
import tools.dynamia.commons.Messages;
import tools.dynamia.navigation.Module;
import tools.dynamia.navigation.NavigationManager;
import tools.dynamia.navigation.NavigationViewBuilder;
import tools.dynamia.navigation.Page;
import tools.dynamia.navigation.PageGroup;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.ui.icons.IconType;
import tools.dynamia.ui.icons.IconsTheme;
import tools.dynamia.zk.util.ZKUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BootstrapMenuBuilder implements NavigationViewBuilder<Component> {

    private final Ul sidebar;
    private final Map<Module, Component> modulesContent = new HashMap<>();
    private final Map<PageGroup, Component> pgContent = new HashMap<>();
    private final Map<Page, Component> pageContent = new HashMap<>();
    private Menupopup contextMenu;
    private Page selectedPage;
    private final Locale locale = Messages.getDefaultLocale();

    public BootstrapMenuBuilder() {
        sidebar = new Ul();
        sidebar.setSclass("navbar-nav navbar-sidenav");

    }

    @Override
    public Component getNavigationView() {

        return sidebar;
    }

    @Override
    public void createModuleView(Module module) {

        if (module.getProperty("submenus") != Boolean.FALSE) {

            Li menu = new Li();
            menu.setSclass("nav-item");
            menu.setClientDataAttribute("toggle", "tooltip");
            menu.setClientDataAttribute("placement", "right");
            menu.setClientAttribute("title", module.getName());

            menu.setParent(sidebar);

            A mod = new A();
            mod.setSclass("nav-link  nav-link-collapse collapsed");
            mod.setDynamicProperty("href", "#" + module.getId());
            mod.setClientDataAttribute("toggle", "collapse");
            mod.setParent(menu);

            I icon = new I();
            icon.setParent(mod);

            Span text = new Span();
            text.appendChild(new Text(" " + module.getLocalizedName(locale)));
            text.setTitle(module.getLocalizedDescription(locale));
            text.setSclass("nav-link-text");
            text.setParent(mod);


            if (module.getIcon() != null && !module.getIcon().isEmpty()) {
                ZKUtil.configureComponentIcon(module.getLocalizedIcon(locale), icon, IconSize.SMALL);

                if (IconsTheme.get().getIcon(module.getLocalizedIcon(locale)).getType() == IconType.FONT) {
                    icon.setSclass(icon.getSclass() + " fa-fw");
                }
            }

            Ul submenu = new Ul();
            submenu.setSclass("sidenav-second-level collapse");
            submenu.setParent(menu);
            submenu.setId(module.getId());

            modulesContent.put(module, submenu);

        }
    }

    @Override
    public void createPageGroupView(PageGroup pageGroup) {
        boolean submenus = pageGroup.getParentModule() == null
                || pageGroup.getParentModule().getProperty("submenus") != Boolean.FALSE;
        if (submenus) {
            Ul menuPg = new Ul();
            menuPg.setSclass("sidenav-third-level collapse");
            menuPg.setId(pageGroup.getId());

            A pgItem = new A();
            pgItem.setClientDataAttribute("toggle", "collapse");
            pgItem.setDynamicProperty("href", "#" + pageGroup.getId());
            pgItem.setSclass("nav-link-collapse collapsed");

            Text label = new Text(" " + pageGroup.getLocalizedName(locale));
            label.setParent(pgItem);

            Li pgLi = new Li();
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
        pageContent.put(page, pageli);
        org.zkoss.zul.A pageitem = new org.zkoss.zul.A();
        pageitem.getAttributes().put("page", page);
        pageitem.addEventListener(Events.ON_CLICK, evt -> {
            Li currentPageLi = (Li) pageContent.get(NavigationManager.getCurrent().getCurrentPage());
            if (currentPageLi != null) {
                currentPageLi.setSclass(null);
            }

            NavigationManager.getCurrent().setCurrentPage(page);
            pageli.setSclass("active");
        });


        Text label = new Text(page.getLocalizedName(locale));

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
