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


/**
 * Interface for customizing {@link Field} instances based on the type of view being rendered.
 * <p>
 * Implementations of this interface can modify field properties such as visibility, label,
 * styling, component type, or validation rules before the field is rendered in a specific view.
 * This allows for dynamic field customization based on view context (e.g., form, table, tree).
 * </p>
 * <p>
 * Field customizers are typically invoked during the view descriptor processing phase,
 * allowing modifications to fields before UI components are created.
 * </p>
 *
 * Example:
 * <pre>{@code
 * public class ReadOnlyFormFieldCustomizer implements FieldCustomizer {
 *     public void customize(String viewTypeName, Field field) {
 *         if ("form".equals(viewTypeName) && field.getName().equals("id")) {
 *             field.addParam("readonly", true);
 *         }
 *     }
 * }
 * }</pre>
 *
 * @author Mario A. Serrano Leones
 */
public interface FieldCustomizer {

    /**
     * Customizes the given field for the specified view type.
     * <p>
     * This method is called during view descriptor processing, allowing implementations
     * to modify field properties based on the view type name (e.g., "form", "table", "tree").
     * </p>
     *
     * @param viewTypeName the name of the view type (e.g., "form", "table", "tree")
     * @param field the field to customize
     */
    void customize(String viewTypeName, Field field);
}
