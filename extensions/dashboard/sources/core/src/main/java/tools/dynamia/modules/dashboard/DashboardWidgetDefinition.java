
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

import java.util.Map;

/**
 * UI-agnostic contract of a dashboard widget: its identity, behaviour flags and lifecycle. It deliberately has no
 * notion of how the widget is rendered, so the same widget can be shown by a ZK view or served to a JS frontend.
 * UI modules extend it (the ZK module adds {@code DashboardWidget#getView()}). Extend
 * {@link AbstractDashboardWidgetDefinition} to get the common properties.
 *
 * @author Mario Serrano Leones
 */
public interface DashboardWidgetDefinition {

    /**
     * Returns the unique widget id, referenced by the {@code widget} param of a dashboard descriptor field.
     *
     * @return the widget id
     */
    String getId();

    /**
     * Returns the widget name.
     *
     * @return the widget name
     */
    String getName();

    /**
     * Returns the title shown in the widget header when {@link #isTitleVisible()} is true.
     *
     * @return the widget title
     */
    String getTitle();

    /**
     * Tells whether the widget can be initialized asynchronously.
     *
     * @return true if async initialization is supported
     */
    boolean isAsyncSupported();

    /**
     * Tells whether the widget can be maximized by the user.
     *
     * @return true if maximizable
     */
    boolean isMaximizable();

    /**
     * Tells whether the widget can be closed by the user.
     *
     * @return true if closable
     */
    boolean isClosable();

    /**
     * Tells whether the widget can be edited by the user.
     *
     * @return true if editable
     */
    boolean isEditable();

    /**
     * Tells whether the widget header with its title is shown.
     *
     * @return true if the title is visible
     */
    boolean isTitleVisible();

    /**
     * Returns the widget type, used by a JS frontend to pick the component that renders the widget.
     *
     * @return the type, see {@link DashboardWidgetTypes}; {@link DashboardWidgetTypes#CUSTOM} by default
     */
    default String getType() {
        return DashboardWidgetTypes.CUSTOM;
    }

    /**
     * Returns the widget data for a JS frontend, after {@link #init(WidgetContext)} has run. The shape depends on
     * {@link #getType()}, see {@link ChartWidgetData}, {@link KpiWidgetData} and {@link ViewerWidgetData}. The
     * value must be serializable to JSON.
     *
     * @param context the widget context
     * @return the widget data, or {@code null} if the widget has no data to serve (the default)
     */
    default Object getData(WidgetContext context) {
        return null;
    }

    /**
     * Loads the data the widget needs. Called before the widget is rendered and again on dashboard reload.
     *
     * @param context the widget context
     */
    void init(WidgetContext context);

    /**
     * Updates the widget with new parameters, for example from a dashboard filter.
     *
     * @param params the new parameters
     */
    void update(Map<String, Object> params);
}
