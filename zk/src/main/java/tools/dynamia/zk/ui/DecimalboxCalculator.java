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

import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;
import tools.dynamia.zk.util.ZKUtil;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

public class DecimalboxCalculator extends Span implements CanBeReadonly {


    private Decimalbox decimalbox;
    private Calculator calculator;
    private A calcButton;
    private Popup calcPopup;
    private BigDecimal value;

    public DecimalboxCalculator() {
        setSclass("decimalbox-calc");
        decimalbox = new Decimalbox();

        calcButton = new A();
        calcButton.setZclass("decimalbox-calc-button");
        calcButton.setIconSclass("z-icon-calculator");

        calculator = new Calculator();
        calculator.setWidth("200px");
        calculator.setHeight("300px");

        calcPopup = new Popup();
        calcPopup.appendChild(calculator);
        calcPopup.setPage(ZKUtil.getFirstPage());
        calcPopup.setStyle("margin: 0; padding: 0px");
        calcPopup.setSclass("decimalbox-calc-popup");

        appendChild(decimalbox);
        appendChild(calcButton);

        calculator.addEventListener(Events.ON_OK, e -> {
            setValue(BigDecimal.valueOf(calculator.getResult().doubleValue()));
            Events.postEvent(Events.ON_CHANGE, DecimalboxCalculator.this, value);
        });

        calcButton.addEventListener(Events.ON_CLICK, e -> {
            calculator.setValue("");
            calcPopup.open(calcButton);
            calculator.getDisplayBox().focus();
        });

        decimalbox.addEventListener(Events.ON_CHANGE, e -> {
            setValue(decimalbox.getValue());
            Events.postEvent(this, e);
        });
    }


    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        if (!Objects.equals(this.value, value)) {
            decimalbox.setValue(value);
            this.value = value;
        }

    }

    public double doubleValue() throws WrongValueException {
        return decimalbox.doubleValue();
    }

    public int intValue() throws WrongValueException {
        return decimalbox.intValue();
    }

    public long longValue() throws WrongValueException {
        return decimalbox.longValue();
    }

    public short shortValue() throws WrongValueException {
        return decimalbox.shortValue();
    }

    public int getScale() {
        return decimalbox.getScale();
    }

    public void setScale(int scale) {
        decimalbox.setScale(scale);
    }

    public void setRoundingMode(int mode) {
        decimalbox.setRoundingMode(mode);
    }

    public void setRoundingMode(String name) {
        decimalbox.setRoundingMode(name);
    }

    public int getRoundingMode() {
        return decimalbox.getRoundingMode();
    }

    public Locale getLocale() {
        return decimalbox.getLocale();
    }

    public void setLocale(Locale locale) {
        decimalbox.setLocale(locale);
    }

    public void setLocale(String locale) {
        decimalbox.setLocale(locale);
    }

    public String getFormat() {
        return decimalbox.getFormat();
    }

    public void setFormat(String format) throws WrongValueException {
        decimalbox.setFormat(format);
    }

    public String getPlaceholder() {
        return decimalbox.getPlaceholder();
    }

    public void setPlaceholder(String placeholder) {
        decimalbox.setPlaceholder(placeholder);
    }

    public void setInplace(boolean inplace) {
        decimalbox.setInplace(inplace);
    }

    public boolean isInplace() {
        return decimalbox.isInplace();
    }

    public boolean isDisabled() {
        return decimalbox.isDisabled();
    }

    public void setDisabled(boolean disabled) {
        decimalbox.setDisabled(disabled);
        calcButton.setVisible(!disabled);
    }

    public boolean isReadonly() {
        return decimalbox.isReadonly();
    }

    public void setReadonly(boolean readonly) {
        decimalbox.setReadonly(readonly);
        calcButton.setVisible(!readonly);
    }

    public void setConstraint(String constr) {
        decimalbox.setConstraint(constr);
    }

    public void setConstraint(Constraint constr) {
        decimalbox.setConstraint(constr);
    }

    public Constraint getConstraint() {
        return decimalbox.getConstraint();
    }

    public Decimalbox getDecimalbox() {
        return decimalbox;
    }

    public Calculator getCalculator() {
        return calculator;
    }
}
