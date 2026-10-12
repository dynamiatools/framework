
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
 * Response of the dashboard widget data endpoint: the widget definition plus its current data.
 *
 * @param field         the dashboard descriptor field name the widget is bound to
 * @param widget        the widget id
 * @param type          the widget type, see {@link DashboardWidgetTypes}
 * @param title         the widget title
 * @param titleVisible  whether the title header is shown
 * @param editable      whether the widget is editable
 * @param closable      whether the widget is closable
 * @param maximizable   whether the widget is maximizable
 * @param data          the widget data, whose shape depends on the type
 * @author Mario Serrano Leones
 */
public record DashboardWidgetResponse(String field, String widget, String type, String title, boolean titleVisible,
                                      boolean editable, boolean closable, boolean maximizable, Object data) {
}
