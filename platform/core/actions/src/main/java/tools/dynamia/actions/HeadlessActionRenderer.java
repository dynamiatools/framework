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
package tools.dynamia.actions;

/**
 * The renderer of an action that leaves a {@link Control} in the toolbar. It builds nothing: it returns the description, and
 * each front end draws it (ZK with its own widgets, a remote client from the metadata). An action with this renderer is
 * portable as long as it only reads the value of the control.
 *
 * <pre>{@code
 * setRenderer(HeadlessActionRenderer.of(Control.dateRange("creationDate")));
 * }</pre>
 */
public class HeadlessActionRenderer implements ActionRenderer<Control> {

    private final Control control;

    /** @param control what the toolbar must show */
    public HeadlessActionRenderer(Control control) {
        this.control = java.util.Objects.requireNonNull(control, "control");
    }

    /** @return a renderer for {@code control} */
    public static HeadlessActionRenderer of(Control control) {
        return new HeadlessActionRenderer(control);
    }

    /** @return the control this renderer describes */
    public Control getControl() {
        return control;
    }

    @Override
    public Control render(Action action, ActionEventBuilder actionEventBuilder) {
        return control;
    }
}
