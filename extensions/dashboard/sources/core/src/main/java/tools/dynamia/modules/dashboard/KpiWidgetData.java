
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
 * Data of a {@link DashboardWidgetTypes#KPI} widget: a single key indicator.
 *
 * @param value the indicator value
 * @param label the indicator label, or {@code null}
 * @param unit  the unit or currency, or {@code null}
 * @param trend the variation against the previous period (for example {@code 0.12} for +12%), or {@code null}
 * @author Mario Serrano Leones
 */
public record KpiWidgetData(Object value, String label, String unit, Number trend) {

    /**
     * Creates a KPI with only a value and a label.
     *
     * @param value the indicator value
     * @param label the indicator label
     */
    public KpiWidgetData(Object value, String label) {
        this(value, label, null, null);
    }
}
