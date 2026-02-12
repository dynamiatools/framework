
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

import java.util.HashMap;
import java.util.Map;

import tools.dynamia.integration.Containers;
import tools.dynamia.viewers.Field;

/**
 * Dashboard context
 *
 * @author Mario Serrano Leones
 */
public class DashboardContext {

    private Dashboard dashboard;
    private DashboardWidgetWindow window;
    private Map<String, Object> data = new HashMap<>();
    private Field field;

    public DashboardContext(Dashboard dashboard, DashboardWidgetWindow window, Field field) {
        this.dashboard = dashboard;
        this.window = window;
        this.window.setDashboardContext(this);
        this.field = field;
    }

    public DashboardWidgetWindow getWindow() {
        return window;
    }

    public void add(String name, Object value) {
        data.put(name, value);
    }

    public Object get(String name) {
        return data.get(name);
    }

    public Map<String, Object> getDataMap() {
        return data;
    }

    public Dashboard getDashboard() {
        return dashboard;
    }

    public Field getField() {
        return field;
    }

    /**
     * Find user info provider or null if not found
     *
     * @return
     */
    public UserInfoProvider findUserInfo() {
        return Containers.get().findObject(UserInfoProvider.class);
    }

}
