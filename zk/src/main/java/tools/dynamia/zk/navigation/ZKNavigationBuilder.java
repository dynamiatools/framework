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

import navigation.builders.Menu;
import org.springframework.context.annotation.Scope;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.Composer;
import tools.dynamia.navigation.NavigationBuilder;
import tools.dynamia.navigation.NavigationViewBuilder;

/**
 * @author Mario A. Serrano Leones
 */
@org.springframework.stereotype.Component("navBuilder")
@Scope("prototype")
public class ZKNavigationBuilder extends NavigationBuilder implements Composer<Component> {

    private Component component;

    @AfterCompose
    public void doAfterCompose(Component comp) {
        this.component = comp;
        setViewBuilderClass((String) comp.getAttribute("builderClass"));
        init();
    }


    @Override
    protected void showNavigation(Object navigationView) {
        if (component != null && navigationView instanceof Component) {
            ((Component) navigationView).setParent(component);
        }
    }

    @Override
    protected NavigationViewBuilder defaultViewVuilder() {
        return new Menu();
    }

}
