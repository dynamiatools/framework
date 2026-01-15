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

/**
 * Builder interface for creating navigation UI components such as menus or trees.
 * <p>
 * Implementations of this interface are responsible for constructing views that represent
 * the application's navigation structure, including modules, page groups, and individual pages.
 * </p>
 * <p>
 * Usage: Use this builder to generate navigation views dynamically based on the application's
 * configuration or user permissions.
 * </p>
 *
 * @param <V> the type of navigation view produced by this builder
 * @author Mario A. Serrano Leones
 * @since 2023
 */
public interface NavigationViewBuilder<V> {

    /**
     * Returns the navigation view instance built by this builder.
     *
     * @return the navigation view of type {@code V}
     */
    V getNavigationView();

    /**
     * Creates and adds a view for the specified module to the navigation UI.
     *
     * @param module the {@link Module} to represent in the navigation view
     */
    void createModuleView(Module module);

    /**
     * Creates and adds a view for the specified page group to the navigation UI.
     *
     * @param pageGroup the {@link PageGroup} to represent in the navigation view
     */
    void createPageGroupView(PageGroup pageGroup);

    /**
     * Creates and adds a view for the specified page to the navigation UI.
     *
     * @param page the {@link Page} to represent in the navigation view
     */
    void createPageView(Page page);
}
