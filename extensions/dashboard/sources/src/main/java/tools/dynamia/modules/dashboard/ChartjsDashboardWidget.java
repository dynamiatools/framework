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


import tools.dynamia.zk.ui.chartjs.Chartjs;
import tools.dynamia.zk.ui.chartjs.ChartjsColorPalette;
import tools.dynamia.zk.ui.chartjs.ChartjsData;

/**
 * Helper class to create ChartJS Widgets
 *
 * @author Mario Serrano Leones
 */
public abstract class ChartjsDashboardWidget extends AbstractDashboardWidget<Chartjs> {

    private ChartjsData data;
    public static final ChartjsColorPalette MATERIAL_COLORS = new ChartjsColorPalette("Material", new String[]{
            "#3366cc",
            "#dc3912",
            "#ff9900",
            "#109618",
            "#990099",
            "#0099c6",
            "#dd4477",
            "#66aa00",
            "#b82e2e",
            "#316395",
            "#994499",
            "#22aa99",
            "#aaaa11",
            "#6633cc",
            "#e67300",
            "#8b0707",
            "#651067",
            "#329262",
            "#5574a6",
            "#3b3eac",
            "#b77322",
            "#16d620",
            "#b91383",
            "#f4359e",
            "#9c5935",
            "#a9c413",
            "#2a778d",
            "#668d1c",
            "#bea413",
            "#0c5922",
            "#743411"
    });

    public static final ChartjsColorPalette DYNAMIA = new ChartjsColorPalette("Dynamia", new String[]{
            "#2563EB", // Blue 600 - base tech
            "#06B6D4", // Cyan 500 - energía
            "#10B981", // Emerald 500 - crecimiento
            "#F59E0B", // Amber 500 - acción
            "#EF4444", // Red 500 - alerta elegante
            "#8B5CF6", // Violet 500 - creatividad
            "#EC4899", // Pink 500 - énfasis moderno
            "#14B8A6", // Teal 500 - balance
            "#6366F1", // Indigo 500 - profundidad
            "#84CC16", // Lime 500 - dinamismo
            "#F97316", // Orange 500 - energía cálida
            "#22D3EE", // Cyan 400 - contraste light
            "#A855F7", // Purple 500 - sofisticación
            "#0EA5E9", // Sky 500 - claridad
            "#4ADE80", // Green 400 - positivo
            "#EAB308", // Yellow 500 - atención
            "#F43F5E", // Rose 500 - impacto
            "#3B82F6", // Blue 500 - variación principal
            "#9333EA", // Purple 600 - autoridad
            "#059669"  // Emerald 600 - solidez
    });

    @Override
    public void init(DashboardContext context) {
        data = initChartjsData(context);
    }

    @Override
    public Chartjs getView() {
        Chartjs chart = new Chartjs(getChartjsType(), data);
        customizeChart(chart);
        return chart;
    }

    public abstract ChartjsData initChartjsData(DashboardContext context);

    public abstract String getChartjsType();

    /**
     * customize chart before rendering
     *
     * @param chart
     */
    protected void customizeChart(Chartjs chart) {

    }

}
