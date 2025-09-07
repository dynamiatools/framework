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
 * Default implementation of the {@link ActionRenderer} interface for rendering actions.
 * <p>
 * This renderer creates a basic {@link ActionComponent} using the provided {@link Action} and {@link ActionEventBuilder}.
 * It is typically used when no custom rendering logic is required for actions.
 * <p>
 * This class is stateless and thread-safe.
 *
 * @author Mario A. Serrano Leones
 * @since 2023
 */
public class DefaultActionRenderer implements ActionRenderer<ActionComponent> {

    /**
     * Renders the given {@link Action} using the specified {@link ActionEventBuilder}.
     * <p>
     * This method creates a new {@link ActionComponent} that encapsulates the action and its event builder.
     *
     * @param action the action to be rendered
     * @param actionEventBuilder the builder for action events
     * @return a new {@link ActionComponent} representing the rendered action
     */
    @Override
    public ActionComponent render(Action action, ActionEventBuilder actionEventBuilder) {

        return new ActionComponent(action, actionEventBuilder);
    }

}
