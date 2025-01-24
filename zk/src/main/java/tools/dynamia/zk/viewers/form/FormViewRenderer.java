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
import tools.dynamia.viewers.*;
import tools.dynamia.viewers.util.ComponentCustomizerUtil;
import tools.dynamia.viewers.util.ViewRendererUtil;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.constraints.ZKExtraConstraints;
import tools.dynamia.zk.converters.Util;
import tools.dynamia.zk.ui.DateRangebox;
import tools.dynamia.zk.ui.DateSelector;
import tools.dynamia.zk.ui.DurationSelector;
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
        int colNum = 2;

        ViewLayout layout = d.getLayout();
        if (layout != null) {
            try {
                colNum = Integer.parseInt(layout.getParams().get(Viewers.LAYOUT_PARAM_COLUMNS).toString());
            } catch (Exception ignored) {

            }
        }

        if (colNum > 12) {
            colNum = 12;
        }
        if (colNum < 1) {
            colNum = 1;
        }

        return colNum;
    }


    protected Component renderRows(FormView<T> view, ViewDescriptor viewDesc, int realCols, T value) {
        Div row = null;
        Div panel = new Div();
        panel.setZclass("panel");

        Div panelBody = new Div();
        panelBody.setZclass("panel-body");
        panelBody.setParent(panel);

        for (Field field : viewDesc.sortFields()) {
            if (field.isVisible() && field.getGroup() == null && ViewRendererUtil.isFieldRenderable(viewDesc, field)) {

                if (!hasSpace(row, realCols, field)) {
                    row = newRow();
                    row.setParent(panelBody);
                    if (panel.getParent() == null) {
                        panel.setParent(view.getContentArea());
                    }
                }
                renderField(row, field, view.getBinder(), view, value, realCols);
            }
        }

        viewDesc.getFieldGroups().sort(new IndexableComparator());
        for (FieldGroup fieldGroup : viewDesc.getFieldGroups()) {
            List<Field> groupFields = getGroupFields(viewDesc, fieldGroup);

            if (!groupFields.isEmpty()) {

                panel = new Div();
                panel.setZclass("panel panel-default  field-group");
                panel.setParent(view.getContentArea());

                FormFieldGroupComponent groupComponent = renderGroup(fieldGroup, realCols, panel);
                view.getGroupsComponentsMap().put(fieldGroup.getName(), groupComponent);
                panelBody = new Div();
                panelBody.setZclass("panel-body");
                panelBody.setParent(panel);

                row = newRow();
                row.setParent(panelBody);


                for (Field field : groupFields) {
                    if (!hasSpace(row, realCols, field)) {
                        row = newRow();
                        row.setParent(panelBody);
                    }
                    renderField(row, field, view.getBinder(), view, value, realCols);
                    FormFieldComponent formFieldComponent = view.getFieldComponent(field.getName());
                    if (formFieldComponent != null && groupComponent != null) {
                        groupComponent.getFieldsComponents().add(formFieldComponent);
                    }
                }
            }
        }

        return view;
    }

    protected Div newRow() {
        Div div = new Div();
        div.setZclass("dt-row dt-items-center");
        return div;
    }

    protected Div newColumn(Component row, int realCols, int colspan, int tabletColSpan) {
        Div column = new Div();
        column.setParent(row);
        column.setAttribute(Viewers.ATTRIBUTE_COLSPAN, colspan);
        colspan = getRealColspan(colspan, realCols);
        column.setZclass("form-group dt-px-2 dt-col-12 dt-col-sm-" + tabletColSpan + " dt-col-md-" + colspan);
        return column;
    }


    protected Label newLabel(Field field, String labelText, String decriptionText) {
        Label label = new Label(labelText);
        if (field.isRequired()) {
            label.addSclass("required");
        }

        label.setTooltiptext(decriptionText);
        ZKViewersUtil.setupFieldIcon(field, label);
        return label;
    }

    protected FormFieldGroupComponent renderGroup(FieldGroup fieldGroup, int realCols, Component box) {


        Div header = new Div();
        header.setZclass("panel-heading");
        if (box instanceof FormView<?> formView) {
            formView.getContentArea().appendChild(header);
        } else {
            box.appendChild(header);
        }


        H3 title = new H3();
        title.setSclass("panel-title");
        header.appendChild(title);

        String label = fieldGroup.getLocalizedLabel(Messages.getDefaultLocale());
        label = filterFieldGroupLabel(fieldGroup, label);

        if (fieldGroup.getIcon() != null) {
            I icon = new I();
            icon.setParent(title);
            label = " " + label;
            ZKUtil.configureComponentIcon(IconsTheme.get().getIcon(fieldGroup.getIcon()), icon, IconSize.NORMAL);
        }
        title.appendChild(new Text(label));

        if (fieldGroup.getParams() != null) {
            BeanUtils.setupBean(box, fieldGroup.getParams());
        }

        return new FormFieldGroupComponent(fieldGroup.getName(), box);

    }

    protected void renderField(Component row, Field field, Binder binder, FormView<T> view, T value, int realCols) {
        boolean showLabel = true;
        Viewers.customizeField("form", field);
        Object sl = field.getParams().get(Viewers.PARAM_SHOW_LABEL);
        if (sl != null && (sl == Boolean.FALSE || sl.toString().equalsIgnoreCase("false"))) {
            showLabel = false;
        }


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

        Label label = newLabel(field, labelText, decriptionText);
        int colspan = 1;
        int tabletColSpan = 6;
        try {
            colspan = Integer.parseInt(field.getParams().get("span").toString());
        } catch (Exception ignored) {
        }

        if (colspan == realCols) {
            tabletColSpan = 12;
        }

        try {
            if (field.getParams().containsKey(Viewers.PARAM_SPAN + "-sm")) {
                tabletColSpan = Integer.parseInt(field.getParams().get(Viewers.PARAM_SPAN + "-sm").toString());
            }
        } catch (Exception ignored) {
        }

        Div column = newColumn(row, realCols, colspan, tabletColSpan);

        Component component = createComponent(field, view, value);

        if (component instanceof Checkbox) {
            showLabel = false;
        }

        if (showLabel) {
            label.setParent(column);
        }

        if (component instanceof HtmlBasedComponent hcom) {
            hcom.setTooltiptext($s(field.getDescription()));
        }
        if (component.getClass().getName().contains("CKeditor")) {
            Form form = new Form();
            component.setParent(form);
            form.setParent(column);
            Object config = field.getParams().get("config");
            if (config instanceof java.util.Map) {
                BeanUtils.invokeSetMethod(component, "config", config);
            }
        } else {
            applyComponentCSS(component, labelText, field);
            component.setParent(column);
        }
        createBinding(component, field, binder, value);
        view.getComponentsFieldsMap().put(field.getName(), new FormFieldComponent(field.getName(), label, component));
    }


    protected void applyComponentCSS(Component component, String labelText, Field field) {
        String styleClass = (String) field.getParams().get(Viewers.PARAM_STYLE_CLASS);
        if (styleClass == null) {
            styleClass = "";
        }

        if (component instanceof InputElement element) {
            if (!field.getParams().containsKey("placeholder")) {
                element.setPlaceholder(labelText + " " + (field.isRequired() ? "*" : ""));
            }
            if (element.getWidth() == null) {
                element.setHflex(null);
            }
            element.addSclass("form-zcontrol");

        }

        if (component instanceof Checkbox checkbox) {
            checkbox.setLabel(" " + labelText);
        }

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
        if (component instanceof Import importComp) {
            importComp.setValue(value);
            importComp.addArgs(field.getParams());
        }
        BeanUtils.setupBean(component, params);
        applyFieldConstraints(component, field);

        return component;
    }

    protected void applyFieldConstraints(Component comp, Field field) {

        try {
            if (comp instanceof InputElement inputElement) {
                Constraint fieldConstraint = null;
                var constValue = field.getParam(Viewers.PARAM_CONSTRAINT);

                if (constValue instanceof String value) {
                    if (fieldConstraint == null) {
                        fieldConstraint = ZKExtraConstraints.getInstance(value);
                    }

                    if (fieldConstraint == null) {
                        fieldConstraint = tryToCreateConstraint(value, fieldConstraint);
                    }
                    if (fieldConstraint == null) {
                        inputElement.setConstraint(value);
                    }
                } else if (constValue instanceof Constraint constraint) {
                    fieldConstraint = constraint;
                }
                if (fieldConstraint != null) {
                    inputElement.setConstraint(fieldConstraint);
                }
            }
        } catch (Exception e) {
            //ignore constraints
        }
    }

    private static Constraint tryToCreateConstraint(String name, Constraint fieldConstraint) {
        try {
            Object object = BeanUtils.newInstance(Class.forName(name));
            if (object instanceof Constraint constraint) {
                fieldConstraint = constraint;
            }
        } catch (Exception e) {
        }
        return fieldConstraint;
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
            ZKBindingUtil.bindComponent(binder, comp, bindingMap, Viewers.BEAN);
        } else {
            String attr = BindingComponentIndex.getInstance().getAttribute(comp.getClass());
            if (field.getParam(Viewers.PARAM_BINDING_ATTRIBUTE) instanceof String) {
                attr = field.getParam(Viewers.PARAM_BINDING_ATTRIBUTE).toString();
            }

            String converterExpression = null;

            converterExpression = (String) field.getParam(Viewers.PARAM_CONVERTER);
            converterExpression = Util.checkConverterClass(converterExpression);

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


    protected int getRealColspan(int colspan, int realCols) {
        return (12 / realCols) * colspan;

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
            for (Component comp : row.getChildren()) {
                if (comp instanceof Cell cell) {
                    space += cell.getColspan();
                } else if (comp.getAttribute(Viewers.ATTRIBUTE_COLSPAN) != null) {
                    space += (int) comp.getAttribute(Viewers.ATTRIBUTE_COLSPAN);
                } else {
                    space++;
                }
            }
        }

        int colspan = 1;
        try {
            colspan = Integer.parseInt(field.getParams().get(Viewers.PARAM_SPAN).toString());
        } catch (Exception ignored) {
        }

        int empty = realCols - space;

        return (empty >= colspan);
    }


    protected void applyFieldParams(Component comp, Field field) {
        comp.setAttribute(Viewers.ATTRIBUTE_FIELD_NAME, field.getName());
        comp.setAttribute(Viewers.ATTRIBUTE_FIELD_CLASS, field.getFieldClass());

        if ((comp instanceof Textbox || comp instanceof NumberInputElement) && !field.getParams().containsKey(Viewers.PARAM_WIDTH)
                && !field.getParams().containsKey(Viewers.PARAM_HFLEX)) {
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
        return formView;
    }

    /**
     * Return only visible and renderable fields
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
