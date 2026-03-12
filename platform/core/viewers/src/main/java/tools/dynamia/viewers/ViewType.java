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
 * Represents a named type of view in the DynamiaTools viewer framework.
 *
 * <p>A {@code ViewType} acts as a descriptor for a specific category of UI view (e.g., form,
 * table, tree). It provides both a unique name to identify the type and the corresponding
 * {@link ViewRenderer} responsible for building the visual representation.</p>
 *
 * <p>Implementations are typically registered in the application context and resolved through
 * a {@link ViewTypeFactory}.</p>
 *
 * @see ViewRenderer
 * @see ViewTypeFactory
 */
public interface ViewType extends Serializable {

    /**
     * Returns the unique name that identifies this view type (e.g., {@code "form"}, {@code "table"}).
     *
     * @return the non-null, non-empty name of this view type
     */
    String getName();

    /**
     * Returns the {@link ViewRenderer} responsible for rendering views of this type.
     *
     * <p>The renderer is used by the {@link ViewFactory} to build the actual UI component
     * from a {@link ViewDescriptor}.</p>
     *
     * @return the view renderer associated with this view type; never {@code null}
     */
    @SuppressWarnings("rawtypes")
    ViewRenderer getViewRenderer();

}
