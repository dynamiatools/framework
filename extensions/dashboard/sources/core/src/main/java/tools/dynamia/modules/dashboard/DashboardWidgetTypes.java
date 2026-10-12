
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
 * Well-known values for {@link DashboardWidgetDefinition#getType()}. A JS frontend selects the component that
 * renders a widget from its type. Applications may use any other value for their own widget kinds.
 *
 * @author Mario Serrano Leones
 */
public final class DashboardWidgetTypes {

    /** A chart. Its data is a {@link ChartWidgetData}. */
    public static final String CHART = "chart";
    /** A viewer (form, table, tree...). Its data is a {@link ViewerWidgetData}. */
    public static final String VIEWER = "viewer";
    /** A single key indicator. Its data is a {@link KpiWidgetData}. */
    public static final String KPI = "kpi";
    /** Free HTML content. Its data is the HTML string. */
    public static final String HTML = "html";
    /** Any other widget; its data is application specific. This is the default. */
    public static final String CUSTOM = "custom";

    private DashboardWidgetTypes() {
    }
}
