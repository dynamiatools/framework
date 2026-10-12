
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

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardCoreTest {

    @Test
    void coreDoesNotDependOnZk() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("org.zkoss.zk.ui.Component"));
    }

    @Test
    void widgetContextSharesDataWithWidgetViews() {
        var context = new WidgetContext(null, null);
        context.add("total", 42);

        assertEquals(42, context.get("total"));
        assertNull(context.get("missing"));
        assertEquals(Map.of("total", 42), context.getDataMap());
    }

    @Test
    void abstractDefinitionHoldsCommonPropertiesAndIgnoresUpdates() {
        var widget = new AbstractDashboardWidgetDefinition() {
            @Override
            public String getId() {
                return "sales";
            }

            @Override
            public void init(WidgetContext context) {
            }
        };
        widget.setTitle("Sales");
        widget.setTitleVisible(true);
        widget.update(Map.of("range", "month"));

        assertEquals("Sales", widget.getTitle());
        assertTrue(widget.isTitleVisible());
    }
}
