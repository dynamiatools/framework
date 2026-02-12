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
package tools.dynamia.viewers;


import java.io.Serializable;

/**
 * Interface for customizing UI components associated with a {@link Field} in a view descriptor.
 * <p>
 * Implementations of this interface can modify component properties, behavior, or appearance
 * based on field metadata such as field type, parameters, or custom attributes defined in
 * the view descriptor.
 * </p>
 * <p>
 * This is typically used to apply framework-specific or application-specific customizations
 * to input components, labels, or other UI elements after they are created by the view renderer.
 * </p>
 *
 * Example:
 * <pre>{@code
 * public class ReadOnlyCustomizer implements ComponentCustomizer<Textbox> {
 *     public void cutomize(Field field, Textbox component) {
 *         if (field.getParams().get("readonly") == Boolean.TRUE) {
 *             component.setReadonly(true);
 *         }
 *     }
 * }
 * }</pre>
 *
 * @param <T> the type of component to customize (e.g., Textbox, Datebox, Combobox)
 * @author Dynamia Soluciones IT
 */
public interface ComponentCustomizer<T> extends Serializable {

    /**
     * Customizes the given component based on the field metadata.
     * <p>
     * This method is invoked after the component is created by the view renderer,
     * allowing implementations to apply additional configuration or styling.
     * </p>
     *
     * @param field the field descriptor containing metadata for customization
     * @param component the UI component to customize
     */
    void cutomize(Field field, T component);

}
