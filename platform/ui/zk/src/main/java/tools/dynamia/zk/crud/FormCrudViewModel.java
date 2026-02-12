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

package tools.dynamia.zk.crud;

import tools.dynamia.zk.viewers.form.FormView;

/**
 * Interface for view models that manage form initialization in CRUD operations.
 * <p>
 * This interface allows custom initialization and configuration of form views within CRUD contexts.
 * Implementations can customize form behavior, add validators, configure field properties, or inject
 * additional UI components before the form is displayed to the user. This is particularly useful for
 * implementing business-specific form logic that goes beyond standard CRUD operations.
 * </p>
 *
 * <p>
 * <b>Key use cases:</b>
 * <ul>
 *   <li>Custom form field initialization based on business rules</li>
 *   <li>Dynamic field visibility or enabling/disabling</li>
 *   <li>Adding custom validators or event listeners to form fields</li>
 *   <li>Injecting additional UI components or widgets into the form</li>
 *   <li>Configuring form layout or styling based on context</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * @Component
 * public class OrderFormViewModel implements FormCrudViewModel<Order> {
 *
 *     @Override
 *     public void initForm(CrudView<Order> crudView, FormView<Order> formView) {
 *         Order order = formView.getValue();
 *
 *         // Add custom validators
 *         formView.getFieldComponet("total").setLabel("Total Amount");
 *
 *
 *
 *         // Add custom event listener
 *         formView.getFielComponent("discount").getInputComponent().addEventListener(Events.ON_CHANGE, evt -> {
 *             recalculateTotal(formView);
 *         });
 *     }
 * }
 * }</pre>
 * </p>
 *
 * @param <T> the entity type being managed by the CRUD form
 * @author Mario A. Serrano Leones
 * @see CrudView
 * @see FormView
 */
public interface FormCrudViewModel<T> {

    /**
     * Initializes and configures the form view before it's displayed.
     * <p>
     * This method is called by the CRUD framework after the form view is created but before
     * it's rendered. Implementations can customize form fields, add validators, configure
     * layouts, or perform any other initialization needed for the specific entity type.
     * </p>
     *
     * @param crudView the parent CRUD view containing the form
     * @param formView the form view to be initialized
     */
    void initForm(CrudView<T> crudView, FormView<T> formView);
}
