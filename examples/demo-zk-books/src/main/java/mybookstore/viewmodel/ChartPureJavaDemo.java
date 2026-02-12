package mybookstore.viewmodel;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.zk.ui.Component;
import tools.dynamia.zk.ui.chartjs.*;

public class ChartPureJavaDemo {

    private Component parent;

    @AfterCompose
    public void afterCompose(@ContextParam(ContextType.VIEW) Component component) {
        this.parent = component;
        initChart();
    }

    private void initChart() {


        CategoryChartjsData data = new CategoryChartjsData();
        data.add("A", 11.1);
        data.add("B", 22.2);
        data.add("C", 33.3);
        data.add("D", 44.4);
        data.setDatasetLabel("The DataSet");

        Chartjs chart = new Chartjs(Chartjs.TYPE_BAR);
        chart.setData(data);
        chart.setOptions(
                ChartjsOptions.Builder.init()
                        .scales(new Scales()
                                .addY(Axe.Builder.init()
                                        .scaleLabel(ScaleLabel.Builder.init()
                                                .labelString("ChartJs For ZK")
                                                .display(true)
                                                .build()) // scale label
                                        .ticks(Ticks.Builder.init()
                                                .min(0)
                                                .max(100)
                                                .build()) // ticks
                                        .build())) //Axe
                        .build() // options
        );
        chart.setWidth("100%");
        chart.setParent(parent);
    }
}
