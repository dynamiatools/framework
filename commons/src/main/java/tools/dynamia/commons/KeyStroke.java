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

package tools.dynamia.commons;

import java.io.Serializable;

/**
 * Helper class to store key strokes and callbacks
 */
public class KeyStroke implements Serializable {
    private final String ctrlkeys;
    private String label;
    private transient Callback callback;
    private boolean executing;
    private int keycode;


    public KeyStroke(KeyStroke originalStroke, Callback callback) {
        this.ctrlkeys = originalStroke.ctrlkeys;
        this.label = originalStroke.label;
        this.keycode = originalStroke.keycode;
        this.callback = callback;
    }

    public KeyStroke(String ctrlkeys, Callback callback) {
        this.ctrlkeys = ctrlkeys;
        this.callback = callback;
    }

    public KeyStroke(String ctrlkeys, String label) {
        this.ctrlkeys = ctrlkeys;
        this.label = label;
    }

    public KeyStroke(String ctrlkeys, String label, Callback callback) {
        this.ctrlkeys = ctrlkeys;
        this.label = label;
        this.callback = callback;
    }

    public void execute() {
        if (!executing && callback != null) {
            executing = true;
            callback.doSomething();
            executing = false;
        }
    }

    public String getCtrlkeys() {
        return ctrlkeys;
    }

    public String getLabel() {
        return label;
    }

    public int getKeycode() {
        return keycode;
    }

    public KeyStroke setLabel(String label) {
        this.label = label;
        return this;
    }

    public KeyStroke setKeycode(int keycode) {
        this.keycode = keycode;
        return this;
    }

    @Override
    public String toString() {
        if (label != null) {
            return label;
        } else {
            return ctrlkeys;
        }
    }
}
