package mybookstore.viewmodel;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import tools.dynamia.zk.ui.chartjs.*;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class ChartDemoViewModel {


    private CategoryChartjsData categoryModel;
    private ChartjsData xyModel;
    private ChartjsData lineModel;
    private ChartjsData xyzModel;
    private ChartjsData radarModel;
    private ChartjsOptions xyOptions;


    private ChartjsData mixedModel;

    public ChartDemoViewModel() {
        load();
        initXYOptions();
    }

    private void initXYOptions() {

        xyOptions = new ChartjsOptions();
        xyOptions.setScales(
                new Scales()
                        .addY(Axe.Builder.init()
                                .type("linear")
                                .display(true)
                                .position("left")
                                .id("y-axis-1")
                                .build())

                        .addY(Axe.Builder.init()
                                .type("linear")
                                .display(true)
                                .position("right")
                                .id("y-axis-2")
                                .build()
                                .addGridLine(GridLine.Builder.init()
                                        .drawOnChartArea(false)
                                        .build())

                        )
        );

        xyOptions.setTooltips(Tooltips.Builder.init()
                .position("nearest")
                .mode("index")
                .intersect(false)
                .build());

    }

    @Command
    @NotifyChange("*")
    public void load() {
        initCategoryModel();
        initXYModel();
        initRadarModel();
        initMixedModel();
    }


    private void initCategoryModel() {
        int max = 2000;
        int min = 500;
        categoryModel = new CategoryChartjsData();

        categoryModel
                .setDatasetLabel("Sales by Year")
                .add("2011", ThreadLocalRandom.current().nextInt(min, max))
                .add("2012", ThreadLocalRandom.current().nextInt(min, max))
                .add("2013", ThreadLocalRandom.current().nextInt(min, max))
                .add("2014", ThreadLocalRandom.current().nextInt(min, max))
                .add("2015", ThreadLocalRandom.current().nextInt(min, max))
                .add("2016", ThreadLocalRandom.current().nextInt(min, max))
                .add("2017", ThreadLocalRandom.current().nextInt(min, max))
                .add("2018", ThreadLocalRandom.current().nextInt(min, max));
    }

    private void initXYModel() {
        xyModel = new ChartjsData();
        xyModel.setLabels("January", "February", "March", "April", "June", "July", "August", "September", "October", "November", "December");

        xyzModel = new ChartjsData();
        xyzModel.setLabels(xyModel.getLabels());
        lineModel = new ChartjsData();

        XYDataset dataset1 = new XYDataset("First Dataset");
        dataset1.setFill(false);
        dataset1.setBorderColor("#93271d");
        dataset1.setBackgroundColor("#93271d");
        dataset1.setLineTension(0);

        xyModel.addDataset(dataset1);
        lineModel.addDataset(dataset1);

        XYDataset dataset2 = new XYDataset("Second Dataset");
        dataset2.setFill(false);
        dataset2.setBorderColor("#2a5eb2");
        dataset2.setBackgroundColor("#2a5eb2");
        dataset2.setLineTension(0);
        xyModel.addDataset(dataset2);

        XYDataset dataset3 = new XYDataset("XYZ Dataset");
        dataset3.setBorderColor("#2faf3a");
        dataset3.setBackgroundColor("#2faf3a");
        xyzModel.addDataset(dataset3);

        int max = 100;
        int min = 10;
        int months = 12;
        for (int i = 0; i < months; i++) {
            int x = ThreadLocalRandom.current().nextInt(min, max);
            int y = ThreadLocalRandom.current().nextInt(min, max);
            dataset1.addData(x, y);


            x = ThreadLocalRandom.current().nextInt(min, max);
            y = ThreadLocalRandom.current().nextInt(min, max);
            dataset2.addData(x, y);

            x = ThreadLocalRandom.current().nextInt(min, max);
            y = ThreadLocalRandom.current().nextInt(min, max);
            int r = ThreadLocalRandom.current().nextInt(10, 20);
            dataset3.addData(x, y, r);
        }
    }

    private void initRadarModel() {
        radarModel = new ChartjsData();
        radarModel.setLabels("Strength", "Speed", "Power", "Stamina", "Mana");

        radarModel.addDataset(Dataset.Builder.init()
                .label("Goku")
                .backgroundColor("#ffe900")
                .data(Arrays.asList(4000, 800, 5000, 2000, 3200))
                .build());


        radarModel.addDataset(Dataset.Builder.init()
                .label("Vegeta")
                .backgroundColor("#d400ffcc")
                .data(Arrays.asList(3800, 1800, 3000, 1500, 500))
                .build());

    }

    private void initMixedModel() {
        Dataset<Double> chartDataset1 = new Dataset<Double>();
        chartDataset1.setLabel("Normal Curve");
        chartDataset1.setType(Chartjs.TYPE_LINE);
        chartDataset1.setFill(false);
        IntStream.range(0, 100).mapToDouble(Double::valueOf).forEach(chartDataset1::addData);

        Dataset<Double> chartDataset2 = new Dataset<Double>();
        chartDataset2.setLabel("Measured Values");
        chartDataset2.setType(Chartjs.TYPE_SCATTER);
        chartDataset2.setBackgroundColor("#ff0000");
        chartDataset2.setFill(false);
        IntStream.range(0, 20).mapToDouble(e -> Math.random() * 100).forEach(chartDataset2::addData);

        ChartjsData chartData = new ChartjsData();
        chartData.addDataset(chartDataset1);
        chartData.addDataset(chartDataset2);
        chartData.addDataset(getCategoryModel().getDataset());
        this.mixedModel = chartData;
    }


    public CategoryChartjsData getCategoryModel() {
        return categoryModel;
    }

    public ChartjsData getXyModel() {
        return xyModel;
    }

    public void setCategoryModel(CategoryChartjsData categoryModel) {
        this.categoryModel = categoryModel;
    }

    public void setXyModel(ChartjsData xyModel) {
        this.xyModel = xyModel;
    }

    public ChartjsData getLineModel() {
        return lineModel;
    }

    public void setLineModel(ChartjsData lineModel) {
        this.lineModel = lineModel;
    }

    public ChartjsData getXyzModel() {
        return xyzModel;
    }

    public void setXyzModel(ChartjsData xyzModel) {
        this.xyzModel = xyzModel;
    }

    public ChartjsData getRadarModel() {
        return radarModel;
    }

    public void setRadarModel(ChartjsData radarModel) {
        this.radarModel = radarModel;
    }

    public ChartjsOptions getXyOptions() {
        return xyOptions;
    }

    public void setXyOptions(ChartjsOptions xyOptions) {
        this.xyOptions = xyOptions;
    }

    public ChartjsData getMixedModel() {
        return mixedModel;
    }

    public void setMixedModel(ChartjsData mixedModel) {
        this.mixedModel = mixedModel;
    }
}
