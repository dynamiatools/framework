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
package tools.dynamia.zk.app.bstemplate;

import org.zkoss.bind.Binder;
import org.zkoss.zhtml.Form;
import org.zkoss.zhtml.H3;
import org.zkoss.zhtml.I;
import org.zkoss.zhtml.Text;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zul.*;
import org.zkoss.zul.impl.InputElement;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.Messages;
import tools.dynamia.ui.icons.IconSize;
import tools.dynamia.ui.icons.IconsTheme;
import tools.dynamia.viewers.*;
import tools.dynamia.viewers.util.ViewRendererUtil;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.zk.ui.DateRangebox;
import tools.dynamia.zk.ui.DateSelector;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.viewers.ZKViewersUtil;
import tools.dynamia.zk.viewers.form.FormFieldComponent;
import tools.dynamia.zk.viewers.form.FormFieldGroupComponent;
import tools.dynamia.zk.viewers.form.FormView;
import tools.dynamia.zk.viewers.form.FormViewRenderer;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static tools.dynamia.viewers.util.ViewersExpressionUtil.$s;
import static tools.dynamia.viewers.util.ViewersExpressionUtil.isExpression;

public class BootstrapFormViewRenderer<T> extends FormViewRenderer<T> {

    @Override
    protected Component renderRows(FormView<T> view, ViewDescriptor viewDesc, int realCols, T value) {

        Div row = null;

        viewDesc.getFields().sort(new IndexableComparator());

        Div panel = new Div();
        panel.setZclass("panel");

        Div panelBody = new Div();
        panelBody.setZclass("panel-body");
        panelBody.setParent(panel);

        for (Field field : viewDesc.getFields()) {
            if (field.isVisible() && field.getGroup() == null && ViewRendererUtil.isFieldRenderable(viewDesc, field)) {

                if (!hasSpace(row, realCols, field)) {
                    row = newRow();
                    row.setParent(panelBody);
                    if (panel.getParent() == null) {
                        panel.setParent(view);
                    }
                }
                renderField(row, field, view.getBinder(), view, value, realCols);
            }
        }

        Collections.sort(viewDesc.getFieldGroups(), new IndexableComparator());
        for (FieldGroup fieldGroup : viewDesc.getFieldGroups()) {
            List<Field> groupFields = getGroupFields(viewDesc, fieldGroup);

            if (!groupFields.isEmpty()) {

                panel = new Div();
                panel.setZclass("panel panel-default  field-group");
                panel.setParent(view);

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

    private Div newRow() {
        Div div = new Div();
        div.setZclass("row");
        return div;
    }

    protected void renderField(Component row, Field field, Binder binder, FormView<T> view, T value, int realCols) {
        boolean showLabel = true;
        Viewers.customizeField("form", field);
        Object sl = field.getParams().get(Viewers.PARAM_SHOW_LABEL);
        if (sl != null && (sl == Boolean.FALSE || sl.toString().equalsIgnoreCase("false"))) {
            showLabel = false;
        }

        Div column = new Div();
        column.setParent(row);

        String labelText = field.getLocalizedLabel(Messages.getDefaultLocale());
        if (isExpression(labelText)) {
            labelText = $s(labelText);
        }

        String decriptionText = field.getLocalizedDescription(Messages.getDefaultLocale());
        if (isExpression(decriptionText)) {
            decriptionText = $s(decriptionText);
        }

        Label label = new Label(labelText);
        label.setZclass("form-view-lbl " + (field.isRequired() ? "required" : ""));
        label.setTooltiptext(decriptionText);
        ZKViewersUtil.setupFieldIcon(field, label);
        int colspan = 1;
        int tabletColSpan = 6;
        try {
            colspan = Integer.parseInt(field.getParams().get("span").toString());
            column.setAttribute(Viewers.ATTRIBUTE_COLSPAN, colspan);

        } catch (Exception e) {
        }

        if (colspan == realCols) {
            tabletColSpan = 12;
        }

        try {
            if (field.getParams().containsKey(Viewers.PARAM_SPAN + "-sm")) {
                tabletColSpan = Integer.parseInt(field.getParams().get(Viewers.PARAM_SPAN + "-sm").toString());
            }
        } catch (Exception e) {
        }


        colspan = getRealColspan(colspan, realCols);
        column.setZclass("form-group col-xs-12 col-sm-" + tabletColSpan + " col-md-" + colspan);

        Component component = createComponent(field, view, value);

        if (component instanceof Checkbox) {
            showLabel = false;
        }

        if (showLabel) {
            label.setParent(column);
        }

        if (component instanceof HtmlBasedComponent) {
            HtmlBasedComponent hcom = (HtmlBasedComponent) component;
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

    protected int getRealColspan(int colspan, int realCols) {
        return (12 / realCols) * colspan;

    }

    @Override
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
                if (comp instanceof Cell) {
                    Cell cell = (Cell) comp;
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
        } catch (Exception e) {
        }

        int empty = realCols - space;

        return (empty >= colspan);
    }

    @Override
    protected void applyFieldParams(Component comp, Field field) {
        comp.setAttribute(Viewers.ATTRIBUTE_FIELD_NAME, field.getName());
        comp.setAttribute(Viewers.ATTRIBUTE_FIELD_CLASS, field.getFieldClass());
    }

    protected void applyComponentCSS(Component component, String labelText, Field field) {
        String styleClass = (String) field.getParams().get(Viewers.PARAM_STYLE_CLASS);
        if (styleClass == null) {
            styleClass = "";
        }

        if (component instanceof InputElement) {
            InputElement element = (InputElement) component;
            if (!field.getParams().containsKey("placeholder")) {
                element.setPlaceholder(labelText + " " + (field.isRequired() ? "*" : ""));
            }
            if (element.getWidth() == null) {
                element.setHflex(null);
            }

            if (element instanceof Datebox || element instanceof Combobox || element instanceof Bandbox
                    || element instanceof Spinner || element instanceof Timebox) {

                element.setSclass("form-zcontrol");
            } else {
                element.setZclass("form-control");
            }
        }

        if (component instanceof Checkbox) {
            Checkbox checkbox = (Checkbox) component;
            checkbox.setLabel(" " + labelText);
        }

        if (component instanceof Button) {
            Button button = (Button) component;
            button.setZclass("none");
            button.setStyle("margin-left: 5px");
            button.setSclass("form-control btn btn-success" + " " + styleClass);
        }

        if (component instanceof Label || component instanceof Image) {
            HtmlBasedComponent html = (HtmlBasedComponent) component;
            html.setSclass("form-zcontrol");
        }

        if (component instanceof DateRangebox) {
            DateRangebox dateRangebox = (DateRangebox) component;
            dateRangebox.setStyle("display: block");
            dateRangebox.setZclass("form-zcontrol");
        }

        if (component instanceof DateSelector) {
            DateSelector dateSelector = (DateSelector) component;
            dateSelector.getDaycombo().setSclass("form-zcontrol");
            dateSelector.getMonthcombo().setSclass("form-zcontrol");
            dateSelector.getYearbox().setZclass("form-control");
        }

        if (component instanceof HtmlBasedComponent) {
            var fieldSclass = field.getName().replace(".", "-") + "-field";
            ((HtmlBasedComponent) component).addSclass(fieldSclass);
        }
    }

    @Override
    protected FormFieldGroupComponent renderGroup(FieldGroup fieldGroup, int realCols, Component box) {


        Div header = new Div();
        header.setZclass("panel-heading");
        box.appendChild(header);


        H3 title = new H3();
        title.setSclass("panel-title");
        header.appendChild(title);

        String label = fieldGroup.getLocalizedLabel(Messages.getDefaultLocale());

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

    @Override
    protected int renderHeaders(FormView<T> view, ViewDescriptor d) {
        int colNum = 2;

        ViewLayout layout = d.getLayout();
        if (layout != null) {
            try {
                colNum = Integer.parseInt(layout.getParams().get(Viewers.LAYOUT_PARAM_COLUMNS).toString());
            } catch (Exception e) {

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

    @Override
    protected FormView<T> newFormView() {
        FormView<T> view = new FormView<>();
        view.setZclass("content");
        return view;
    }

}
