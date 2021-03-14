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

import org.zkoss.zhtml.Div;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;
import tools.dynamia.navigation.NavigationManager;
import tools.dynamia.navigation.NavigationViewBuilder;
import tools.dynamia.navigation.Page;
import tools.dynamia.navigation.PageGroup;
import tools.dynamia.navigation.Module;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mario A. Serrano Leones
 */
public class Accordion implements NavigationViewBuilder<Tabbox> {

    private transient Tabbox tabbox;
    private transient Map<Module, Vbox> modulesContent = new HashMap<>();

    public Accordion() {
        tabbox = new Tabbox();
        tabbox.setWidth("100%");
        tabbox.setMold("accordion-lite");

        Tabs tabs = new Tabs();
        tabs.setParent(tabbox);
        Tabpanels panels = new Tabpanels();
        panels.setParent(tabbox);
    }

    @Override
    public Tabbox getNavigationView() {
        return tabbox;
    }

    @Override
    public void createModuleView(Module module) {
        Tab tab = new Tab();
        tab.setLabel(module.getName());
        tab.setImage(module.getIcon());
        tab.setParent(tabbox.getTabs());

        Tabpanel tp = new Tabpanel();
        tp.setParent(tabbox.getTabpanels());
        Vbox box = new Vbox();
        box.setWidth("100%");
        box.setParent(tp);
        modulesContent.put(module, box);
    }

    @Override
    public void createPageGroupView(PageGroup pageGroup) {
        if (pageGroup.getParentModule().getPageGroups().size() > 1) {
            Vbox vbox = modulesContent.get(pageGroup.getParentModule());
            vbox.setSclass("nav-pagegroup");
            Div div = new Div();
            div.setParent(vbox);
            div.setSclass("nav-pagegroup-title");

            new Label(pageGroup.getName()).setParent(div);

        }
    }

    @Override
    public void createPageView(Page page) {
        Vbox vbox = modulesContent.get(page.getPageGroup().getParentModule());
        A link = new A(page.getName());
        link.getAttributes().put("page", page);
        link.setParent(vbox);
        link.addEventListener(Events.ON_CLICK, evt -> NavigationManager.getCurrent().setCurrentPage(page));
    }
}
