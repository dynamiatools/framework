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
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
/**
 * ZK container that resolves and hosts a Dynamia {@link View} instance.
 * <p>
 * The view is created lazily from a descriptor id, descriptor instance, or view type and bean class.
 * This component also exposes helper APIs for actions, read-only propagation, and dialog rendering.
 */
public class Viewer extends Div implements ActionEventBuilder, CanBeReadonly {

    /**
     * Serialization identifier.
     */
    @Serial
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

    /**
     * Creates an empty viewer. The hosted view is resolved when rendering is triggered.
     */
    public Viewer() {
        init();
    }

    /**
     * Creates a viewer from a descriptor.
     *
     * @param descriptor descriptor used to create the internal view
     */
    public Viewer(ViewDescriptor descriptor) {
        this(descriptor, null);
    }

    /**
     * Creates a viewer from a descriptor and initial value.
     *
     * @param descriptor descriptor used to create the internal view
     * @param value initial value for the view
     */
    public Viewer(ViewDescriptor descriptor, Object value) {
        this.descriptor = descriptor;
        this.beanClass = descriptor.getBeanClass();
        this.viewType = descriptor.getViewTypeName();
        this.value = value;
        init();
    }

    /**
     * Creates a viewer from a descriptor id and initial value.
     *
     * @param descriptorId id used to locate a {@link ViewDescriptor}
     * @param value initial value for the view
     */
    public Viewer(String descriptorId, Object value) {
        this.descriptorId = descriptorId;
        this.value = value;
        init();
    }

    /**
     * Creates a viewer from a descriptor id.
     *
     * @param descriptorId id used to locate a {@link ViewDescriptor}
     */
    public Viewer(String descriptorId) {
        this.descriptorId = descriptorId;
        init();
    }

    /**
     * Creates a viewer from a view type and bean class.
     *
     * @param viewType view type name
     * @param objectClass bean type used by the view
     */
    public Viewer(String viewType, Class objectClass) {
        this(viewType, objectClass, null);
    }

    /**
     * Creates a viewer from a view type, bean class, and initial value.
     *
     * @param viewType view type name
     * @param objectClass bean type used by the view
     * @param value initial value for the view
     */
    public Viewer(String viewType, Class objectClass, Object value) {
        super();
        this.viewType = viewType;
        this.beanClass = objectClass;
        this.value = value;
        init();
    }

    /**
     * Applies ZK properties and triggers lazy view rendering.
     */
    @Override
    public void applyProperties() {
        super.applyProperties();
        render();
    }

    /**
     * Resolves and renders the internal view into the content region.
     */
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

    /**
     * Renders action components for command actions associated with the current value and explicit actions.
     */
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

    /**
     * Returns the configured view type name.
     *
     * @return view type name
     */
    public String getViewType() {
        return viewType;
    }

    /**
     * Sets the view type and re-renders the hosted view.
     *
     * @param viewType view type name
     */
    public void setViewType(String viewType) {
        this.viewType = viewType;
        render();
    }

    /**
     * Alias for {@link #setViewType(String)}.
     *
     * @param viewType view type name
     */
    public void setType(String viewType) {
        setViewType(viewType);
    }

    /**
     * Returns the bean class used to resolve the view.
     *
     * @return bean class
     */
    public Class getBeanClass() {
        return beanClass;
    }

    /**
     * Sets the bean class and re-renders the hosted view.
     *
     * @param beanClass bean class
     */
    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
        render();
    }

    /**
     * Returns the descriptor id used to resolve the view.
     *
     * @return descriptor id
     */
    public String getDescriptorId() {
        return descriptorId;
    }

    /**
     * Sets the descriptor id and re-renders the hosted view.
     *
     * @param descriptorId descriptor id
     */
    public void setDescriptorId(String descriptorId) {
        this.descriptorId = descriptorId;
        render();
    }

    /**
     * Resolves and sets the bean class by fully qualified class name.
     *
     * @param beanClass fully qualified class name
     */
    public void setBeanClass(String beanClass) {
        try {
            setBeanClass(Class.forName(beanClass));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the current value from the rendered view when available.
     *
     * @return current value
     */
    public Object getValue() {
        if (view != null) {
            return view.getValue();
        } else {
            return value;
        }
    }

    /**
     * Sets the current value. String literal {@code "null"} is converted to {@code null}.
     *
     * @param value new value
     */
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

    /**
     * Returns the selected item when the hosted view is a {@link DataSetView}.
     *
     * @return selected item, or {@code null} if selection is not supported
     */
    public Object getSelected() {
        if (view instanceof DataSetView) {
            return ((DataSetView) view).getSelected();
        } else {
            return null;
        }
    }

    /**
     * Updates the selected item when the hosted view is a {@link DataSetView}.
     *
     * @param selected selected item
     */
    public void setSelected(Object selected) {
        if (view instanceof DataSetView) {
            ((DataSetView) view).setSelected(selected);
        }
    }

    /**
     * Adds an event listener directly to the rendered view component when available.
     * <p>
     * If the view is not rendered yet, the listener is queued and attached during rendering.
     *
     * @param evtnm event name
     * @param listener listener instance
     * @return {@code true} when attached immediately, otherwise {@code false}
     */
    @Override
    public boolean addEventListener(String evtnm, EventListener<? extends Event> listener) {
        if (view != null && view instanceof Component) {
            return ((Component) view).addEventListener(evtnm, listener);
        } else {
            events.put(evtnm, listener);
            return false;
        }
    }

    /**
     * Returns the hosted view, rendering it first if needed.
     *
     * @return hosted view instance
     */
    public View getView() {
        if (view == null) {
            render();
        }
        return view;
    }


    /**
     * Renders component properties to the client.
     *
     * @param renderer ZK content renderer
     * @throws IOException if rendering fails
     */
    @Override
    protected void renderProperties(ContentRenderer renderer) throws IOException {
        super.renderProperties(renderer);
    }

    /**
     * Sets the parent component and ensures the hosted view is rendered.
     *
     * @param parent new parent component
     */
    @Override
    public void setParent(Component parent) {
        super.setParent(parent);
        render();
    }


    /**
     * Sets the content region vertical flex.
     *
     * @param vflex vertical flex value
     */
    public void setContentVflex(String vflex) {
        contentRegion.setVflex(vflex);
    }

    /**
     * Returns the content region vertical flex.
     *
     * @return content region vertical flex
     */
    public String getContentVflex() {
        return contentRegion.getVflex();
    }

    /**
     * Sets the CSS class of the content region.
     *
     * @param sclass CSS class
     */
    public void setContentSclass(String sclass) {
        contentRegion.setSclass(sclass);
    }

    /**
     * Returns the CSS class of the content region.
     *
     * @return content region CSS class
     */
    public String getContentSclass() {
        return contentRegion.getSclass();
    }

    /**
     * Sets inline style of the content region.
     *
     * @param style inline style
     */
    public void setContentStyle(String style) {
        contentRegion.setStyle(style);
    }

    /**
     * Returns inline style of the content region.
     *
     * @return content region style
     */
    public String getContentStyle() {
        return contentRegion.getStyle();
    }

    /**
     * Returns the current toolbar instance.
     *
     * @return toolbar component
     */
    public Toolbar getToolbar() {
        return toolbar;
    }

    /**
     * Sets the toolbar component in the toolbar region.
     * <p>
     * When the toolbar is an {@link ActionToolbar}, this viewer is injected as event builder if missing.
     *
     * @param toolbar toolbar component
     */
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

    /**
     * Initializes base layout regions used by this component.
     */
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

    /**
     * Creates the action area container.
     */
    private void renderActionRegions() {
        Div actions = new Div();
        actions.setSclass("viewer-actions-reg");
        actions.setParent(this);

        Hlayout actionLayout = new Hlayout();
        actionLayout.setParent(actions);
        actionsRegion = actionLayout;
    }

    /**
     * Adds an explicit action to the viewer.
     *
     * @param action action to add
     */
    public void addAction(Action action) {
        actions.add(action);
    }

    /**
     * Returns the action region container, creating it if needed.
     *
     * @return action region component
     */
    public Component getActionsRegion() {
        if (actionsRegion == null) {
            renderActionRegions();
        }
        return actionsRegion;
    }

    /**
     * Builds an action event enriched with viewer context parameters.
     *
     * @param source ignored source parameter; the event source is this viewer
     * @param params custom event parameters, can be {@code null}
     * @return populated action event
     */
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

    /**
     * Indicates whether auto-height mode is enabled.
     *
     * @return {@code true} if auto-height is enabled
     */
    public boolean isAutoheight() {
        return autoheight;
    }

    /**
     * Enables or disables auto-height mode.
     * <p>
     * When enabled, fixed component style and content vflex are cleared.
     *
     * @param autoheight auto-height flag
     */
    public void setAutoheight(boolean autoheight) {
        if (this.autoheight != autoheight) {
            this.autoheight = autoheight;
            if (autoheight) {
                setContentVflex(null);
                setStyle(null);
            }
        }
    }

    /**
     * Creates and shows a dialog with a viewer for the given value.
     * <p>
     * Layout is adjusted for smartphone and non-smartphone devices.
     *
     * @param title dialog title
     * @param viewType view type name
     * @param value value shown by the viewer
     * @return created dialog window
     */
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

    /**
     * Creates and shows a dialog with a viewer for the given type and value class.
     *
     * @param title dialog title
     * @param viewType view type name
     * @param valueClass bean class associated with the view
     * @param value value shown by the viewer
     * @return created dialog window
     */
    public static Window showDialog(String title, String viewType, Class valueClass, Object value) {
        Viewer viewer = new Viewer(viewType, valueClass, value);
        String height = null;
        if (viewer.getView() instanceof Listbox) {
            height = "80%";
        }
        return ZKUtil.showDialog(title, viewer, "80%", height);
    }


    /**
     * Returns a string representation that includes descriptor id when available.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        if (descriptorId != null) {
            return super.toString() + " - " + descriptorId;
        }
        return super.toString();
    }

    /**
     * Returns inline style applied to the hosted view component.
     *
     * @return hosted view style
     */
    public String getViewStyle() {
        return viewStyle;
    }

    /**
     * Sets inline style to apply to the hosted view component when it is HTML-based.
     *
     * @param viewStyle hosted view style
     */
    public void setViewStyle(String viewStyle) {
        this.viewStyle = viewStyle;
    }

    /**
     * Returns CSS class applied to the hosted view component.
     *
     * @return hosted view CSS class
     */
    public String getViewSclass() {
        return viewSclass;
    }

    /**
     * Sets CSS class to apply to the hosted view component when it is HTML-based.
     *
     * @param viewSclass hosted view CSS class
     */
    public void setViewSclass(String viewSclass) {
        this.viewSclass = viewSclass;
    }

    /**
     * Shows a form inside a viewer dialog and closes the dialog when the form closes.
     *
     * @param title dialog title
     * @param form form to display
     * @return created dialog window
     */
    public static Window showForm(String title, Form form) {
        Window win = showDialog(title, "form", form);
        form.onClose(win::detach);
        return win;
    }


    /**
     * Sets read-only mode and propagates the state to the hosted view when supported.
     *
     * @param readOnly read-only flag
     */
    @Override
    public void setReadonly(boolean readOnly) {
        this.readOnly = true;
        updateReadOnly();
    }

    /**
     * Applies read-only mode to the hosted view when it implements {@link CanBeReadonly}.
     */
    private void updateReadOnly() {
        if (readOnly != null && view != null && view instanceof CanBeReadonly) {
            ((CanBeReadonly) view).setReadonly(this.readOnly);
        }
    }

    /**
     * Indicates whether this viewer is marked as read-only.
     *
     * @return {@code true} if read-only mode is enabled
     */
    @Override
    public boolean isReadonly() {
        if (readOnly == null) {
            return false;
        }
        return readOnly;
    }

    /**
     * Returns the custom source object forwarded to the hosted view.
     *
     * @return source object
     */
    public Object getSource() {
        return source;
    }

    /**
     * Sets a custom source object and forwards it to the hosted view when available.
     *
     * @param source source object
     */
    public void setSource(Object source) {
        this.source = source;
        if (view != null) {
            view.setSource(source);
        }
    }

    /**
     * Sets component height and enables content region vflex when a height is provided.
     *
     * @param height component height
     */
    @Override
    public void setHeight(String height) {
        super.setHeight(height);
        if (height != null) {
            contentRegion.setVflex("1");
        }
    }


    /**
     * Sets vertical flex and mirrors full-flex values to the content region.
     *
     * @param flex vertical flex value
     */
    @Override
    public void setVflex(String flex) {
        super.setVflex(flex);
        if ("1".equalsIgnoreCase(flex) || "true".equalsIgnoreCase(flex)) {
            contentRegion.setVflex("1");
        }
    }

    /**
     * Returns the resolved descriptor used by the hosted view.
     *
     * @return resolved view descriptor
     */
    public ViewDescriptor getViewDescriptor() {
        return descriptor;
    }

    /**
     * Shows this viewer in a modal dialog.
     *
     * @param title dialog title
     * @param autoscroll whether to enable auto scrolling in dialog content
     * @param onClose optional close listener
     * @return created dialog window
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
     * Shows this viewer in a modal dialog with auto-scroll enabled.
     *
     * @param title dialog title
     * @return created dialog window
     */
    public Window showModal(String title) {
        return showModal(title, true, null);
    }
}
