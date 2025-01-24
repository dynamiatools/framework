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


package tools.dynamia.zk.viewers.form;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Binder;
import org.zkoss.bind.Phase;
import org.zkoss.bind.PhaseListener;
import org.zkoss.zhtml.H3;
import org.zkoss.zhtml.Text;
import org.zkoss.zhtml.impl.AbstractTag;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Center;
import org.zkoss.zul.Div;
import org.zkoss.zul.South;
import org.zkoss.zul.impl.LabelElement;
import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionEventBuilder;
import tools.dynamia.actions.ActionsContainer;
import tools.dynamia.commons.Callback;
import tools.dynamia.commons.PropertyChangeEvent;
import tools.dynamia.commons.PropertyChangeListener;
import tools.dynamia.commons.PropertyChangeListenerContainer;
import tools.dynamia.viewers.*;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.actions.ActionPanel;
import tools.dynamia.zk.actions.ButtonActionRenderer;
import tools.dynamia.zk.ui.CanBeReadonly;
import tools.dynamia.zk.util.ZKBindingUtil;
import tools.dynamia.zk.util.ZKUtil;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings("rawtypes")
public class FormView<T> extends Div implements FormViewComponent<T, Component>, PropertyChangeListener, CanBeReadonly, IdSpace, ActionsContainer {

    static {
        BindingComponentIndex.getInstance().put("value", FormView.class);
        ComponentAliasIndex.getInstance().add(FormView.class);
    }

    /**
     *
     */
    public static final String ON_VALUE_CHANGED = "onValueChanged";
    private static final long serialVersionUID = 1L;

    protected T value;
    private Binder binder;
    private boolean readOnly;
    private ViewDescriptor viewDescriptor;
    private final Map<String, FormFieldComponent> componentsFieldsMap = new HashMap<>();
    private final Map<String, FormFieldGroupComponent> groupsComponentsMap = new HashMap<>();
    private View parentView;
    private final List<View> subviews = new ArrayList<>();

    // Renderer data
    protected int _realCols;
    protected Component _rows;
    protected FormViewRenderer<T> _renderer;
    private boolean autosaveBindings = true;
    private String title;

    private Object source;
    private Consumer onSourceChange;
    private String customView;

    private Component layout;

    private Component titleArea;
    private Component contentArea;
    private Component actionsArea;
    private ActionPanel actionPanel;

    private ActionEventBuilder actionEventBuilder;

    private boolean autoheight;

    private Supplier<T> valueSupplier;


    public FormView() {
        this(false);
    }

    public FormView(boolean autoheight) {
        this.autoheight = autoheight;
        initLayout();
    }

    protected void initLayout() {
        setSclass("form-view");

        var bl = new Borderlayout();
        bl.setParent(this);
        bl.appendChild(new Center());
        bl.getCenter().setAutoscroll(true);
        bl.getCenter().setSclass("form-view-center");
        layout = bl;


        var div = new Div();
        div.setSclass("form-view-content");
        contentArea = div;
        bl.getCenter().appendChild(contentArea);

        var titleTag = new H3();
        titleTag.setSclass("form-view-title");
        titleTag.setStyle("display: none");
        titleArea = titleTag;
        contentArea.appendChild(titleTag);

        setupAutoheight();
    }

    private void setupAutoheight() {
        if (autoheight) {
            setVflex("1");
            if (layout instanceof Borderlayout bl) {
                bl.setVflex("1");
                bl.setZclass("z-borderlayout");
                bl.getCenter().setAutoscroll(true);
                bl.getCenter().setZclass("z-center");
            }
        } else {
            setVflex(null);
            if (layout instanceof Borderlayout bl) {
                bl.setVflex(null);
                bl.setZclass("auto");
                bl.getCenter().setAutoscroll(false);
                bl.getCenter().setZclass("auto");
            }
        }
    }

    @Override
    public T getValue() {
        if (autosaveBindings) {
            saveBindings();
        }
        return value;
    }

    @Override
    public boolean isAutosaveBindings() {
        return autosaveBindings;
    }

    @Override
    public void setAutosaveBindings(boolean autosaveBindings) {
        this.autosaveBindings = autosaveBindings;
    }

    @Override
    public void saveBindings() {
        ZKBindingUtil.postNotifyChange(this, "*");
    }

    @Override
    public void setValue(T value) {
        if (value instanceof Collection) {
            //ignore
            return;
        }

        if (value != this.value) {
            this.value = value;
            if (value instanceof PropertyChangeListenerContainer) {
                ((PropertyChangeListenerContainer) value).addPropertyChangeListener(this);
            }
            Events.postEvent(ON_VALUE_CHANGED, this, value);
        }
        updateUI();

    }

    @Override
    public void setViewDescriptor(ViewDescriptor viewDescriptor) {
        this.viewDescriptor = viewDescriptor;
    }

    @Override
    public ViewDescriptor getViewDescriptor() {
        return viewDescriptor;
    }

    @Override
    public FormFieldComponent getFieldComponent(String fieldName) {
        return componentsFieldsMap.get(fieldName);
    }

    @Override
    public FormFieldGroupComponent getFieldGroupComponent(String groupName) {
        return groupsComponentsMap.get(groupName);
    }

    @Override
    public void updateUI() {
        if (binder != null) {
            ZKBindingUtil.bindBean(this, Viewers.BEAN, value);
            binder.loadComponent(this, false);
        }
    }

    public void setBinder(Binder binder) {
        this.binder = binder;
    }


    public void setReadonly(boolean readOnly) {
        if (this.readOnly != readOnly) {
            this.readOnly = readOnly;
            ZKUtil.changeReadOnly(this, readOnly);
        }
    }


    public boolean isReadonly() {
        return readOnly;
    }


    @Override
    public void addSubview(String title, View subview) {
        if (subview instanceof Component subviewComp && !subviews.contains(subview)) {
            subviews.add(subview);
            FieldGroup group = new FieldGroup(title, title);
            _renderer.renderGroup(group, _realCols, _rows);
            subviewComp.setParent(getContentArea());
        }

    }

    @Override
    public List<View> getSubviews() {
        return subviews;
    }

    @Override
    public Map<String, FormFieldComponent> getComponentsFieldsMap() {
        return componentsFieldsMap;
    }

    @Override
    public Map<String, FormFieldGroupComponent> getGroupsComponentsMap() {
        return groupsComponentsMap;
    }

    public Binder getBinder() {
        return binder;
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
    public T getRawValue() {
        return value;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Field field = viewDescriptor.getField(evt.propertyName());
        if (field != null) {
            updateUI();
            saveBindings();
        }

    }

    public void onSaveBinding(Callback callback) {
        if (binder != null) {
            binder.setPhaseListener(new PhaseListener() {

                @Override
                public void prePhase(Phase arg0, BindContext arg1) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void postPhase(Phase phase, BindContext arg1) {
                    if (phase == Phase.SAVE_BINDING) {
                        callback.doSomething();
                    }
                }
            });
        }
    }

    public boolean containsField(String fieldName) {
        return componentsFieldsMap.containsKey(fieldName);
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
        titleArea.getChildren().clear();
        if (title != null && !title.isEmpty()) {
            updateTitle();
        }
    }

    private void updateTitle() {
        if (titleArea instanceof AbstractTag tag) {
            tag.appendChild(new Text(title));
            tag.setStyle("display: block");
        } else if (titleArea instanceof LabelElement label) {
            label.setLabel(title);
        }
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public void setSource(Object source) {
        this.source = source;
        if (onSourceChange != null) {
            //noinspection unchecked
            onSourceChange.accept(source);
        }
    }

    @Override
    public String getCustomView() {
        return customView;
    }

    @Override
    public void setCustomView(String customView) {
        this.customView = customView;
    }

    @Override
    public void setParent(Component parent) {
        super.setParent(parent);
        if (parent != null && customView != null && getChildren().isEmpty()) {
            ZKUtil.createComponent(customView, this, viewDescriptor.getParams());
        }
    }

    public Component getContentArea() {
        if (contentArea == null) {
            return this;
        }
        return contentArea;
    }

    public Component getActionsArea() {
        return actionsArea;
    }

    public Component getTitleArea() {
        return titleArea;
    }

    public ActionPanel getActionPanel() {
        return actionPanel;
    }

    public void onSourceChanged(Consumer onSourceChange) {
        this.onSourceChange = onSourceChange;
    }

    @Override
    public void clearActions() {
        if (actionsArea != null) {
            actionsArea.detach();
            actionPanel = null;
            actionsArea = null;
        }
    }

    /**
     * Add action to {@link FormView}. Form actions are not rendered in custom views
     *
     * @param action
     */
    @Override
    public void addAction(Action action) {
        if (customView == null || customView.isBlank()) {
            initActionsArea();
            actionPanel.addAction(action);
        }
    }

    @Override
    public List<Action> getActions() {
        if (actionPanel != null) {
            return actionPanel.getActions();
        } else {
            return Collections.emptyList();
        }
    }

    private void initActionsArea() {
        if (actionsArea == null) {
            actionPanel = new ActionPanel(getActionEventBuilder());
            var renderer = new ButtonActionRenderer();
            renderer.setAppendActionNameSclass(true);
            renderer.setStyleClass("btn");
            actionPanel.setActionRenderer(renderer);
            if (layout instanceof Borderlayout) {
                var south = new South();
                south.setSclass("form-view-actions");
                actionsArea = south;
                layout.appendChild(actionsArea);
                actionsArea.appendChild(actionPanel);
            } else {
                actionsArea = actionPanel;
                actionsArea.appendChild(this);
            }


        }
    }

    public ActionEventBuilder getActionEventBuilder() {
        return actionEventBuilder;
    }

    public void setActionEventBuilder(ActionEventBuilder actionEventBuilder) {
        this.actionEventBuilder = actionEventBuilder;
        if (actionEventBuilder != null && actionPanel != null) {
            actionPanel.setEventBuilder(actionEventBuilder);
        }
    }

    public boolean isAutoheight() {
        return autoheight;
    }

    public void setAutoheight(boolean autoheight) {
        this.autoheight = autoheight;
        setupAutoheight();
    }

    /**
     * Reload form view value using a value {@link Supplier} or reset the same value
     */
    public void reloadValue() {
        if (valueSupplier != null) {
            setValue(valueSupplier.get());
        } else {
            setValue(getValue());
        }
    }

    public Supplier<T> getValueSupplier() {
        return valueSupplier;
    }

    public void setValueSupplier(Supplier<T> valueSupplier) {
        this.valueSupplier = valueSupplier;
    }
}
