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
package tools.dynamia.zk.ui;

import org.zkoss.bind.Binder;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.integration.Containers;
import tools.dynamia.viewers.ComponentCustomizer;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.FieldCustomizer;
import tools.dynamia.web.util.HttpUtils;
import tools.dynamia.zk.util.ZKBindingUtil;
import tools.dynamia.zk.util.ZKUtil;

import java.util.Collection;

@SuppressWarnings("rawtypes")
public class InputPanel extends Div {

    public static final String ON_INPUT = "onInput";
    private static final long serialVersionUID = 7388726856898185544L;

    private HtmlBasedComponent textbox;
    private Label label;
    private Button okButton;
    private Object value;
    private final Class inputClass;

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

        Binder binder = ZKBindingUtil.createBinder();
        ZKBindingUtil.initBinder(binder, this, this);
        ZKBindingUtil.bindComponent(binder, textbox, "inputPanel.value", null);
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
        okButton.setParent(box);
        okButton.setIconSclass("z-icon-check");
        okButton.setSclass("btn btn-success");
        okButton.setStyle("float: right");
    }

    @SuppressWarnings({"unchecked"})
    private Component buildTextbox() {
        Class componClass = null;

        Field field = new Field("field", inputClass);
        Collection<FieldCustomizer> customizers = Containers.get().findObjects(FieldCustomizer.class);
        if (customizers != null) {
            for (FieldCustomizer fieldCustomizer : customizers) {
                fieldCustomizer.customize("form", field);
            }
        }

        if (field.getComponentClass() != null) {
            componClass = field.getComponentClass();
        } else {
            componClass = Textbox.class;
        }

        Component comp = (Component) BeanUtils.newInstance(componClass);
        if (field.getComponentCustomizer() != null) {
            try {
                ComponentCustomizer customizer = BeanUtils.newInstance(field.getComponentCustomizer());
                customizer.cutomize(field, comp);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        BeanUtils.setupBean(comp, field.getParams());
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
            e.printStackTrace();
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
