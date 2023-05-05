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

import org.zkoss.bind.Binder;
import org.zkoss.zhtml.Form;
import org.zkoss.zhtml.H3;
import org.zkoss.zhtml.I;
import org.zkoss.zhtml.Text;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zul.*;
import org.zkoss.zul.impl.InputElement;
import org.zkoss.zul.impl.NumberInputElement;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.LocalizedMessagesProvider;
import tools.dynamia.commons.Messages;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.ui.icons.IconsTheme;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.FieldGroup;
import tools.dynamia.viewers.IndexableComparator;
import tools.dynamia.viewers.View;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.ViewLayout;
import tools.dynamia.viewers.ViewRenderer;
import tools.dynamia.viewers.ViewRendererCustomizer;
import tools.dynamia.viewers.util.ComponentCustomizerUtil;
import tools.dynamia.viewers.util.ViewRendererUtil;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ui.Import;
import tools.dynamia.zk.util.ZKBindingUtil;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.viewers.ZKViewersUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static tools.dynamia.viewers.util.ViewersExpressionUtil.$s;
import static tools.dynamia.viewers.util.ViewersExpressionUtil.isExpression;

/**
 * @author Mario A. Serrano Leones
 */
public class FormViewRenderer<T> implements ViewRenderer<T> {

    private LocalizedMessagesProvider messagesProvider;

    @Override
    public View<T> render(ViewDescriptor descriptor, T value) {


        FormView<T> view = null;
        if (descriptor.getParams().containsKey(Viewers.PARAM_CUSTOM_VIEW)) {
            view = new FormView<>();
            view.setAutosaveBindings(false);
            String customView = (String) descriptor.getParams().get(Viewers.PARAM_CUSTOM_VIEW);
            view.setCustomView(customView);
            view.getChildren().clear();
            view.setVflex("1");
            Viewers.setupView(view, descriptor.getParams());
            return view;
        } else {
            view = newFormView();
            ViewRendererUtil.beforeRender(descriptor, view);
            View<T> result = render(view, descriptor, value);
            ViewRendererUtil.afterRender(descriptor, result);
            return result;
        }
    }

    public View<T> render(FormView<T> view, ViewDescriptor descriptor, T value) {

        view.setSclass(Viewers.ATTRIBUTE_FORM_VIEW);
        Viewers.setupView(view, descriptor.getParams());

        Binder binder = ZKBindingUtil.createBinder();
        ZKBindingUtil.initBinder(binder, view, view);
        view.setBinder(binder);

        int realCols = renderHeaders(view, descriptor);
        view._renderer = this;
        view._realCols = realCols;
        view._rows = renderRows(view, descriptor, realCols, value);
        view.setValue(value);

        return view;
    }

    // protected METHODS
    protected int renderHeaders(FormView<T> view, ViewDescriptor d) {
        Grid grid = (Grid) view.getFirstChild().getNextSibling();

        Columns columns = new Columns();
        columns.setParent(grid);

        int colNum = 2;

        try {
            ViewLayout layout = d.getLayout();
            if (layout != null) {
                colNum = Integer.parseInt(layout.getParams().get(Viewers.LAYOUT_PARAM_COLUMNS).toString());
            }
        } catch (Exception ignored) {
        }

        int realColNum = colNum;// * 2;

        for (int i = 0; i < realColNum; i++) {
            Column column = new Column();
            column.setParent(columns);

        }

        return realColNum;
    }

    protected Component renderRows(FormView<T> view, ViewDescriptor viewDesc, int realCols, T value) {
        Grid grid = (Grid) view.getFirstChild().getNextSibling();
        Binder binder = ZKBindingUtil.createBinder();
        ZKBindingUtil.initBinder(binder, view, view);

        Rows rows = new Rows();
        rows.setParent(grid);

        Row row = null;
        viewDesc.getFields().sort(new IndexableComparator());


        for (Field field : viewDesc.getFields()) {
            if (field.isVisible() && field.getGroup() == null && ViewRendererUtil.isFieldRenderable(viewDesc, field)) {

                if (!hasSpace(row, realCols, field)) {
                    row = new Row();
                    row.setParent(rows);
                }
                renderField(row, field, binder, view, value);
            }
        }

        viewDesc.getFieldGroups().sort(new IndexableComparator());
        for (FieldGroup fieldGroup : viewDesc.getFieldGroups()) {
            List<Field> groupFields = getGroupFields(viewDesc, fieldGroup);
            if (!groupFields.isEmpty()) {
                FormFieldGroupComponent groupComponent = renderGroup(fieldGroup, realCols, rows);
                if (groupComponent != null) {
                    view.getGroupsComponentsMap().put(fieldGroup.getName(), groupComponent);
                }
                row = new Row();
                row.setParent(rows);
                for (Field field : groupFields) {
                    if (!hasSpace(row, realCols, field)) {
                        row = new Row();
                        row.setParent(rows);
                    }
                    renderField(row, field, binder, view, value);
                }
            }
        }

        view.setBinder(binder);
        return rows;
    }

    protected FormFieldGroupComponent renderGroup(FieldGroup fieldGroup, int realCols, Component rows) {

        Row group = new Row();
        group.setParent(rows);

        Cell cell = new Cell();
        cell.setColspan(realCols);
        cell.setParent(group);


        H3 title = new H3();
        title.setStyle("color: #3bafda");
        cell.appendChild(title);

        String label = fieldGroup.getLocalizedLabel(Messages.getDefaultLocale());
        label = filterFieldGroupLabel(fieldGroup, label);

        if (fieldGroup.getIcon() != null) {
            I icon = new I();
            icon.setParent(title);
            label = " " + label;
            ZKUtil.configureComponentIcon(IconsTheme.get().getIcon(fieldGroup.getIcon()), icon, IconSize.NORMAL);
        }
        title.appendChild(new Text(label));
        return new FormFieldGroupComponent(fieldGroup.getName(), group);
    }

    protected void renderField(Component row, Field field, Binder binder, FormView<T> view, T value) {
        Viewers.customizeField("form", field);
        boolean showLabel = true;

        String labelText = field.getLocalizedLabel(Messages.getDefaultLocale());
        if (isExpression(labelText)) {
            labelText = $s(labelText);
        } else {
            labelText = filterFieldLabel(field, labelText);
        }

        String decriptionText = field.getLocalizedDescription(Messages.getDefaultLocale());
        if (isExpression(decriptionText)) {
            decriptionText = $s(decriptionText);
        } else {
            decriptionText = filterFieldDescription(field, decriptionText);
        }

        Object sl = field.getParam(Viewers.PARAM_SHOW_LABEL);
        if (sl != null && (sl == Boolean.FALSE || sl.toString().equalsIgnoreCase("false"))) {
            showLabel = false;
        }


        Label label = null;
        if (showLabel) {
            label = new Label(labelText);
            label.setTooltiptext($s(decriptionText));
            label.setSclass("form-view-lbl " + (field.isRequired() ? "required" : ""));
            if (field.getParam("labelStyle") != null) {
                label.setStyle((String) field.getParam("labelStyle"));
            }
            ZKViewersUtil.setupFieldIcon(field, label);

        }

        int colspan = 1;
        try {
            colspan = Integer.parseInt(field.getParam(Viewers.PARAM_SPAN).toString());
        } catch (Exception ignored) {
        }

        int compRealSpan = getRealColspan(colspan);


        Cell compCell = new Cell();
        compCell.setColspan(compRealSpan);
        compCell.setParent(row);
        compCell.setStyle("padding:6px");
        if (colspan > 1) {
            int realColspan = getRealColspan(colspan);

            compCell.setColspan(realColspan);
        }

        Component component = createComponent(field, view, value);
        if (component instanceof HtmlBasedComponent hcom) {
            hcom.setTooltiptext(decriptionText);

        }
        if (component instanceof InputElement && !field.getParams().containsKey("placeholder")) {
            ((InputElement) component).setPlaceholder(labelText);
        }


        //Add Label
        if (label != null) {
            label.setParent(compCell);

            if (!(component instanceof Checkbox) && !(component instanceof Radio)) {
                label.setStyle("display: block");
            }
        }

        //Add Component
        if (component.getClass().getName().contains("CKeditor")) {
            Form form = new Form();
            component.setParent(form);
            form.setParent(compCell);
            Object config = field.getParam("config");
            if (config instanceof java.util.Map) {
                BeanUtils.invokeSetMethod(component, "config", config);
            }
        } else {

            component.setParent(compCell);
        }
        createBinding(component, field, binder, value);
        view.getComponentsFieldsMap().put(field.getName(), new FormFieldComponent(field.getName(), label, component));
        ViewRendererUtil.afterFieldRender(view.getViewDescriptor(), field, component);
    }

    protected Component createComponent(Field field, FormView<T> view, T value) {
        Component component = null;

        Map<String, Object> params = field.getParams();

        if (field.getComponentClass() == null) {
            component = new Label();
        } else {
            component = (Component) BeanUtils.newInstance(field.getComponentClass());
        }
        ComponentCustomizerUtil.customizeComponent(field, component, field.getComponentCustomizer());
        component.setAttribute(Viewers.ATTRIBUTE_FORM_VIEW, view);
        applyFieldParams(component, field);
        applyFieldConstraints(component, field);
        if (component instanceof Import importComp) {
            importComp.setValue(value);
            importComp.addArgs(field.getParams());
        }

        BeanUtils.setupBean(component, params);

        return component;
    }

    protected void applyFieldConstraints(Component comp, Field field) {

        if (comp instanceof InputElement inputElement && field.getParam(Viewers.PARAM_CONSTRAINT) instanceof Constraint) {
            inputElement.setConstraint((Constraint) field.getParam(Viewers.PARAM_CONSTRAINT));

        }

    }

    protected void createBinding(Component comp, Field field, Binder binder, T value) {
        if (field.getViewDescriptor().getParams().get(Viewers.PARAM_IGNORE_BINDINGS) == Boolean.TRUE) {
            return;
        }

        if (field.getParam(Viewers.PARAM_IGNORE_BINDINGS) == Boolean.TRUE) {
            return;
        }

        Object bmapObject = field.getParam(Viewers.PARAM_BINDINGS);
        if (bmapObject != null && bmapObject instanceof Map bindingMap) {
            ZKBindingUtil.bindComponent(binder, comp, bindingMap,Viewers.BEAN);
        } else {
            String attr = BindingComponentIndex.getInstance().getAttribute(comp.getClass());
            if (field.getParam(Viewers.PARAM_BINDING_ATTRIBUTE) instanceof String) {
                attr = field.getParam(Viewers.PARAM_BINDING_ATTRIBUTE).toString();
            }

            String converterExpression = null;

            converterExpression = (String) field.getParam(Viewers.PARAM_CONVERTER);

            if (attr != null && !attr.isEmpty()) {
                String bindName = (String) field.getParam(Viewers.PARAM_BIND);
                if (bindName == null) {
                    bindName = field.getName();
                }
                String expression = Viewers.BEAN + "." + bindName;
                ZKBindingUtil.bindComponent(binder, comp, attr, expression, converterExpression);
            }
        }

    }

    protected int getRealColspan(int colspan) {
        return colspan;
    }

    protected boolean hasSpace(Component row, int realCols, Field field) {
        if (row == null) {
            return false;
        }

        if (field.getParams().containsKey(Viewers.PARAM_NEW_ROW)) {
            return false;
        }

        if (BeanUtils.isAssignable(field.getFieldClass(), Collection.class)) {
            return false;
        }

        int space = 0;
        if (row.getChildren() != null) {
            for (Object object : row.getChildren()) {
                if (object instanceof Cell cell) {
                    space += cell.getColspan();
                } else {
                    space++;
                }
            }
        }

        int colspan = 1;
        try {
            colspan = Integer.parseInt(field.getParam(Viewers.PARAM_SPAN).toString());
        } catch (Exception ignored) {
        }

        int empty = realCols - space;

        return (empty >= (colspan));
    }

    protected void applyFieldParams(Component comp, Field field) {
        comp.setAttribute(Viewers.ATTRIBUTE_FIELD_NAME, field.getName());
        comp.setAttribute(Viewers.ATTRIBUTE_FIELD_CLASS, field.getFieldClass());

        if ((comp instanceof Textbox || comp instanceof NumberInputElement) && !field.getParams().containsKey(Viewers.PARAM_WIDTH)
                && !field.getParams().containsKey(Viewers.PARAM_HFLEX)) {
            HtmlBasedComponent tb = (HtmlBasedComponent) comp;
            tb.setWidth("99%");
        }

        if (field.getParams().containsKey(Viewers.PARAMS_ATTRIBUTES)) {
            Map attributes = (Map) field.getParam(Viewers.PARAMS_ATTRIBUTES);
            if (attributes != null) {
                //noinspection unchecked
                attributes.forEach((k, v) -> comp.setAttribute(k.toString(), v));
            }

        }

    }

    protected String filterFieldLabel(Field field, String label) {
        if (messagesProvider == null) {
            return label;
        } else {
            return messagesProvider.getMessage(field.getName(), Viewers.buildMessageClasffier(field.getViewDescriptor()), Messages.getDefaultLocale(), label);
        }
    }

    protected String filterFieldGroupLabel(FieldGroup fieldGroup, String label) {
        if (messagesProvider == null) {
            return label;
        } else {
            return messagesProvider.getMessage("Group " + fieldGroup.getName(), Viewers.buildMessageClasffier(fieldGroup.getViewDescriptor()), Messages.getDefaultLocale(), label);
        }
    }

    protected String filterFieldDescription(Field field, String description) {
        if (messagesProvider == null) {
            return description;
        } else {
            return messagesProvider.getMessage(field.getName() + " Description", Viewers.buildMessageClasffier(field.getViewDescriptor()), Messages.getDefaultLocale(), description);
        }
    }

    protected FormView<T> newFormView() {
        FormView<T> formView = new FormView<>();
        Grid grid = new Grid();
        grid.setOddRowSclass("none");
        grid.setParent(formView);
        grid.setStyle("border: 0");
        return formView;
    }

    /**
     * Return only visible and renderable fields
     *
     */
    protected List<Field> getGroupFields(ViewDescriptor descriptor, FieldGroup group) {
        ViewRendererCustomizer customizer = ViewRendererUtil.findViewRendererCustomizer(descriptor);
        return group.getFields().stream().filter(f -> f.isVisible() && (customizer == null || customizer.isRenderable(f))).collect(Collectors.toList());
    }


    public LocalizedMessagesProvider getMessagesProvider() {
        return messagesProvider;
    }

    public void setMessagesProvider(LocalizedMessagesProvider messagesProvider) {
        this.messagesProvider = messagesProvider;
    }
}
