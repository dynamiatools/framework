
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
 * Data of a {@link DashboardWidgetTypes#VIEWER} widget. The frontend renders the value with the view described by
 * {@code descriptorId} (fetched with the metadata API) or, when there is no descriptor, with a default view of
 * {@code viewType}.
 *
 * @param descriptorId the view descriptor id, or {@code null}
 * @param viewType     the view type, e.g. {@code table}, used when there is no descriptor id
 * @param value        the value to show, e.g. a list of entities
 * @author Mario Serrano Leones
 */
public record ViewerWidgetData(String descriptorId, String viewType, Object value) {
}
