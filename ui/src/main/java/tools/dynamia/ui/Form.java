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
package tools.dynamia.ui;

import tools.dynamia.commons.Callback;
import tools.dynamia.commons.PropertyChangeSupport;


public class Form extends PropertyChangeSupport {

    private Callback onSubmitCallback;
    private Callback onCancelCallback;
    private Callback onCloseCallback;


    protected void submit() {
        if (onSubmitCallback != null) {
            onSubmitCallback.doSomething();
        }
    }

    protected void cancel() {
        if (onCancelCallback != null) {
            onCancelCallback.doSomething();
        }
    }

    public void close() {
        if (onCloseCallback != null) {
            onCloseCallback.doSomething();
        }
    }

    public void onCancel(Callback onCancelCallback) {
        this.onCancelCallback = onCancelCallback;
    }

    public void onSubmit(Callback onSubmitCallback) {
        this.onSubmitCallback = onSubmitCallback;
    }

    public void onClose(Callback callback) {
        this.onCloseCallback = callback;
    }


    /**
     * Notify PropertyChangeListeners change, this method automatically check if the
     * oldValue and newValue are different to fire the listeners.
     *
     * @param propertyName
     * @param oldValue
     * @param newValue
     */
    protected void notifyChange(String propertyName, Object oldValue, Object newValue) {
        if (oldValue == null || oldValue != newValue) {
            firePropertyChange(propertyName, oldValue, newValue);
        }
    }

}
