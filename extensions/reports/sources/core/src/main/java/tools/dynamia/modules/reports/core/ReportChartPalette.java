package tools.dynamia.modules.reports.core;

/**
 * Colors used to draw report charts, shared by every front end so ZK and Vue charts look the same.
 */
public final class ReportChartPalette {

    /**
     * Colors in hexadecimal notation, in drawing order.
     */
    public static final String[] COLORS = {
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
    };

    private ReportChartPalette() {
    }

    /**
     * @param index position of the label or dataset
     * @return a color, cycling when there are more items than colors
     */
    public static String color(int index) {
        return COLORS[Math.floorMod(index, COLORS.length)];
    }
}
