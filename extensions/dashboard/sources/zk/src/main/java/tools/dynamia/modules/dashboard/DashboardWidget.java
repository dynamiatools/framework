
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

package tools.dynamia.modules.dashboard;

/**
 * Dashboard Widget API for ZK. It adds the ZK view ({@link #getView()}) and the ZK {@link DashboardContext} to the
 * UI-agnostic {@link DashboardWidgetDefinition}. New widgets can extend {@link AbstractDashboardWidget} to get a
 * basic implementation.
 *
 * @param <V> the view type: a ZK component, or a zul uri {@link String}
 * @author Mario Serrano Leones
 */
public interface DashboardWidget<V> extends DashboardWidgetDefinition {

    /**
     * Loads the data the widget needs, with access to the ZK dashboard and widget window.
     *
     * @param context the ZK dashboard context
     */
    void init(DashboardContext context);

    /**
     * Bridges the UI-agnostic lifecycle to {@link #init(DashboardContext)}.
     *
     * @param context a context that must be a {@link DashboardContext}
     * @throws IllegalArgumentException if the context is not a {@link DashboardContext}
     */
    @Override
    default void init(WidgetContext context) {
        if (context instanceof DashboardContext dashboardContext) {
            init(dashboardContext);
        } else {
            throw new IllegalArgumentException("ZK dashboard widget " + getId() + " requires a DashboardContext");
        }
    }

    /**
     * Returns the widget view.
     *
     * @return a ZK component, or a zul uri {@link String}
     */
    V getView();
}
