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

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to define an {@link Action} from a single method.
 * <p>
 * Use this annotation on a method to automatically generate an action in the system. The annotated method will be invoked
 * when the action is executed. You can customize the action's name, description, image, localization, renderer, and order.
 * </p>
 * <p>
 * <b>Example usage:</b>
 * <pre>
 *     @ActionCommand(name = "Save", description = "Save the current item", image = "save", order = 1)
 *     public void save(ActionEvent evt) {
 *         // Save logic here
 *     }
 * </pre>
 * </p>
 * <ul>
 *   <li><b>name</b>: The display name of the action.</li>
 *   <li><b>description</b>: A short description for tooltips or help.</li>
 *   <li><b>image</b>: The icon/image for the action.</li>
 *   <li><b>autolocalize</b>: If true, enables automatic localization of name/description.</li>
 *   <li><b>classier</b>: Optional classifier for grouping or filtering actions.</li>
 *   <li><b>actionRenderer</b>: The renderer class to use for this action.</li>
 *   <li><b>order</b>: The priority/order of the action in lists or toolbars.</li>
 * </ul>
 * <p>
 * <b>Example with Form:</b>
 * <pre>
 *     public class MyForm extends tools.dynamia.ui.Form {
 *         @ActionCommand(name = "Submit", description = "Submit the form", image = "submit", order = 1)
 *         public void submit(ActionEvent evt) {
 *             // Submit logic here
 *         }
 *
 *         @ActionCommand(name = "Reset", description = "Reset the form", image = "reset", order = 2)
 *         public void reset(ActionEvent evt) {
 *             // Reset logic here
 *         }
 *     }
 * </pre>
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface ActionCommand {
    /**
     * The display name of the action.
     *
     * @return the name of the action
     */
    String name() default "";

    /**
     * A short description for the action (e.g., tooltip).
     *
     * @return the description
     */
    String description() default "";

    /**
     * The icon/image for the action.
     *
     * @return the image name or path
     */
    String image() default "";

    /**
     * Enables automatic localization of name and description if true.
     *
     * @return true to autolocalize, false otherwise
     */
    boolean autolocalize() default true;

    /**
     * Optional classifier for grouping or filtering actions.
     *
     * @return the classifier string
     */
    String classier() default "";

    /**
     * The renderer class to use for this action.
     *
     * @return the ActionRenderer class
     */
    Class<? extends ActionRenderer<?>> actionRenderer() default DefaultActionRenderer.class;

    /**
     * The priority/order of the action in lists or toolbars.
     *
     * @return the order value
     */
    int order() default 0;

    /**
     * An optional type for further categorization of the action.
     *
     * @return the type string
     */
    String type() default "";
}
