
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

import tools.dynamia.integration.Containers;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.ViewDescriptor;

import java.util.HashMap;
import java.util.Map;

/**
 * UI-agnostic context handed to a {@link DashboardWidgetDefinition} when it is initialized. It identifies the
 * dashboard descriptor and the field (widget slot) being initialized, and carries a data map that widgets use to
 * share values with their view. UI-specific modules extend it with their own references (for example the ZK
 * module adds the dashboard component and the widget window).
 *
 * <pre>{@code
 * public void init(WidgetContext context) {
 *     String range = (String) context.getField().getParams().get("range");
 *     context.add("total", salesService.total(range));
 * }
 * }</pre>
 *
 * @author Mario Serrano Leones
 */
public class WidgetContext {

    private final ViewDescriptor descriptor;
    private final Field field;
    private final Map<String, Object> data = new HashMap<>();

    /**
     * Creates a context for one widget slot of a dashboard.
     *
     * @param descriptor the dashboard view descriptor
     * @param field      the descriptor field the widget is bound to; its params carry the widget configuration
     */
    public WidgetContext(ViewDescriptor descriptor, Field field) {
        this.descriptor = descriptor;
        this.field = field;
    }

    /**
     * Returns the dashboard view descriptor this widget belongs to.
     *
     * @return the dashboard descriptor
     */
    public ViewDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Returns the descriptor field the widget is bound to.
     *
     * @return the field, whose params carry the widget configuration
     */
    public Field getField() {
        return field;
    }

    /**
     * Stores a value in the context data map.
     *
     * @param name  the value name
     * @param value the value
     */
    public void add(String name, Object value) {
        data.put(name, value);
    }

    /**
     * Returns a value from the context data map.
     *
     * @param name the value name
     * @return the value, or {@code null} if absent
     */
    public Object get(String name) {
        return data.get(name);
    }

    /**
     * Returns the whole context data map.
     *
     * @return the mutable data map
     */
    public Map<String, Object> getDataMap() {
        return data;
    }

    /**
     * Finds the application's {@link UserInfoProvider}.
     *
     * @return the provider, or {@code null} if none is registered
     */
    public UserInfoProvider findUserInfo() {
        return Containers.get().findObject(UserInfoProvider.class);
    }
}
