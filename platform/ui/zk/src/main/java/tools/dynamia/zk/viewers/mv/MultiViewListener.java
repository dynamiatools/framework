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

package tools.dynamia.zk.viewers.mv;

import tools.dynamia.viewers.View;

/**
 * Listener interface for receiving events from multi-view containers.
 * <p>
 * This interface allows components to be notified when sub-views are loaded or selected within
 * a {@link MultiView} container. It's useful for implementing custom behavior when views change,
 * such as updating navigation breadcrumbs, logging view access, performing view-specific initialization,
 * or coordinating between parent and child views.
 * </p>
 *
 * <p>
 * <b>Key use cases:</b>
 * <ul>
 *   <li>Tracking view navigation and user interactions</li>
 *   <li>Coordinating data synchronization between views</li>
 *   <li>Implementing view-specific initialization or cleanup</li>
 *   <li>Updating UI elements based on active view</li>
 *   <li>Logging and analytics for view access patterns</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * @Component
 * public class ViewTrackingListener implements MultiViewListener {
 *
 *     @Autowired
 *     private AnalyticsService analyticsService;
 *
 *     @Override
 *     public void subviewLoaded(MultiView parentView, View subview) {
 *         logger.info("Subview loaded: {}", subview.getId());
 *         analyticsService.trackViewLoad(subview.getId());
 *
 *         // Perform view-specific initialization
 *         if (subview instanceof FormView) {
 *             ((FormView) subview).refresh();
 *         }
 *     }
 *
 *     @Override
 *     public void subviewSelected(MultiView parentView, View subview) {
 *         logger.info("Subview selected: {}", subview.getId());
 *
 *         // Update breadcrumb navigation
 *         updateBreadcrumbs(subview);
 *
 *         // Enable/disable toolbar buttons based on view
 *         configureToolbar(subview);
 *     }
 * }
 *
 * // Register listener
 * multiView.addListener(new ViewTrackingListener());
 * }</pre>
 * </p>
 *
 * @author Mario A. Serrano Leones
 * @see MultiView
 * @see View
 * @see ViewLoader
 */
public interface MultiViewListener {

    /**
     * Invoked when a sub-view has been loaded into the multi-view container.
     * <p>
     * This method is called after a sub-view is successfully created and loaded, but before
     * it's displayed. Implementations can use this to perform initialization, register event
     * handlers, or update related UI components.
     * </p>
     *
     * @param parentView the multi-view container that loaded the sub-view
     * @param subview the sub-view that was loaded
     */
    void subviewLoaded(MultiView parentView, View subview);

    /**
     * Invoked when a sub-view is selected (becomes the active view) in the multi-view container.
     * <p>
     * This method is called when the user or system selects a different view within the container,
     * such as switching tabs or navigating to a different panel. Implementations can use this to
     * update navigation state, refresh data, or coordinate UI updates.
     * </p>
     *
     * @param parentView the multi-view container where the selection occurred
     * @param subview the sub-view that was selected and is now active
     */
    void subviewSelected(MultiView parentView, View subview);

}
