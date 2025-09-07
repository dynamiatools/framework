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
 * Interface for receiving notifications about the lifecycle events of an {@link Action} instance.
 * <p>
 * Implement this interface in a class that extends {@link Action} or any of its subclasses to be notified when the action
 * is created, destroyed, or rendered in the UI. This is useful for resource management, initialization, cleanup, or customizing
 * rendering behavior.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 *     public class MyAction extends Action implements ActionLifecycleAware {
 *         @Override
 *         public void onCreate() {
 *             // Initialization logic
 *         }
 *         @Override
 *         public void onDestroy() {
 *             // Cleanup logic
 *         }
 *         @Override
 *         public void beforeRenderer(ActionRenderer renderer) {
 *             // Custom logic before rendering
 *         }
 *         @Override
 *         public void afterRenderer(ActionRenderer renderer, Object component) {
 *             // Custom logic after rendering
 *         }
 *     }
 * </pre>
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
public interface ActionLifecycleAware {

    /**
     * Called when the action instance is created.
     * Use this method for initialization logic or resource allocation.
     */
    default void onCreate() {
    }

    /**
     * Called when the action instance is destroyed.
     * Use this method for cleanup logic or resource release.
     */
    default void onDestroy() {
    }

    /**
     * Called before the action is rendered in the UI.
     * Use this method to customize rendering or prepare the renderer.
     *
     * @param renderer the renderer that will render the action
     */
    default void beforeRenderer(ActionRenderer renderer) {
    }

    /**
     * Called after the action is rendered in the UI.
     * Use this method to perform post-rendering logic or interact with the rendered component.
     *
     * @param renderer the renderer that rendered the action
     * @param component the UI component produced by the renderer
     */
    default void afterRenderer(ActionRenderer renderer, Object component) {
    }

}
