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

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;
import org.zkoss.zul.impl.InputElement;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.util.ZKUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Mario Serrano Leones
 */
public class Keypad extends Div {

    static {
        ComponentAliasIndex.getInstance().add(Keypad.class);
        BindingComponentIndex.getInstance().put("value", Keypad.class);
    }

    public static final Key NEW_ROW = new Key();


    private Label displayLabel;
    private InputElement displayBox;
    private boolean autoclearOnOK = true;
    private String labelFormat;
    private String value = "0";
    private Div displayRegion;
    private boolean preferStringValue;

    private List<Key> defaultKeys = new ArrayList<>(Arrays.asList(
            new Key("1"), new Key("2"), new Key("3"), NEW_ROW,
            new Key("4"), new Key("5"), new Key("6"), NEW_ROW,
            new Key("7"), new Key("8"), new Key("9"), NEW_ROW,
            new Key("0"), new Key("00"), new Key("000"), NEW_ROW,
            new Key("").setLabel("C").setCommand(e -> clear()),
            new Key("").setLabel("backspace").setIcon("fa fa-arrow-left").setCommand(e -> backspace()),
            new Key("").setLabel("OK").setCommand(e -> ok())));
    private boolean disabled;

    public Keypad() {
        init();
    }

    public Keypad(Label displayLabel, InputElement displayBox) {
        this.displayLabel = displayLabel;
        this.displayBox = displayBox;
        init();
    }

    public Keypad(Label displayLabel) {
        this.displayLabel = displayLabel;
        init();
    }

    public Keypad(InputElement displayBox) {
        this.displayBox = displayBox;
        init();
    }

    public void init() {
        getChildren().clear();

        setSclass("keypad");
        setStyle("overflow: hidden");

        displayRegion = new Div();
        displayRegion.setSclass("kp-display");
        appendChild(displayRegion);

        Grid container = new Grid();
        container.setParent(this);
        container.appendChild(new Rows());
        container.setVflex("1");


        Component row = newRow();
        container.getRows().appendChild(row);
        for (Key key : defaultKeys) {
            if (key == NEW_ROW) {
                row = newRow();
                container.getRows().appendChild(row);
            } else {
                KeyButton btn = new KeyButton(key);
                Cell keycell = new Cell();
                keycell.setHeight("1em");
                keycell.appendChild(btn);
                row.appendChild(keycell);

                keycell.setColspan(key.colSpan);
                keycell.setRowspan(key.rowSpan);

            }
        }
        syncDisplay();

    }

    private Component newRow() {

        return new Row();
    }

    public void clear() {
        value = "0";
        syncDisplay();
    }

    public void backspace() {
        if (!value.isEmpty()) {
            value = value.substring(0, value.length() - 1);
            if (value.isEmpty()) {
                clear();
            } else {
                syncDisplay();
            }
        }

    }

    public void ok() {
        if ((value == null || value.isBlank()) && displayBox != null && displayBox.getRawValue() != null) {
            value = String.valueOf(displayBox.getRawValue());
        }

        syncDisplay();

        final Object finalValue = preferStringValue ? getValue() : getBigDecimalValue();
        if (autoclearOnOK) {
            clear();
        }
        ok(finalValue);

    }

    public void ok(Object value) {
        Events.postEvent(Events.ON_OK, this, value);
    }

    public void appendValue(String delta) {
        if (delta != null) {
            if (value == null) {
                value = "";
            }
            value += delta;
            syncDisplay();
        }
    }

    public void appendValue(Number number) {
        if (number != null) {
            appendValue(String.valueOf(number));
        }
    }

    public String getValue() {
        return value;
    }

    public long getLongValue() {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException numberFormatException) {
            return 0;
        }
    }

    public BigDecimal getBigDecimalValue() {
        try {
            return new BigDecimal(value);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public void setValue(String value) {
        this.value = value;
        syncDisplay();
    }

    public void setDisplayLabel(String id) {
        Component component = getFellow(id);
        if (component instanceof Label) {
            displayLabel = (Label) component;
        }
    }

    public void setDisplayBox(String id) {
        Component component = getFellow(id);
        if (component instanceof InputElement) {
            displayBox = (InputElement) component;
        }
    }

    public void setDisplayBox(InputElement displaybox) {
        this.displayBox = displaybox;
    }

    private void syncDisplay() {
        if (displayLabel != null) {
            String lblValue = getValue();
            if (labelFormat != null) {
                lblValue = StringUtils.format(getBigDecimalValue(), labelFormat);

            }
            displayLabel.setValue(lblValue);
        }

        if (displayBox != null) {
            if (displayBox instanceof Decimalbox) {
                ((Decimalbox) displayBox).setValue(getBigDecimalValue());
            } else {
                displayBox.setRawValue(value);
            }
        }
    }


    public boolean isAutoclearOnOK() {
        return autoclearOnOK;
    }

    public void setAutoclearOnOK(boolean autoclearOnOK) {
        this.autoclearOnOK = autoclearOnOK;
    }

    public String getLabelFormat() {
        return labelFormat;
    }

    public void setLabelFormat(String labelFormat) {
        this.labelFormat = labelFormat;
    }

    public List<Key> getDefaultKeys() {
        return defaultKeys;
    }

    public void setDefaultKeys(List<Key> defaultKeys) {
        this.defaultKeys = defaultKeys;
    }

    public boolean isPreferStringValue() {
        return preferStringValue;
    }

    public void setPreferStringValue(boolean preferStringValue) {
        this.preferStringValue = preferStringValue;
    }

    class KeyButton extends Button {

        private final Key key;

        public KeyButton(Key key) {
            this.key = key;
            setZclass("kp-key kp-" + key.getLabel() + "key" + " " + key.getSclass());
            if (key.getIcon() != null) {
                setIconSclass(key.getIcon());
            } else {
                setLabel(key.getLabel());
            }

            if (key.getCommand() != null) {

                addEventListener(Events.ON_CLICK, key.getCommand());
            } else {
                addEventListener(Events.ON_CLICK, e -> appendValue(key.getValue()));
            }

            setWidth("100%");
            setHeight("100%");


        }

        public Key getKey() {
            return key;
        }

    }

    public static class Key {

        private String label;
        private String value;
        private String icon;
        private int colSpan = 1;
        private int rowSpan = 1;
        private String sclass = "";
        private EventListener<Event> command;

        public Key() {
        }

        public Key(String value) {
            this.value = value;
            this.label = value;
        }

        public Key(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public Key(String value, int colSpan, int rowSpan) {
            this(value);
            this.colSpan = colSpan;
            this.rowSpan = rowSpan;
        }

        public String getLabel() {
            return label;
        }

        public Key setLabel(String label) {
            this.label = label;
            return this;
        }

        public String getValue() {
            return value;
        }

        public Key setValue(String value) {
            this.value = value;
            return this;
        }

        public String getIcon() {
            return icon;
        }

        public Key setIcon(String icon) {
            this.icon = icon;
            return this;
        }

        public String getSclass() {
            return sclass;
        }

        public Key setSclass(String sclass) {
            this.sclass = sclass;
            return this;
        }

        public EventListener<Event> getCommand() {
            return command;
        }

        public Key setCommand(EventListener<Event> command) {
            this.command = command;
            return this;
        }


    }


    public static Keypad show(String title, EventListener<Event> onOk) {
        Vlayout vlayout = new Vlayout();
        Label label = new Label();
        label.setZclass("kp-label");
        Keypad pad = new Keypad(label);
        pad.setLabelFormat("###,###");
        vlayout.appendChild(label);
        vlayout.appendChild(pad);
        pad.setWidth("100%");
        pad.setHeight("400px");


        Window win = ZKUtil.showDialog(title, vlayout, "300px", null);
        pad.addEventListener(Events.ON_OK, evt -> {
            win.detach();
            onOk.onEvent(evt);
        });
        return pad;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        updateStatus();
    }

    protected void updateStatus() {
        queryAll("button").forEach(e -> {
            Button btn = (Button) e;
            btn.setDisabled(isDisabled());
        });

    }

    public boolean isDisabled() {
        return disabled;
    }

    public Div getDisplayRegion() {
        return displayRegion;
    }

    public InputElement getDisplayBox() {
        return displayBox;
    }

    public Label getDisplayLabel() {
        return displayLabel;
    }

    public Keypad.Key findKey(String labelOrValue) {
        return defaultKeys.stream().
                filter(k -> labelOrValue.equals(k.getValue()) || labelOrValue.equals(k.getLabel()))
                .findFirst()
                .orElse(null);
    }

    public void replaceKey(String labelOrValue, Keypad.Key newKey) {
        Key originalKey = findKey(labelOrValue);
        if (originalKey != null) {
            defaultKeys.add(defaultKeys.indexOf(originalKey), newKey);
            defaultKeys.remove(originalKey);
            init();
        }
    }

}
