
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
 * Dashbaord Widget API. New widgets can extend {@link AbstractDashboardWidget} to get basic implementation
 * @author Mario Serrano Leones
 */
public interface DashboardWidget<V> {

    String getId();

    String getName();

    String getTitle();

    boolean isAsyncSupported();

    boolean isMaximizable();

    boolean isClosable();

    boolean isEditable();

    boolean isTitleVisible();

    void init(DashboardContext context);

    void update(Map<String, Object> params);

    V getView();
}
