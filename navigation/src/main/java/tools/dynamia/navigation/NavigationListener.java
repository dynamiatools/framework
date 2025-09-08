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
 * Listener interface for navigation events within the application workspace.
 * <p>
 * Implement this interface to handle lifecycle events of pages, such as loading, unloading, and closing.
 * This allows custom logic to be executed when navigation changes occur.
 * </p>
 * <p>
 * Typical usage includes tracking user activity, managing resources, or updating UI components
 * in response to navigation events.
 * </p>
 *
 * @author Ing. Mario Serrano Leones
 * @since 2023
 */
public interface NavigationListener {

    /**
     * Invoked when a page is loaded and becomes active in the workspace.
     * <p>
     * This event is typically fired after the page's initialization and before user interaction.
     * </p>
     *
     * @param evt the {@link PageEvent} containing details about the loaded page
     */
    void onPageLoad(PageEvent evt);

    /**
     * Invoked when the current page is unloaded from the workspace.
     * <p>
     * This event is fired before the page is removed or replaced, allowing cleanup or state saving.
     * </p>
     *
     * @param evt the {@link PageEvent} containing details about the unloaded page
     */
    void onPageUnload(PageEvent evt);

    /**
     * Invoked when a page is closed permanently in the workspace.
     * <p>
     * This event is fired when the page is no longer available for navigation, such as when a tab is closed.
     * </p>
     *
     * @param evt the {@link PageEvent} containing details about the closed page
     */
    void onPageClose(PageEvent evt);
}
