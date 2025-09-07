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

import java.util.Map;

/**
 * Builder interface for creating {@link ActionEvent} instances with custom parameters and source objects.
 * <p>
 * Implement this interface when you need to customize how {@link ActionEvent} objects are constructed for action execution.
 * This is useful for scenarios where additional context, parameters, or logic are required to build the event before
 * triggering an action.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 *     ActionEventBuilder builder = ...;
 *     ActionEvent event = builder.buildActionEvent(source, params);
 * </pre>
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
public interface ActionEventBuilder {

    /**
     * Builds an {@link ActionEvent} using the provided source object and parameters.
     * <p>
     * The source typically represents the origin of the event (such as a UI component or domain object),
     * and the params map contains additional data to be passed to the action.
     * </p>
     *
     * @param source the source object for the event (e.g., UI component, domain entity)
     * @param params a map of parameters to include in the event
     * @return a constructed {@link ActionEvent} instance
     */
    ActionEvent buildActionEvent(Object source, Map<String, Object> params);

}
