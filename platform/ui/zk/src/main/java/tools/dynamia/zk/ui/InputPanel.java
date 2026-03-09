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
package tools.dynamia.zk.ui;

import org.zkoss.bind.Binder;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.logger.Loggable;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.util.ComponentCustomizerUtil;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.web.util.HttpUtils;
import tools.dynamia.zk.util.ZKBindingUtil;
import tools.dynamia.zk.util.ZKUtil;

import java.io.Serial;

@SuppressWarnings("rawtypes")
public class InputPanel extends Div implements Loggable {

    public static final String ON_INPUT = "onInput";
    @Serial
    private static final long serialVersionUID = 7388726856898185544L;
    public static final String BINDING_ATTRIBUTE = "bindingAttribute";

    private HtmlBasedComponent textbox;
    private Label label;
    private Button okButton;
    private Object value;
    private final Class inputClass;
    private Field inputField;

    public InputPanel() {
        this(null, null, String.class);
    }

    public InputPanel(Class inputClass) {
        this(null, null, inputClass);
    }

    public InputPanel(Object value, Class inputClass) {
        this(null, value, inputClass);
    }

    public InputPanel(String label, Object value, Class inputClass) {
        this.inputClass = inputClass;
        this.value = value;
        renderView(label);

        String bindingAttribute = inputField != null && inputField.getParam(BINDING_ATTRIBUTE) != null ? inputField.getParam(BINDING_ATTRIBUTE).toString() : null;
        Binder binder = ZKBindingUtil.createBinder();
        ZKBindingUtil.initBinder(binder, this, this);
        ZKBindingUtil.bindComponent(binder, textbox, bindingAttribute, "inputPanel.value", null);
        ZKBindingUtil.bindBean(this, "inputPanel", this);
        binder.loadComponent(this, false);
        addListeners();
    }

    private void addListeners() {
        textbox.addEventListener(Events.ON_OK, event -> {
            saveValue();
            Events.postEvent(ON_INPUT, InputPanel.this, getValue());
        });

        okButton.addEventListener(Events.ON_CLICK, event -> {
            saveValue();
            Events.postEvent(ON_INPUT, InputPanel.this, getValue());
        });
    }

    private void renderView(String label) {
        Vbox box = new Vbox();
        box.setParent(this);
        box.setStyle("margin-bottom: 4px");
        box.setVflex("1");
        box.setHflex("1");

        if (label != null) {
            this.label = new Label();
            this.label.setValue(label);
            this.label.setParent(box);
            this.label.setStyle("font-weight:bold");
        }


        textbox = (HtmlBasedComponent) buildTextbox();
        textbox.setHflex("1");
        textbox.setParent(box);
        if (textbox instanceof Textbox ||
                textbox instanceof Decimalbox ||
                textbox instanceof Doublebox) {
            textbox.setSclass("form-control");
        }


        okButton = new Button("OK");
        okButton.setAutodisable("self");
        okButton.setParent(box);
        okButton.setIconSclass("z-icon-check");
        okButton.setSclass("btn btn-success");
        okButton.setStyle("float: right");
    }

    @SuppressWarnings({"unchecked"})
    private Component buildTextbox() {
        Class componClass = null;

        inputField = new Field("field", inputClass);
        Viewers.customizeField("form", inputField);

        if (inputField.getComponentClass() != null) {
            componClass = inputField.getComponentClass();
        } else {
            componClass = Textbox.class;
        }

        Component comp = (Component) ObjectOperations.newInstance(componClass);
        if (inputField.getComponentCustomizer() != null) {
            try {
                ComponentCustomizerUtil.customizeComponent(inputField, comp, inputField.getComponentCustomizer());
            } catch (Exception e) {
                log("Cannot create component customizer", e);
            }
        }
        ObjectOperations.setupBean(comp, inputField.getParams());
        return comp;
    }

    public void setLabel(String label) {
        this.label.setValue(label);
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void saveValue() {

    }

    public Object showDialog() {
        final Window win = ZKUtil.createWindow(null);
        if (HttpUtils.isSmartphone()) {
            win.setWidth("100%");
        } else {
            win.setWidth("450px");
        }
        win.setClosable(true);
        win.setTitle(label.getValue());
        label.setValue("");

        this.setParent(win);
        addEventListener(ON_INPUT, event -> win.detach());

        try {
            win.doModal();
        } catch (Exception e) {
            log("Error showing dialog", e);
        }
        return getValue();
    }

    public HtmlBasedComponent getTextbox() {
        return textbox;
    }

    public Label getLabel() {
        return label;
    }

    public Button getOkButton() {
        return okButton;
    }
}
