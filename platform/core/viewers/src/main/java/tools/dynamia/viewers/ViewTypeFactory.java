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
 * Central registry and resolver for {@link ViewType} instances.
 *
 * <p>A {@code ViewTypeFactory} is responsible for looking up registered {@link ViewType}s by name,
 * obtaining their associated {@link ViewRenderer}s, and allowing applications to override the
 * default renderer for any view type at runtime.</p>
 *
 * <p>Implementations are typically registered as application-scoped beans and resolved via
 * {@link tools.dynamia.integration.Containers}.</p>
 *
 * @see ViewType
 * @see ViewRenderer
 */
public interface ViewTypeFactory extends Serializable {

    /**
     * Returns the {@link ViewType} registered under the given name.
     *
     * @param name the unique name of the view type (e.g., {@code "form"}, {@code "table"});
     *             must not be {@code null}
     * @return the matching {@link ViewType}, or {@code null} if none is registered with that name
     */
    ViewType getViewType(String name);

    /**
     * Returns the {@link ViewRenderer} associated with the given {@link ViewType}.
     *
     * <p>If a custom renderer has been set for the view type via
     * {@link #setCustomViewRenderer(String, Class)}, that renderer is returned instead of the
     * default one.</p>
     *
     * @param viewType the view type whose renderer is requested; must not be {@code null}
     * @return the {@link ViewRenderer} for the given view type; never {@code null}
     */
    ViewRenderer getViewRenderer(ViewType viewType);

    /**
     * Overrides the default {@link ViewRenderer} for the view type identified by {@code viewTypeName}.
     *
     * <p>This allows applications or plugins to substitute a custom rendering strategy without
     * re-registering the entire {@link ViewType}. The override takes effect immediately and
     * is reflected in subsequent calls to {@link #getViewRenderer(ViewType)}.</p>
     *
     * @param viewTypeName      the name of the view type to override; must not be {@code null}
     * @param viewRendererClass the custom renderer class to associate with the view type;
     *                          must not be {@code null} and must have a no-arg constructor
     */
    void setCustomViewRenderer(String viewTypeName, Class<? extends ViewRenderer> viewRendererClass);
}
