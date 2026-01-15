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

package tools.dynamia.zk.viewers.ui;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.sys.ContentRenderer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Toolbar;
import org.zkoss.zul.Window;
import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.ActionEventBuilder;
import tools.dynamia.actions.ActionLoader;
import tools.dynamia.actions.ActionRenderer;
import tools.dynamia.actions.Actions;
import tools.dynamia.commons.collect.ArrayListMultiMap;
import tools.dynamia.commons.collect.MultiMap;
import tools.dynamia.integration.Containers;
import tools.dynamia.ui.Form;
import tools.dynamia.viewers.DataSetView;
import tools.dynamia.viewers.View;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.ViewFactory;
import tools.dynamia.viewers.ViewRendererException;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.web.util.HttpUtils;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.actions.ActionToolbar;
import tools.dynamia.zk.actions.ButtonActionRenderer;
import tools.dynamia.zk.ui.CanBeReadonly;
import tools.dynamia.zk.util.ZKUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class Viewer extends Div implements ActionEventBuilder, CanBeReadonly {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private View<Object> view;
    private String viewType;
    private Class beanClass;
    private Object value;
    private String descriptorId;
    private Div contentRegion;
    private Div toolbarRegion;
    private Component actionsRegion;
    private Toolbar toolbar;
    private final List<Action> actions = new ArrayList<>();
    private final MultiMap<String, EventListener> events = new ArrayListMultiMap<>();
    private boolean autoheight;
    private String viewStyle;
    private String viewSclass;
    private Object source;


    static {
        BindingComponentIndex.getInstance().put("value", Viewer.class);
        ComponentAliasIndex.getInstance().add(Viewer.class);
    }

    private ViewDescriptor descriptor;
    private Boolean readOnly;

    public Viewer() {
        init();
    }

    public Viewer(ViewDescriptor descriptor) {
        this(descriptor, null);
    }

    public Viewer(ViewDescriptor descriptor, Object value) {
        this.descriptor = descriptor;
        this.beanClass = descriptor.getBeanClass();
        this.viewType = descriptor.getViewTypeName();
        this.value = value;
        init();
    }

    public Viewer(String descriptorId, Object value) {
        this.descriptorId = descriptorId;
        this.value = value;
        init();
    }

    public Viewer(String descriptorId) {
        this.descriptorId = descriptorId;
        init();
    }

    public Viewer(String viewType, Class objectClass) {
        this(viewType, objectClass, null);
    }

    public Viewer(String viewType, Class objectClass, Object value) {
        super();
        this.viewType = viewType;
        this.beanClass = objectClass;
        this.value = value;
        init();
    }

    @Override
    public void applyProperties() {
        super.applyProperties();
        render();
    }

    private void render() {

        ViewFactory viewFactory = Containers.get().findObject(ViewFactory.class);
        if (viewFactory == null) {
            throw new ViewRendererException("No ViewFactory found");
        }

        if (beanClass == null && value != null) {
            beanClass = value.getClass();
        }

        if (view == null && descriptorId != null) {
            String device = HttpUtils.detectDevice();
            this.descriptor = Viewers.findViewDescriptor(descriptorId, device);

            if (this.descriptor == null) {
                this.descriptor = Viewers.findViewDescriptor(descriptorId);
            }

            view = viewFactory.getView(descriptor, value);
        } else if (viewType != null && beanClass != null && view == null) {
            if (descriptor != null) {
                view = viewFactory.getView(descriptor, value);
            } else {
                view = viewFactory.getView(viewType, HttpUtils.detectDevice(), value, beanClass);
                descriptor = view.getViewDescriptor();
            }

        }

        if (view != null) {
            if (value == null && view.getValue() != null) {
                value = view.getValue();
            }

            view.setValue(value);
            view.setSource(source);
        }

        if (view instanceof Component viewComp) {
            //noinspection unchecked
            events.forEach((name, values) -> values.forEach(listener -> viewComp.addEventListener(name, listener)));
            viewComp.setParent(contentRegion);

        }

        if (view instanceof HtmlBasedComponent) {
            if (viewStyle != null) {
                ((HtmlBasedComponent) view).setStyle(viewStyle);
            }

            if (viewSclass != null) {
                ((HtmlBasedComponent) view).setSclass(viewSclass);
            }

        }
        renderActions();
        updateReadOnly();
    }

    private void renderActions() {

        List<Action> allActions = ActionLoader.loadActionCommands(value);
        allActions.addAll(actions);

        if (allActions.isEmpty()) {
            return;
        }

        if (actionsRegion == null) {
            renderActionRegions();
        }

        actionsRegion.getChildren().clear();

        for (Action action : allActions) {
            ActionRenderer actionRenderer = new ButtonActionRenderer();
            if (action.getRenderer() != null) {
                actionRenderer = action.getRenderer();
            }

            Component actionComponent = (Component) Actions.render(actionRenderer, action, this);

            if (actionComponent instanceof Button button) {
                String type = "primary";
                if (action.getAttribute("type") != null) {
                    type = (String) action.getAttribute("type");
                }
                button.setZclass("btn btn-" + type);
            }

            actionsRegion.appendChild(actionComponent);
        }

    }

    public String getViewType() {
        return viewType;
    }

    public void setViewType(String viewType) {
        this.viewType = viewType;
        render();
    }

    public void setType(String viewType) {
        setViewType(viewType);
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
        render();
    }

    public String getDescriptorId() {
        return descriptorId;
    }

    public void setDescriptorId(String descriptorId) {
        this.descriptorId = descriptorId;
        render();
    }

    public void setBeanClass(String beanClass) {
        try {
            setBeanClass(Class.forName(beanClass));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Object getValue() {
        if (view != null) {
            return view.getValue();
        } else {
            return value;
        }
    }

    public void setValue(Object value) {
        if ("null".equals(value)) {
            value = null;
        }

        this.value = value;
        if (view != null) {
            this.view.setValue(value);
        } else {
            render();
        }

    }

    public Object getSelected() {
        if (view instanceof DataSetView) {
            return ((DataSetView) view).getSelected();
        } else {
            return null;
        }
    }

    public void setSelected(Object selected) {
        if (view instanceof DataSetView) {
            ((DataSetView) view).setSelected(selected);
        }
    }

    @Override
    public boolean addEventListener(String evtnm, EventListener<? extends Event> listener) {
        if (view != null && view instanceof Component) {
            return ((Component) view).addEventListener(evtnm, listener);
        } else {
            events.put(evtnm, listener);
            return false;
        }
    }

    public View getView() {
        if (view == null) {
            render();
        }
        return view;
    }


    @Override
    protected void renderProperties(ContentRenderer renderer) throws IOException {
        super.renderProperties(renderer);
    }

    @Override
    public void setParent(Component parent) {
        super.setParent(parent);
        render();
    }


    public void setContentVflex(String vflex) {
        contentRegion.setVflex(vflex);
    }

    public String getContentVflex() {
        return contentRegion.getVflex();
    }

    public void setContentSclass(String sclass) {
        contentRegion.setSclass(sclass);
    }

    public String getContentSclass() {
        return contentRegion.getSclass();
    }

    public void setContentStyle(String style) {
        contentRegion.setStyle(style);
    }

    public String getContentStyle() {
        return contentRegion.getStyle();
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public void setToolbar(Toolbar toolbar) {
        this.toolbar = toolbar;

        if (toolbar instanceof ActionToolbar actionToolbar) {
            if (actionToolbar.getEventBuilder() == null) {
                actionToolbar.setEventBuilder(this);
            }
        }

        toolbarRegion.getChildren().clear();
        toolbarRegion.appendChild(toolbar);
    }

    private void init() {
        setStyle("overflow-y:auto;overflow-x:hidden");
        setSclass("viewer");


        toolbarRegion = new Div();
        toolbarRegion.setSclass("viewer-toolbar-reg");
        toolbarRegion.setParent(this);

        contentRegion = new Div();
        contentRegion.setSclass("viewer-content-reg");
        contentRegion.setVflex(null);
        contentRegion.setParent(this);


    }

    private void renderActionRegions() {
        Div actions = new Div();
        actions.setSclass("viewer-actions-reg");
        actions.setParent(this);

        Hlayout actionLayout = new Hlayout();
        actionLayout.setParent(actions);
        actionsRegion = actionLayout;
    }

    public void addAction(Action action) {
        actions.add(action);
    }

    public Component getActionsRegion() {
        if (actionsRegion == null) {
            renderActionRegions();
        }
        return actionsRegion;
    }

    @Override
    public ActionEvent buildActionEvent(Object source, Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put("value", getValue());
        params.put("viewer", this);
        params.put("view", getView());
        params.put("viewDescriptor", descriptor);
        params.put("viewType", getViewType());

        return new ActionEvent(getValue(), this, params);
    }

    public boolean isAutoheight() {
        return autoheight;
    }

    public void setAutoheight(boolean autoheight) {
        if (this.autoheight != autoheight) {
            this.autoheight = autoheight;
            if (autoheight) {
                setContentVflex(null);
                setStyle(null);
            }
        }
    }

    public static Window showDialog(String title, String viewType, Object value) {
        Window win = showDialog(title, viewType, value.getClass(), value);

        Viewer viewer = (Viewer) win.query("viewer");
        if (HttpUtils.isSmartphone()) {
            viewer.setContentVflex("1");
            viewer.setVflex("1");
        } else {
            viewer.setContentVflex(null);
            viewer.setStyle(null);
        }
        return win;
    }

    public static Window showDialog(String title, String viewType, Class valueClass, Object value) {
        Viewer viewer = new Viewer(viewType, valueClass, value);
        String height = null;
        if (viewer.getView() instanceof Listbox) {
            height = "80%";
        }
        return ZKUtil.showDialog(title, viewer, "80%", height);
    }


    @Override
    public String toString() {
        if (descriptorId != null) {
            return super.toString() + " - " + descriptorId;
        }
        return super.toString();
    }

    public String getViewStyle() {
        return viewStyle;
    }

    public void setViewStyle(String viewStyle) {
        this.viewStyle = viewStyle;
    }

    public String getViewSclass() {
        return viewSclass;
    }

    public void setViewSclass(String viewSclass) {
        this.viewSclass = viewSclass;
    }

    public static Window showForm(String title, Form form) {
        Window win = showDialog(title, "form", form);
        form.onClose(win::detach);
        return win;
    }


    @Override
    public void setReadonly(boolean readOnly) {
        this.readOnly = true;
        updateReadOnly();
    }

    private void updateReadOnly() {
        if (readOnly != null && view != null && view instanceof CanBeReadonly) {
            ((CanBeReadonly) view).setReadonly(this.readOnly);
        }
    }

    @Override
    public boolean isReadonly() {
        if (readOnly == null) {
            return false;
        }
        return readOnly;
    }

    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
        if (view != null) {
            view.setSource(source);
        }
    }

    @Override
    public void setHeight(String height) {
        super.setHeight(height);
        if (height != null) {
            contentRegion.setVflex("1");
        }
    }


    @Override
    public void setVflex(String flex) {
        super.setVflex(flex);
        if ("1".equalsIgnoreCase(flex) || "true".equalsIgnoreCase(flex)) {
            contentRegion.setVflex("1");
        }
    }

    public ViewDescriptor getViewDescriptor() {
        return descriptor;
    }

    /**
     * Show this viewer as a modal window
     */
    public Window showModal(String title, boolean autoscroll, EventListener<Event> onClose) {
        var win = ZKUtil.showDialog(title, this);
        if (autoscroll) {
            win.setContentStyle("overflow: auto");
        }
        if (onClose != null) {
            win.addEventListener(Events.ON_CLOSE, onClose);
        }
        return win;
    }

    /**
     * Show this viewer as a modal Window
     */
    public Window showModal(String title) {
        return showModal(title, true, null);
    }
}
