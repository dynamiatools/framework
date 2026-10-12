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
 * <p>
 * {@code description}, {@code icon} and {@code color} are presentation hints. A frontend may ignore them; one that
 * renders the indicator with its own design system uses them to pick the icon and the tone of the card.
 *
 * @param value       the indicator value
 * @param label       the indicator label, or {@code null}
 * @param unit        the unit or currency, or {@code null}
 * @param trend       the variation against the previous period (for example {@code 0.12} for +12%), or {@code null}
 * @param description a short complementary line, for example {@code "12 sales made"}, or {@code null}
 * @param icon        an icon name the frontend understands (its meaning belongs to the frontend), or {@code null}
 * @param color       a semantic tone: {@code primary}, {@code success}, {@code danger}, {@code warning} or
 *                    {@code neutral}, or {@code null} for the frontend default
 * @author Mario Serrano Leones
 */
public record KpiWidgetData(Object value, String label, String unit, Number trend,
                            String description, String icon, String color) {

    /**
     * Creates a KPI without presentation hints.
     *
     * @param value the indicator value
     * @param label the indicator label
     * @param unit  the unit or currency, or {@code null}
     * @param trend the variation against the previous period, or {@code null}
     */
    public KpiWidgetData(Object value, String label, String unit, Number trend) {
        this(value, label, unit, trend, null, null, null);
    }

    /**
     * Creates a KPI with only a value and a label.
     *
     * @param value the indicator value
     * @param label the indicator label
     */
    public KpiWidgetData(Object value, String label) {
        this(value, label, null, null);
    }

    /**
     * Returns a copy of this KPI with the presentation hints.
     *
     * @param description a short complementary line, or {@code null}
     * @param icon        an icon name the frontend understands, or {@code null}
     * @param color       a semantic tone, or {@code null}
     * @return the new KPI
     */
    public KpiWidgetData withHints(String description, String icon, String color) {
        return new KpiWidgetData(value, label, unit, trend, description, icon, color);
    }
}
