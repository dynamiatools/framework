
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

import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import tools.dynamia.zk.ui.chartjs.CategoryChartjsData;
import tools.dynamia.zk.ui.chartjs.Chartjs;
import tools.dynamia.zk.ui.chartjs.ChartjsData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChartjsDashboardWidgetDataTest {

    private static class SalesChart extends ChartjsDashboardWidget {
        @Override
        public String getId() {
            return "sales-chart";
        }

        @Override
        public ChartjsData initChartjsData(DashboardContext context) {
            var data = new CategoryChartjsData();
            data.add("Jan", 10.5);
            data.add("Feb", 20.0);
            data.setDatasetLabel("Sales");
            return data;
        }

        @Override
        public String getChartjsType() {
            return Chartjs.TYPE_BAR;
        }
    }

    @Test
    void servesChartDataFromAPlainWidgetContext() {
        var widget = new SalesChart();
        var context = new WidgetContext(null, null);

        widget.init(context);
        var data = assertInstanceOf(ChartWidgetData.class, widget.getData(context));

        assertEquals(DashboardWidgetTypes.CHART, widget.getType());
        assertEquals(Chartjs.TYPE_BAR, data.type());
        var json = JsonMapper.builder().build().readTree(JsonMapper.builder().build().writeValueAsString(data));
        var chartData = json.get("data");
        assertEquals("Jan", chartData.get("labels").get(0).asString());
        assertEquals("Feb", chartData.get("labels").get(1).asString());
        var dataset = chartData.get("datasets").get(0);
        assertEquals("Sales", dataset.get("label").asString());
        assertEquals(10.5, dataset.get("data").get(0).asDouble());
        assertEquals(20.0, dataset.get("data").get(1).asDouble());
    }
}
