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
 * Provides a default {@link ActionRenderer} when an {@link Action} does not define its own renderer.
 * <p>
 * Implementations of this interface supply a named renderer that can be used to render actions in the UI
 * when no specific renderer is set for the action. This enables flexible and pluggable rendering strategies
 * for actions across different UI frameworks or contexts.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 *     ActionRenderProvider provider = ...;
 *     String rendererName = provider.getName();
 *     ActionRenderer renderer = provider.getActionRenderer();
 * </pre>
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
public interface ActionRenderProvider {

    /**
     * Returns the name of this action renderer provider.
     * <p>
     * The name can be used to identify or select the provider among multiple implementations.
     * </p>
     * @return the provider name
     */
    String getName();

    /**
     * Returns the {@link ActionRenderer} instance provided by this provider.
     * <p>
     * This renderer will be used to render actions that do not have a specific renderer defined.
     * </p>
     * @return the action renderer
     */
    ActionRenderer getActionRenderer();

}
