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

import java.util.List;

/**
 * Defines a renderer for an {@link ActionGroup} and its associated components.
 * <p>
 * Implementations of this interface are responsible for rendering a group of actions into a specific UI or container type.
 * The generic type parameter <T> represents the type of UI component or element used for rendering actions.
 * </p>
 * <p>
 * Typical use cases include rendering actions as buttons, menu items, toolbar elements, or other interactive components
 * within a given container.
 * </p>
 *
 * @param <T> the type of UI component or element used for rendering actions
 */
public interface ActionsRenderer<T> {

    /**
     * Renders the specified {@link ActionGroup} into the provided container, using the given list of action components.
     * <p>
     * Implementations should add or arrange the action components within the container according to the group definition.
     * </p>
     *
     * @param group the group of actions to render
     * @param actionsComponents the list of UI components representing individual actions
     * @param container the container in which the actions will be rendered
     */
    void render(ActionGroup group, List<T> actionsComponents, T container);

}
