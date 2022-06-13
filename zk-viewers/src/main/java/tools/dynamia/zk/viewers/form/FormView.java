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


package tools.dynamia.zk.viewers.form;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Binder;
import org.zkoss.bind.Phase;
import org.zkoss.bind.PhaseListener;
import org.zkoss.zhtml.H3;
import org.zkoss.zhtml.Text;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Div;
import tools.dynamia.commons.Callback;
import tools.dynamia.commons.PropertyChangeListenerContainer;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.FieldGroup;
import tools.dynamia.viewers.View;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.ui.CanBeReadonly;
import tools.dynamia.zk.util.ZKBindingUtil;
import tools.dynamia.zk.util.ZKUtil;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings("rawtypes")
public class FormView<T> extends Div implements View<T>, PropertyChangeListener, CanBeReadonly, IdSpace {

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
    private final H3 titleTag;
    private Object source;
    private Consumer onSourceChange;
    private String customView;



    public FormView() {
        titleTag = new H3();
        titleTag.setSclass("form-view-title");
        titleTag.setStyle("display: none");
        appendChild(titleTag);
    }

    @Override
    public T getValue() {
        if (autosaveBindings) {
            saveBindings();
        }
        return value;
    }

    public boolean isAutosaveBindings() {
        return autosaveBindings;
    }

    public void setAutosaveBindings(boolean autosaveBindings) {
        this.autosaveBindings = autosaveBindings;
    }

    protected void saveBindings() {
        ZKBindingUtil.postNotifyChange(this, "*");
    }

    @Override
    public void setValue(T value) {
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

    public FormFieldComponent getFieldComponent(String fieldName) {
        return componentsFieldsMap.get(fieldName);
    }

    public FormFieldGroupComponent getFieldGroupComponent(String groupName) {
        return groupsComponentsMap.get(groupName);
    }

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


    public void addSubview(String title, View subview) {
        if (subview instanceof Component && !subviews.contains(subview)) {
            subviews.add(subview);
            FieldGroup group = new FieldGroup(title, title);
            _renderer.renderGroup(group, _realCols, _rows);
            ((Component) subview).setParent(this);
        }

    }

    public List<View> getSubviews() {
        return subviews;
    }

    public Map<String, FormFieldComponent> getComponentsFieldsMap() {
        return componentsFieldsMap;
    }

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

    public T getRawValue() {
        return value;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Field field = viewDescriptor.getField(evt.getPropertyName());
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        titleTag.getChildren().clear();
        if (title != null && !title.isEmpty()) {
            titleTag.appendChild(new Text(title));
            titleTag.setStyle("display: normal");
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
            onSourceChange.accept(source);
        }
    }

    public String getCustomView() {
        return customView;
    }

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

    public void onSourceChanged(Consumer onSourceChange) {
        this.onSourceChange = onSourceChange;
    }
}
