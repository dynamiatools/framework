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
 * Fine-grained observer that participates in the {@link ViewRenderer} rendering pipeline.
 *
 * <p>A {@code ViewRendererCustomizer} is scoped to a specific domain class and view type
 * (declared via {@link #getTargetBeanClass()} and {@link #getTargetViewType()}). The framework
 * applies only the customizers whose target matches the descriptor being rendered, keeping each
 * customizer focused and side-effect free for unrelated views.</p>
 *
 * <p>The interface exposes three interception points:
 * <ol>
 *   <li>{@link #isRenderable(Field)} — gate that lets the customizer suppress individual fields
 *       before any component is created.</li>
 *   <li>{@link #beforeRender(View)} — called once before the renderer starts iterating fields;
 *       useful for initialising shared state or adjusting the view container.</li>
 *   <li>{@link #afterRender(View)} — called once after all field components have been created;
 *       useful for final layout adjustments or binding additional behaviour.</li>
 *   <li>{@link #afterFieldRender(Field, Object)} — called after each individual field component
 *       is created; useful for per-field post-processing.</li>
 * </ol>
 * </p>
 *
 * <p>Implementations must be serializable and are typically registered as application beans.</p>
 *
 * @param <V> the concrete {@link View} subtype this customizer operates on
 * @see ViewRenderer
 * @see ViewCustomizer
 */
public interface ViewRendererCustomizer<V extends View> extends Serializable {

    /**
     * Returns the domain class this customizer targets.
     *
     * <p>The framework only applies this customizer when the descriptor's
     * {@link ViewDescriptor#getBeanClass()} is assignable to the returned class.</p>
     *
     * @return the target bean class; never {@code null}
     */
    Class<?> getTargetBeanClass();

    /**
     * Returns the view type name this customizer targets (e.g., {@code "form"}, {@code "table"}).
     *
     * <p>The framework only applies this customizer when the descriptor's
     * {@link ViewDescriptor#getViewTypeName()} matches the returned value. Return {@code "*"} or
     * {@code null} (if supported by the implementation) to match any view type.</p>
     *
     * @return the target view type name; never {@code null}
     */
    String getTargetViewType();

    /**
     * Determines whether the given field should be rendered.
     *
     * <p>Returning {@code false} instructs the renderer to skip this field entirely — no UI
     * component will be created for it. This is evaluated before
     * {@link #beforeRender(View)} is called.</p>
     *
     * @param field the field candidate; never {@code null}
     * @return {@code true} if the field should be rendered; {@code false} to suppress it
     */
    boolean isRenderable(Field field);

    /**
     * Invoked once before the renderer begins iterating the field list.
     *
     * <p>Use this hook to prepare shared resources, adjust the view container, or set global
     * rendering options.</p>
     *
     * @param view the view being rendered; never {@code null}
     */
    void beforeRender(V view);

    /**
     * Invoked once after the renderer has finished creating all field components.
     *
     * <p>Use this hook for final layout tweaks, binding aggregate event listeners, or any
     * operation that requires the complete set of field components to be available.</p>
     *
     * @param view the fully rendered view; never {@code null}
     */
    void afterRender(V view);

    /**
     * Invoked after each individual field component is created by the renderer.
     *
     * <p>Use this hook to apply per-field post-processing such as adding tooltips, custom
     * validators, or event handlers to the rendered component.</p>
     *
     * @param field     the field descriptor whose component was just created; never {@code null}
     * @param component the UI component created for the field; never {@code null}
     */
    void afterFieldRender(Field field, Object component);
}
