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
 * Interface for loading sub-views within a multi-view container.
 * <p>
 * This interface defines the contract for dynamically loading and creating child views that will
 * be displayed within a {@link MultiView} parent container. It enables lazy loading of views,
 * conditional view creation, and dynamic view composition based on runtime conditions or user
 * interactions. This is particularly useful in tabbed interfaces, wizard flows, or master-detail
 * scenarios where different views need to be loaded on demand.
 * </p>
 *
 * <p>
 * <b>Key use cases:</b>
 * <ul>
 *   <li>Lazy loading of expensive or complex views</li>
 *   <li>Dynamic tab content generation in tabbed interfaces</li>
 *   <li>Wizard step views loaded on demand</li>
 *   <li>Context-sensitive view creation based on user roles or data</li>
 *   <li>Master-detail views with dynamic detail panels</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * @Component
 * public class ProductDetailViewLoader implements ViewLoader {
 *
 *     @Autowired
 *     private ViewFactory viewFactory;
 *
 *     @Override
 *     public View loadSubview(MultiView parentView) {
 *         Product product = (Product) parentView.getValue();
 *
 *         // Load different views based on product type
 *         if (product.isDigital()) {
 *             return viewFactory.getView("digital-product-detail", product);
 *         } else {
 *             return viewFactory.getView("physical-product-detail", product);
 *         }
 *     }
 * }
 *
 * // Usage in MultiView
 * MultiView multiView = new MultiView();
 * multiView.setViewLoader(new ProductDetailViewLoader());
 * multiView.setValue(selectedProduct);
 * }</pre>
 * </p>
 *
 * @author Mario A. Serrano Leones
 * @see MultiView
 * @see View
 */
public interface ViewLoader {

    /**
     * Loads and returns a sub-view to be displayed within the parent multi-view container.
     * <p>
     * This method is called when the multi-view needs to create or load a child view.
     * Implementations should create and configure the appropriate view based on the
     * parent view's context, state, or data.
     * </p>
     *
     * @param parentView the parent multi-view container requesting the sub-view
     * @return the loaded sub-view to be displayed, or {@code null} if no view should be loaded
     */
    View loadSubview(MultiView parentView);

}
