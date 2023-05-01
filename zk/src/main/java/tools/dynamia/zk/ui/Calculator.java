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

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Textbox;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.commons.math.MathFunction;

import java.util.Arrays;
import java.util.List;

public class Calculator extends Keypad {


    private Number result;


    public Calculator() {
        super(new Textbox());
        List<Key> calculatorKeys = Arrays.asList(
                new Key("%"), new Key("/"), new Key("x", "*"), new Key("-"), NEW_ROW,
                new Key("7"), new Key("8"), new Key("9"), new Key("+", 1, 2), NEW_ROW,
                new Key("4"), new Key("5"), new Key("6"), NEW_ROW,
                new Key("1"), new Key("2"), new Key("3"),
                new Key("=", 1, 2)
                        .setCommand(e -> calc())
                        .setSclass("kp-key-green"), NEW_ROW,
                new Key("0", 2, 1), new Key(","), NEW_ROW,
                new Key("").setLabel("CE").setCommand(e -> clear()), new Key("("), new Key(")"),
                new Key("").setLabel("backspace").setIcon("fa fa-arrow-left").setCommand(e -> backspace()));
        setDefaultKeys(calculatorKeys);
        init();
        getDisplayBox().setSclass("kp-display-box");
        getDisplayRegion().appendChild(getDisplayBox());

        setWidth("400px");
        setHeight("400px");
        setValue("");
        getDisplayBox().addEventListener(Events.ON_OK, e -> {
            setValue(getDisplayBox().getText());
            calc();
        });
    }

    public void calc() {
        try {
            String exp = getValue();
            exp = exp.replace("%", "/100");
            result = MathFunction.evaluate(exp);
            setValue(String.valueOf(result));
            Events.postEvent(Events.ON_OK, this, result);
            getDisplayBox().setStyle(null);
        } catch (Exception e) {
            styleError();
        }
    }

    private void styleError() {
        getDisplayBox().setStyle("color: red; border-color: red");
    }

    @Override
    public void appendValue(String delta) {
        try {
            if (isOperation(delta)) {
                String lastChar = StringUtils.getLastCharacter(getValue());
                if (lastChar != null && !isOperation(lastChar)) {
                    super.appendValue(delta);
                }
            } else {
                super.appendValue(delta);
            }
        } catch (Exception e) {
            styleError();
        }
    }

    private boolean isOperation(String delta) {
        return switch (delta) {
            case "+", "-", "/", "*", "%", ")", "(" -> true;
            default -> false;
        };
    }

    @Override
    public void clear() {
        setValue("");
        getDisplayBox().setStyle(null);
    }

    public Number getResult() {
        return result;
    }
}
