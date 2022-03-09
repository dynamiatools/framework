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
package tools.dynamia.zk.navigation;

import navigation.builders.Menu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import tools.dynamia.navigation.Module;
import tools.dynamia.navigation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Mario A. Serrano Leones
 */
@org.springframework.stereotype.Component("navBuilder")
@Scope("prototype")
public class NavigationBuilder extends GenericForwardComposer {

    @Autowired
    private NavigationManager navManager;
    @Autowired
    private ModuleContainer moduleContainer;

    private String viewBuilderClass;
    private NavigationViewBuilder viewBuilder;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        viewBuilderClass = (String) self.getAttribute("builderClass");
        buildNavigation();
        ZKNavigationManager.getInstance().setCurrentNavBuilder(this);
    }

    private void buildNavigation() {
        this.viewBuilder = instanceNavigationBuilder();
        List<Module> modules = new ArrayList<>(moduleContainer.getModules());
        navManager.getAvailablesPages().clear();
        modules.sort(new ModuleComparator());
        for (Module module : modules) {
            if (NavigationRestrictions.allowAccess(module) && hasPagesWithAccess(module)) {
                viewBuilder.createModuleView(module);

                buildPages(module.getDefaultPageGroup(), viewBuilder);
                buildPageGroups(module, viewBuilder);
            }
        }
        Component container = (Component) viewBuilder.getNavigationView();
        container.setParent(self);
    }

    private boolean hasPagesWithAccess(Module module) {
        boolean allowed = module.getDefaultPageGroup().getPages().stream().anyMatch(NavigationRestrictions::allowAccess);

        if (!allowed) {
            for (PageGroup pg : module.getPageGroups()) {
                allowed = pg.getPages().stream().anyMatch(NavigationRestrictions::allowAccess);
                if (allowed) {
                    break;
                }
            }
        }

        return allowed;
    }

    private void buildPageGroups(Module module, NavigationViewBuilder viewBuilder) {
        for (PageGroup pageGroup : module.getPageGroups()) {
            buildPageGroupAndSubgroups(pageGroup, viewBuilder);
        }
    }

    private void buildPageGroupAndSubgroups(PageGroup pageGroup, NavigationViewBuilder viewBuilder) {
        if (NavigationRestrictions.allowAccess(pageGroup) && pageGroup.isVisible()) {
            viewBuilder.createPageGroupView(pageGroup);
            if (!pageGroup.getPageGroups().isEmpty()) {
                for (PageGroup subgroup : pageGroup.getPageGroups()) {
                    buildPageGroupAndSubgroups(subgroup, viewBuilder);
                }
            }
            buildPages(pageGroup, viewBuilder);
        }
    }

    private void buildPages(PageGroup pageGroup, NavigationViewBuilder viewBuilder) {
        Collection<Page> pages = pageGroup.getPages();
        for (Page p : pages) {
            if (NavigationRestrictions.allowAccess(p) && p.isVisible()) {
                viewBuilder.createPageView(p);
                navManager.addAvailablePage(p);
            }
        }
    }

    private NavigationViewBuilder instanceNavigationBuilder() {
        if (viewBuilderClass != null && !viewBuilderClass.isEmpty()) {
            try {
                Class clazz = Class.forName(viewBuilderClass);
                return (NavigationViewBuilder) clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Navigation view builder cannot be created: " + viewBuilderClass, e);
            }
        } else {
            return new Menu();
        }

    }

    public NavigationViewBuilder getViewBuilder() {
        return viewBuilder;
    }
}
