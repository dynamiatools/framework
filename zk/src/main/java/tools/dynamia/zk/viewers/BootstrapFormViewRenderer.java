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
package tools.dynamia.zk.viewers;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zul.*;
import org.zkoss.zul.impl.InputElement;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.zk.ui.DateRangebox;
import tools.dynamia.zk.ui.DateSelector;
import tools.dynamia.zk.ui.DurationSelector;
import tools.dynamia.zk.viewers.form.FormView;
import tools.dynamia.zk.viewers.form.FormViewRenderer;

public class BootstrapFormViewRenderer<T> extends FormViewRenderer<T> {

    @Override
    protected Div newRow() {
        Div div = super.newRow();
        div.setZclass("row");
        return div;
    }

    @Override
    protected Div newColumn(Component row, int realCols, int colspan, int tabletColSpan) {
        var column = super.newColumn(row, realCols, colspan, tabletColSpan);
        colspan = getRealColspan(colspan, realCols);
        column.setZclass("form-group col-12 col-sm-" + tabletColSpan + " col-md-" + colspan);
        return column;
    }

    @Override
    protected Label newLabel(Field field, String labelText, String decriptionText) {
        var label = super.newLabel(field, labelText, decriptionText);
        label.setZclass("form-view-lbl");
        return label;
    }

    @Override
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

            if (element instanceof Datebox || element instanceof Combobox || element instanceof Bandbox
                    || element instanceof Spinner || element instanceof Timebox) {

                element.setSclass("form-zcontrol");
            } else {
                element.setZclass("form-control");
            }
        }

        if (component instanceof Checkbox checkbox) {
            checkbox.setLabel(" " + labelText);
        }

        if (component instanceof Button button) {
            button.setZclass("none");
            button.setStyle("margin-left: 5px");
            button.setSclass("form-control btn btn-success" + " " + styleClass);
        }

        if (component instanceof Label || component instanceof Image) {
            HtmlBasedComponent html = (HtmlBasedComponent) component;
            html.setSclass("form-zcontrol");
        }

        if (component instanceof DateRangebox dateRangebox) {
            dateRangebox.setStyle("display: block");
            dateRangebox.setZclass("form-zcontrol");
        }

        if (component instanceof DateSelector dateSelector) {
            dateSelector.getDaycombo().setSclass("form-zcontrol");
            dateSelector.getMonthcombo().setSclass("form-zcontrol");
            dateSelector.getYearbox().setZclass("form-control");
        }

        if (component instanceof DurationSelector durationSelector) {
            durationSelector.getUnitsbox().setSclass("form-zcontrol");
            durationSelector.getValuebox().setSclass("form-control");
        }


        if (component instanceof HtmlBasedComponent) {
            var fieldSclass = field.getName().replace(".", "-") + "-field";
            ((HtmlBasedComponent) component).addSclass(fieldSclass);
        }
    }

    @Override
    protected void applyFieldParams(Component comp, Field field) {
        comp.setAttribute(Viewers.ATTRIBUTE_FIELD_NAME, field.getName());
        comp.setAttribute(Viewers.ATTRIBUTE_FIELD_CLASS, field.getFieldClass());
    }


}
