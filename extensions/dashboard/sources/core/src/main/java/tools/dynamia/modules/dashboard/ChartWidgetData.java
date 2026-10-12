
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
 * Data of a {@link DashboardWidgetTypes#CHART} widget, shaped like a Chart.js configuration so a JS frontend can
 * pass it to Chart.js directly.
 *
 * @param type    the Chart.js chart type, e.g. {@code bar}, {@code line}, {@code pie}
 * @param data    the Chart.js {@code data} object (labels and datasets)
 * @param options the Chart.js {@code options} object, or {@code null} to use the defaults
 * @author Mario Serrano Leones
 */
public record ChartWidgetData(String type, Object data, Object options) {

    /**
     * Creates chart data without custom options.
     *
     * @param type the Chart.js chart type
     * @param data the Chart.js {@code data} object
     */
    public ChartWidgetData(String type, Object data) {
        this(type, data, null);
    }
}
