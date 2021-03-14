
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

package tools.dynamia.zk.viewers;

import org.zkoss.bind.Binder;
import org.zkoss.bind.DefaultBinder;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Div;
import tools.dynamia.viewers.View;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.zk.util.ZKBindingUtil;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class ZKWrapperView<T> extends Div implements View<T> {

    /**
     *
     */
    private static final long serialVersionUID = 2791320828193575786L;
    private static final String ZK_WRAPPER_BINDER = "ZK-WRAPPER-BINDER";
    private View<T> view;
    private ViewDescriptor viewDescriptor;
    private Component component;
    private T value;

    public ZKWrapperView(Component component) {
        if (component instanceof View) {
            init((View<T>) component);
        } else {
            this.component = component;
            bindComponent();
        }
    }

    private void bindComponent() {
        Binder binder = (Binder) component.getAttribute(ZK_WRAPPER_BINDER);
        if (binder == null) {
            binder = new DefaultBinder();
            ZKBindingUtil.initBinder(binder, component, this);
            ZKBindingUtil.bindComponent(binder, component, "bean.value", null);
            component.setAttribute(ZK_WRAPPER_BINDER, binder);
        }
        ZKBindingUtil.bindBean(component, "bean", this);
        binder.loadComponent(component, false);

    }

    private void init(View<T> view) {
        this.view = view;
        if (this.view instanceof Component) {
            this.component = (Component) view;
            this.component.setParent(this);
        }
    }

    @Override
    public ViewDescriptor getViewDescriptor() {
        return viewDescriptor;
    }

    @Override
    public void setViewDescriptor(ViewDescriptor viewDescriptor) {
        this.viewDescriptor = viewDescriptor;
    }

    @Override
    public View getParentView() {
        return view.getParentView();
    }

    @Override
    public void setParentView(View parentView) {
        view.setParentView(parentView);
    }

    @Override
    public T getValue() {
        if (view != null) {
            return view.getValue();
        } else {
            return value;
        }
    }

    @Override
    public void setValue(T value) {
        if (view != null) {
            this.view.setValue(null);
        } else {
            this.value = value;

        }
    }

    public Component getComponent() {
        return component;
    }

}
