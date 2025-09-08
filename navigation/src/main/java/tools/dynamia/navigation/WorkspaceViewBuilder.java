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
package tools.dynamia.navigation;

import java.io.Serializable;
import java.util.Map;

/**
 * Builder interface for managing the main workspace area of the application.
 * <p>
 * Implementations of this interface are responsible for initializing, building, updating,
 * and closing views for pages within the workspace. The workspace typically represents the
 * primary content area, such as a DIV in a web page or a multi-tab interface in a desktop application.
 * </p>
 * <p>
 * Usage: Use this builder to control the lifecycle and presentation of pages in the workspace,
 * including dynamic updates and resource management.
 * </p>
 *
 * @param <T> the type of the workspace container managed by this builder
 * @author Mario A. Serrano Leones
 * @since 2023
 */
public interface WorkspaceViewBuilder<T> extends Serializable {

    /**
     * Initializes the workspace with the specified container.
     * <p>
     * This method should prepare the workspace for displaying content, setting up necessary resources or UI components.
     * </p>
     *
     * @param container the workspace container of type {@code T}
     */
    void init(T container);

    /**
     * Builds and displays the specified page in the workspace.
     * <p>
     * This method is responsible for rendering the page's content and integrating it into the workspace layout.
     * </p>
     *
     * @param page the {@link Page} to build and display
     */
    void build(Page page);

    /**
     * Updates the specified page in the workspace with new parameters.
     * <p>
     * This method allows dynamic changes to the page's content or state based on the provided parameters.
     * </p>
     *
     * @param page the {@link Page} to update
     * @param params a map of parameters to apply to the page
     */
    void update(Page page, Map<String, Serializable> params);

    /**
     * Closes the specified page in the workspace, releasing resources and removing it from view.
     * <p>
     * This method should handle cleanup and ensure the workspace remains consistent after the page is closed.
     * </p>
     *
     * @param page the {@link Page} to close
     */
    void close(Page page);
}
