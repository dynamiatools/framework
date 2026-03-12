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
import java.util.Map;

/**
 * Defines the layout configuration of a {@link ViewDescriptor}.
 *
 * <p>A {@code ViewLayout} holds a set of named parameters that control how a view is arranged
 * visually (e.g., number of columns, spacing, orientation). These parameters are interpreted by
 * the concrete {@link ViewRenderer} when building the UI component.</p>
 *
 * <p>Parameters can be set individually via {@link #addParam(String, Object)} or in bulk via
 * {@link #addParams(Map)}, and are retrieved collectively through {@link #getParams()}.</p>
 *
 * @see ViewDescriptor
 * @see ViewRenderer
 */
public interface ViewLayout extends Serializable {

    /**
     * Returns all layout parameters as an unmodifiable (or live) map.
     *
     * <p>Keys are parameter names (e.g., {@code "columns"}, {@code "orientation"}) and values
     * are the corresponding configuration objects.</p>
     *
     * @return a non-null map of layout parameters; may be empty
     */
    Map<String, Object> getParams();

    /**
     * Adds or replaces a single layout parameter.
     *
     * @param name  the parameter name; must not be {@code null}
     * @param value the parameter value; may be {@code null} to clear the parameter
     */
    void addParam(String name, Object value);

    /**
     * Adds or replaces multiple layout parameters in a single call.
     *
     * <p>Entries in the provided map are merged into the existing parameters, overwriting any
     * previously set values for the same keys.</p>
     *
     * @param params a map of parameter names to values; must not be {@code null}
     */
    void addParams(Map<String, Object> params);
}
