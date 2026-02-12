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
 * Defines how an {@link Action} should be rendered in a user interface (UI).
 * <p>
 * Implementations of this interface are responsible for converting an {@link Action} and its associated event builder
 * into a UI component or element of type <T>. This allows flexible rendering of actions in different UI frameworks or
 * component models, such as buttons, menu items, links, etc.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 *     ActionRenderer<Button> renderer = ...;
 *     Button button = renderer.render(action, eventBuilder);
 * </pre>
 * </p>
 *
 * @param <T> the type of UI component or element produced by the renderer
 */
public interface ActionRenderer<T> {

    /**
     * Renders the given {@link Action} using the provided {@link ActionEventBuilder} and returns a UI component.
     * <p>
     * The returned component can be added to the UI and will be configured to trigger the action when interacted with.
     * </p>
     *
     * @param action the action to render
     * @param actionEventBuilder the builder for creating action events
     * @return a UI component or element representing the action
     */
    T render(Action action, ActionEventBuilder actionEventBuilder);

}
