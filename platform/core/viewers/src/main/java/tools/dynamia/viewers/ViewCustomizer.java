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
 * Post-processing hook applied to a {@link View} immediately after it is constructed by a
 * {@link ViewRenderer}.
 *
 * <p>A {@code ViewCustomizer} allows framework users to programmatically adjust a view's
 * appearance or behaviour without subclassing the renderer. Typical uses include:
 * <ul>
 *   <li>Attaching event listeners to form components.</li>
 *   <li>Applying dynamic styles or CSS classes.</li>
 *   <li>Changing field visibility based on runtime conditions.</li>
 *   <li>Injecting additional child components.</li>
 * </ul>
 * </p>
 *
 * <p>The concrete customizer class is declared in the {@link ViewDescriptor} via
 * {@link ViewDescriptor#getViewCustomizerClass()} and is instantiated by the framework
 * after the view is fully built. Implementations must be serializable and have a
 * public no-arg constructor.</p>
 *
 * @param <T> the concrete {@link View} subtype this customizer operates on
 * @see ViewDescriptor#getViewCustomizerClass()
 * @see ViewRenderer
 */
public interface ViewCustomizer<T extends View> extends Serializable {

    /**
     * Applies custom post-processing logic to the given view.
     *
     * <p>This method is called once per view construction, after the {@link ViewRenderer}
     * has finished building all components. Modifications made here are reflected in the
     * final UI presented to the user.</p>
     *
     * @param view the fully constructed view to customize; never {@code null}
     */
    void customize(T view);
}
