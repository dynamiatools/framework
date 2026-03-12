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
 * Strategy interface responsible for building a {@link View} from a {@link ViewDescriptor} and an optional value.
 *
 * <p>A {@code ViewRenderer} translates the abstract metadata held in a {@link ViewDescriptor} into
 * a concrete, framework-specific UI component tree. Each {@link ViewType} has exactly one associated
 * renderer, although the default renderer can be overridden at runtime via
 * {@link ViewTypeFactory#setCustomViewRenderer(String, Class)}.</p>
 *
 * <p>Implementations must be stateless and serializable, as a single instance may be reused
 * across multiple render calls.</p>
 *
 * <p>The rendering pipeline typically follows these steps:
 * <ol>
 *   <li>Create the root UI container for the view type (e.g., form panel, table).</li>
 *   <li>Iterate the {@link Field}s (and optionally {@link FieldGroup}s) from the descriptor.</li>
 *   <li>Instantiate and configure a UI component for each field.</li>
 *   <li>Apply {@link ViewRendererCustomizer}s and {@link ViewCustomizer}s.</li>
 *   <li>Return the fully constructed {@link View}.</li>
 * </ol>
 * </p>
 *
 * @param <T> the type of domain object (value) the rendered view will hold
 * @see ViewType
 * @see ViewDescriptor
 * @see ViewFactory
 * @see ViewRendererCustomizer
 */
public interface ViewRenderer<T> extends Serializable {

    /**
     * Renders a {@link View} from the given descriptor and initial value.
     *
     * <p>The renderer reads the field list, layout, and parameters from {@code descriptor}
     * and produces a fully initialized UI component. If {@code value} is non-null, it is
     * bound to the view before returning.</p>
     *
     * @param descriptor the view descriptor that defines fields, layout, and parameters;
     *                   must not be {@code null}
     * @param value      the initial domain object to display or edit; may be {@code null}
     * @return a fully constructed, non-null {@link View} backed by the given descriptor
     */
    View<T> render(ViewDescriptor descriptor, T value);
}
