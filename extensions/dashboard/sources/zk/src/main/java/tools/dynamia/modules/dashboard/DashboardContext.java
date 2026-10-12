
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

import tools.dynamia.viewers.Field;

/**
 * ZK dashboard context. Extends the UI-agnostic {@link WidgetContext} with the ZK {@link Dashboard} and the
 * {@link DashboardWidgetWindow} that hosts the widget.
 *
 * @author Mario Serrano Leones
 */
public class DashboardContext extends WidgetContext {

    private final Dashboard dashboard;
    private final DashboardWidgetWindow window;

    /**
     * Creates the context and binds it to the widget window.
     *
     * @param dashboard the dashboard component
     * @param window    the window hosting the widget
     * @param field     the descriptor field the widget is bound to
     */
    public DashboardContext(Dashboard dashboard, DashboardWidgetWindow window, Field field) {
        super(dashboard.getViewDescriptor(), field);
        this.dashboard = dashboard;
        this.window = window;
        this.window.setDashboardContext(this);
    }

    /**
     * Creates a headless context that wraps a plain {@link WidgetContext} (same descriptor, field and shared data)
     * but has no ZK dashboard or window. It is used when a widget is initialized to serve data over REST, so
     * widgets used that way must not call {@link #getDashboard()} or {@link #getWindow()}.
     *
     * @param source the context to wrap
     */
    public DashboardContext(WidgetContext source) {
        super(source);
        this.dashboard = null;
        this.window = null;
    }

    /**
     * Returns the window hosting the widget, or {@code null} in a headless context.
     *
     * @return the widget window
     */
    public DashboardWidgetWindow getWindow() {
        return window;
    }

    /**
     * Returns the dashboard component, or {@code null} in a headless context.
     *
     * @return the dashboard
     */
    public Dashboard getDashboard() {
        return dashboard;
    }
}
