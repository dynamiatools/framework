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
package tools.dynamia.ui;

import tools.dynamia.commons.Callback;
import tools.dynamia.commons.PropertyChangeSupport;

/**
 * Base class for forms in the UI layer. This class provides common functionality for handling form actions such as submit, cancel, and close.
 * It also supports property change notifications to allow UI components to react to changes in form state.
 *
 * <p>Subclasses can override the submit(), cancel(), and close() methods to implement specific behavior for these actions.</p>
 *
 * @author Mario A. Serrano Leones
 */
public class Form extends PropertyChangeSupport {

    private Callback onSubmitCallback;
    private Callback onCancelCallback;
    private Callback onCloseCallback;

    /**
     * Trigger the submit action. This method checks if an onSubmitCallback is registered and executes it if present.
     * Subclasses can override this method to provide additional behavior on form submission.
     */
    protected void submit() {
        if (onSubmitCallback != null) {
            onSubmitCallback.doSomething();
        }
    }

    /**
     * Trigger the cancel action. This method checks if an onCancelCallback is registered and executes it if present.
     * Subclasses can override this method to provide additional behavior on form cancellation.
     */
    protected void cancel() {
        if (onCancelCallback != null) {
            onCancelCallback.doSomething();
        }
    }

    /**
     * Trigger the close action. This method checks if an onCloseCallback is registered and executes it if present.
     * Subclasses can override this method to provide additional behavior on form closure.
     */
    public void close() {
        if (onCloseCallback != null) {
            onCloseCallback.doSomething();
        }
    }

    /**
     * Setters for action callbacks. These methods allow external code to register callbacks that will be executed when the corresponding form actions are triggered.
     */
    public void onCancel(Callback onCancelCallback) {
        this.onCancelCallback = onCancelCallback;
    }

    /**
     * Set the callback to be executed when the form is submitted. This allows external code to define custom behavior for form submission.
     *
     * @param onSubmitCallback The callback to execute on form submission.
     */
    public void onSubmit(Callback onSubmitCallback) {
        this.onSubmitCallback = onSubmitCallback;
    }

    /**
     * Set the callback to be executed when the form is closed. This allows external code to define custom behavior for form closure.
     *
     * @param callback The callback to execute on form closure.
     */
    public void onClose(Callback callback) {
        this.onCloseCallback = callback;
    }


    /**
     * Notify PropertyChangeListeners change, this method automatically check if the
     * oldValue and newValue are different to fire the listeners.
     *
     */
    protected void notifyChange(String propertyName, Object oldValue, Object newValue) {
        if (oldValue == null || oldValue != newValue) {
            firePropertyChange(propertyName, oldValue, newValue);
        }
    }

}
