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
package tools.dynamia.zk;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Html;
import org.zkoss.zul.Iframe;
import org.zkoss.zul.Window;
import tools.dynamia.navigation.Page;
import tools.dynamia.navigation.RendereablePage;
import tools.dynamia.navigation.WorkspaceViewBuilder;
import tools.dynamia.zk.ui.DivContainer;
import tools.dynamia.zk.util.ZKUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractZKWorkspaceBuilder implements WorkspaceViewBuilder<Component> {

    protected Component container;
    protected Window currentWindow;

    @Override
    public void init(Component container) {
        this.container = container;
    }

    @Override
    public void update(Page page, Map<String, Serializable> params) {
        if (page != null) {
            Component pageContainer = getPageContainer(page);
            if (pageContainer == null) {
                pageContainer = container;
            }
            currentWindow = null;
            if (page.isShowAsPopup()) {
                currentWindow = new Window(page.getName(), "normal", page.isClosable());
                currentWindow.setPage(ZKUtil.getFirstPage());
                currentWindow.setSclass("pageContentWindow");
                currentWindow.setContentStyle("flex-direction: unset");
                pageContainer = currentWindow;
                currentWindow.addEventListener(Events.ON_CLOSE, evt -> close(page));

                currentWindow.setWidth((String) page.getAttribute("width"));
                currentWindow.setHeight((String) page.getAttribute("height"));

                String title = (String) page.getAttribute("title");
                if (title != null && !title.isEmpty()) {
                    currentWindow.setTitle(title);
                }

                Boolean showTitle = (Boolean) page.getAttribute("showTitle");
                if (showTitle == Boolean.FALSE) {
                    if (currentWindow.getCaption() != null) {
                        currentWindow.getCaption().detach();
                    }
                    currentWindow.setBorder(false);
                    currentWindow.setClosable(false);
                    currentWindow.setTitle(null);
                }

                currentWindow.doModal();
            } else {
                clearPageContainer(pageContainer);
            }

            Component pageComponent = renderPage(pageContainer, page, params);
            if (pageComponent != null) {
                postUpdate(pageComponent, page, params);
            }
        }
    }

    public abstract Component getPageContainer(Page page);

    public abstract void clearPageContainer(Component pageContainer);

    protected void postUpdate(Component pageComponent, Page page, Map<String, Serializable> params) {

    }

    protected Component renderPage(Component pageContainer, Page page, Map<String, Serializable> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put("parentWindow", currentWindow);
        params.put("navigationPage", page);
        params.put("page", page);


        if (page instanceof RendereablePage renderPage) {
            Component comp = (Component) renderPage.renderPage();
            if (comp != null) {
                comp.setParent(pageContainer);
            }
            return comp;
        } else {
            DivContainer pageContent = new DivContainer();
            pageContent.setSclass("pageContent");
            pageContent.setParent(pageContainer);
            if (page.isShowAsPopup()) {
                pageContent.setVflex("1");

            }
            if (page.isHtml()) {
                Iframe iframe = new Iframe(page.getPath());
                iframe.setParent(pageContent);
                iframe.setHflex("1");
                iframe.setVflex("1");
            } else {
                ZKUtil.createComponent(page.getPath(), pageContent, params);
            }
            return pageContent;
        }
    }

    @Override
    public void close(Page page) {
        if (currentWindow != null) {
            currentWindow.detach();
        } else if (container != null) {
            container.getChildren().clear();
        }
    }

}
