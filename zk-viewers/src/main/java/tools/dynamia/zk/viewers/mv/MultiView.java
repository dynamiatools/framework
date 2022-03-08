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

package tools.dynamia.zk.viewers.mv;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;
import tools.dynamia.viewers.View;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class MultiView<T> extends Tabbox implements View<T>, EventListener<Event> {

    public static final String MULTI_VIEW_LISTENER = "multiViewListener";
    public static final String VIEW_LOADER = "viewLoader";

    static {
        BindingComponentIndex.getInstance().put("value", MultiView.class);
        ComponentAliasIndex.getInstance().add(MultiView.class);
    }

    /**
     *
     */
    private static final long serialVersionUID = 7394287706848766562L;
    private T value;
    private ViewDescriptor viewDescriptor;

    private final List<View> subviews = new ArrayList<>();
    private View parentView;
    private Object source;

    public MultiView() {
        new Tabpanels().setParent(this);
        new Tabs().setParent(this);
        addEventListener(Events.ON_SELECT, this);
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public void setViewDescriptor(ViewDescriptor viewDescriptor) {
        this.viewDescriptor = viewDescriptor;
    }

    @Override
    public ViewDescriptor getViewDescriptor() {
        return viewDescriptor;
    }

    public void addView(String label, View view) {
        createTab(label).getLinkedPanel().appendChild((Component) view);
        view.setParentView(this);
    }

    public void addView(String label, ViewLoader viewLoader) {
        addView(label, null, viewLoader);
    }

    public void addView(String label, MultiViewListener listener, ViewLoader viewLoader) {
        addView(label, listener, viewLoader, false);
    }

    public void addView(String label, ViewLoader viewLoader, boolean loadInmediatly) {
        addView(label, null, viewLoader, loadInmediatly);
    }

    public void addView(String label, MultiViewListener listener, ViewLoader viewLoader, boolean loadInmediatly) {
        Tab tab = createTab(label);
        tab.setAttribute("viewLoader", viewLoader);
        tab.setAttribute("multiViewListener", listener);

        if (loadInmediatly) {
            loadTab(tab);
        }
    }

    @Override
    public void onEvent(Event event) {
        Tab tab = getSelectedTab();
        loadTab(tab);
    }

    protected Tab createTab(String label) {
        Tab tab = new Tab();
        tab.setLabel(label);
        tab.addSclass(label.toLowerCase().replace(" ", "-") + "-tab");
        getTabs().appendChild(tab);

        Tabpanel panel = new Tabpanel();
        panel.setVflex("1");
        panel.setSclass("multiview-panel");
        getTabpanels().appendChild(panel);
        checkTabs();
        return tab;
    }

    protected void loadTab(Tab tab) {
        View subview = null;
        Tabpanel panel = tab.getLinkedPanel();
        MultiViewListener listener = (MultiViewListener) tab.getAttribute(MULTI_VIEW_LISTENER);

        if (panel.getChildren().isEmpty()) {
            ViewLoader loader = (ViewLoader) tab.getAttribute(VIEW_LOADER);
            if (loader != null) {
                subview = loader.loadSubview(this);
                subview.setParentView(this);
                panel.appendChild((Component) subview);
                subviews.add(subview);

                if (listener != null) {
                    listener.subviewLoaded(this, subview);
                }
            }
        } else {
            subview = (View) panel.getFirstChild();
        }

        if (listener != null) {
            listener.subviewSelected(this, subview);
        }
    }

    private void checkTabs() {
        if (getTabs().getChildren().size() == 1) {
            getTabs().setStyle("display: none !important");

        } else {
            getTabs().setStyle("");
        }
    }

    public List<View> getSubviews() {
        return subviews;
    }

    @Override
    public View getParentView() {
        return parentView;
    }

    @Override
    public void setParentView(View parentView) {
        this.parentView = parentView;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public void setSource(Object source) {
        this.source = source;
    }
}
