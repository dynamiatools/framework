/*
 * Copyright (C)  2020. Dynamia Soluciones IT S.A.S - NIT 900302344-1 All Rights Reserved.
 * Colombia - South America
 *
 * This file is free software: you can redistribute it and/or modify it  under the terms of the
 *  GNU Lesser General Public License (LGPL v3) as published by the Free Software Foundation,
 *   either version 3 of the License, or (at your option) any later version.
 *
 *  This file is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *   without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *   See the GNU Lesser General Public License for more details. You should have received a copy of the
 *   GNU Lesser General Public License along with this file.
 *   If not, see <https://www.gnu.org/licenses/>.
 *
 */
package tools.dynamia.modules.reports.ui;


import org.zkoss.zhtml.Hr;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SortEvent;
import org.zkoss.zul.*;
import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.ActionEventBuilder;
import tools.dynamia.actions.Actions;
import tools.dynamia.commons.*;
import tools.dynamia.commons.reflect.AccessMode;
import tools.dynamia.commons.reflect.PropertyInfo;
import tools.dynamia.crud.FilterCondition;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.query.QueryCondition;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.modules.dashboard.ChartjsDashboardWidget;
import tools.dynamia.modules.reports.api.EnumFilterProvider;
import tools.dynamia.modules.reports.core.*;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.ReportField;
import tools.dynamia.modules.reports.core.domain.ReportFilter;
import tools.dynamia.modules.reports.core.domain.enums.DataType;
import tools.dynamia.modules.reports.core.services.ReportsService;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.impl.DefaultViewDescriptor;
import tools.dynamia.web.util.HttpUtils;
import tools.dynamia.zk.actions.ButtonActionRenderer;
import tools.dynamia.zk.crud.ui.EntityFiltersPanel;
import tools.dynamia.zk.ui.chartjs.CategoryChartjsData;
import tools.dynamia.zk.ui.chartjs.Chartjs;
import tools.dynamia.zk.ui.chartjs.ChartjsOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.*;

public class ReportViewer extends Div implements ActionEventBuilder {


    public static final int MAX_RESULT_TO_DISPLAY = 2000;
    private final ClassMessages messages = ClassMessages.get(ReportViewer.class);
    private final ReportsService service;
    private Report report;
    private final ReportDataSource dataSource;
    private Borderlayout layout;
    private EntityFiltersPanel filtersPanel;
    private Listbox dataView;
    private ReportData reportData;
    private Button executeButton;
    private Button exportButton;
    private Button reloadButton;
    private Hlayout buttons;
    private final List<Action> actions;
    private List<Chartjs> currentCharts;
    private Component filtersContainer;
    private Component dataViewContainer;
    private Component chartsContainer;
    private ReportFilters filters;

    public ReportViewer(ReportsService service, Report report, ReportDataSource dataSource) {
        this.service = service;
        this.report = report;
        this.dataSource = dataSource;
        this.actions = new ArrayList<Action>();
        if (this.report != null) {
            init();
        }

    }

    private void init() {
        this.report = this.service.loadReportModel(this.report.getId());
        initUI();
        initFiltersPanel();
        initDataView();
    }

    public void reload() {
        getChildren().clear();
        init();
        renderActions();
    }

    public void initUI() {
        setVflex("1");
        layout = new Borderlayout();

        layout.appendChild(new Center());
        layout.appendChild(new South());
        layout.setVflex("1");
        layout.setHflex("1");
        layout.getSouth().setStyle("padding-top: 3px ");
        dataViewContainer = layout.getCenter();

        if (HttpUtils.isSmartphone()) {
            initMobileLayout();
        } else {
            initDesktopLayout();
        }

        appendChild(layout);

        this.buttons = new Hlayout();
        executeButton = new Button(messages.get("execute"));
        executeButton.addEventListener(Events.ON_CLICK, evt -> execute());
        executeButton.setZclass("btn btn-primary");
        executeButton.setIconSclass("fa fa-play");
        buttons.appendChild(executeButton);

        exportButton = new Button(messages.get("exportExcel"));
        exportButton.setIconSclass("fa fa-file-excel-o");
        exportButton.addEventListener(Events.ON_CLICK, evt -> export());
        exportButton.setZclass("btn btn-success");
        buttons.appendChild(exportButton);

        reloadButton = new Button(messages.get("reload"));
        reloadButton.setIconSclass("fa fa-refresh");
        reloadButton.addEventListener(Events.ON_CLICK, evt -> reload());
        reloadButton.setZclass("btn btn-danger");
        buttons.appendChild(reloadButton);
        layout.getSouth().appendChild(buttons);
    }

    public void initMobileLayout() {
        Tabbox content = new Tabbox();
        layout.getCenter().getChildren().clear();
        layout.getCenter().appendChild(content);

        content.appendChild(new Tabs());
        content.appendChild(new Tabpanels());
        content.setVflex("1");
        content.setHflex("1");


        if (report.getFilters() != null && !report.getFilters().isEmpty()) {
            content.getTabs().appendChild(new Tab(messages.get("filters")));
            filtersContainer = new Tabpanel();
            content.getTabpanels().appendChild(filtersContainer);
        }


        content.getTabs().appendChild(new Tab(messages.get("result")));
        dataViewContainer = new Tabpanel();
        content.getTabpanels().appendChild(dataViewContainer);

        if (report.getChartable()) {
            content.getTabs().appendChild(new Tab(messages.get("charts")));
            chartsContainer = new Tabpanel();
            content.getTabpanels().appendChild(chartsContainer);
        }

    }

    private void initDesktopLayout() {
        if (report.getFilters() != null && !report.getFilters().isEmpty()) {

            layout.appendChild(new West());
            layout.getWest().setWidth("20%");
            layout.getWest().setCollapsible(true);
            layout.getWest().setSplittable(true);
            layout.getWest().setTitle(messages.get("filters"));

            filtersContainer = layout.getWest();
        }


        if (report.getChartable()) {

            layout.appendChild(new East());
            layout.getEast().setWidth("40%");
            layout.getEast().setSplittable(true);
            layout.getEast().setAutoscroll(true);

            chartsContainer = layout.getEast();
        }

    }

    public void initFiltersPanel() {
        if (!report.getFilters().isEmpty()) {

            final DefaultViewDescriptor descriptor = new DefaultViewDescriptor();
            report.getFilters().forEach(filter -> {
                Field field = new Field(filter.getName(), filter.getDataType().getTypeClass());
                field.setPropertyInfo(new PropertyInfo(field.getName(), field.getFieldClass(), Report.class, AccessMode.READ_WRITE));
                field.setLabel(filter.getLabel());
                field.setRequired(filter.isRequired());
                if (filter.getHideLabel()) {
                    field.set("showLabel", false);
                }

                field.addParam("condition", FilterCondition.EQUALS.name());

                if (filter.getDataType().equals(DataType.ENUM) && filter.getEnumClassName() != null) {
                    EnumFilterProvider provider = ReportsUtils.findEnumFilterProvider(filter.getEnumClassName());
                    if (provider != null) {
                        try {
                            field.setFieldClass(Class.forName(filter.getEnumClassName()));
                        } catch (ClassNotFoundException e) {

                        }
                        field.setPropertyInfo(new PropertyInfo(field.getName(), field.getFieldClass(), Report.class, AccessMode.READ_WRITE));
                        field.addParam("enumValues", Arrays.asList(provider.getValues()));
                    } else {
                        field.setVisible(false);
                    }


                } else if (filter.getDataType().equals(DataType.ENTITY) && filter.getEntityClassName() != null) {
                    try {
                        field.setFieldClass(Class.forName(filter.getEntityClassName()));
                    } catch (ClassNotFoundException e) {

                    }
                    field.setPropertyInfo(new PropertyInfo(field.getName(), field.getFieldClass(), Report.class, AccessMode.READ_WRITE));
                } else if (filter.getQueryValues() != null && !filter.getQueryValues().isEmpty()) {
                    List<ReportFilterOption> options = filter.loadOptions(dataSource);
                    field.setComponent("combobox");
                    field.addParam("readonly", true);
                    field.addParam("model", new ValueWrapper(new ListModelList(options), ListModel.class));
                    field.addParam("itemRenderer", new ValueWrapper(new ReportFilterOptionItemRenderer(), ComboitemRenderer.class));
                } else if (filter.getDataType().equals(DataType.BOOLEAN)) {
                    field.setComponent("booleanbox");
                } else if (filter.getDataType().equals(DataType.TIME)) {
                    field.setComponent("timebox");
                } else if (filter.getDataType().equals(DataType.DATE_TIME)) {
                    field.addParam("format", "dd/MM/yyyy HH:mm");
                }

                descriptor.addField(field);
            });


            filtersPanel = new EntityFiltersPanel(Report.class);
            filtersPanel.setViewDescriptor(descriptor);
            filtersPanel.addEventListener(EntityFiltersPanel.ON_SEARCH, evt -> execute());
            filtersContainer.appendChild(filtersPanel);
            filtersPanel.getSouth().detach();
        }

    }

    public void initDataView() {
        dataView = new Listbox();
        dataView.setSclass("table-view");
        dataView.appendChild(new Listhead());
        dataView.appendChild(new Listfoot());
        dataView.setVflex("1");
        dataView.setHflex("1");
        dataView.setMold("paging");
        dataView.setSizedByContent(true);

        addColumnNumber();

        if (!report.getAutofields()) {
            report.getFields().stream().sorted(Comparator.comparingInt(ReportField::getOrder))
                    .forEach(f -> {
                        Listheader col = new Listheader(f.getLabel());
                        col.setSortAscending(new FieldComparator(f.getName(), true));
                        col.setSortDescending(new FieldComparator(f.getName(), false));
                        setupColumn(f, col);
                        getDataView().getListhead().appendChild(col);
                        createFooter(f.getName());
                    });
        }

        dataViewContainer.appendChild(dataView);

    }

    private void createFooter(String name) {
        Listfooter footer = new Listfooter();
        footer.getAttributes().put("reportFieldName", name);
        dataView.getListfoot().appendChild(footer);
    }

    public void execute() {
        try {
            this.filters = new ReportFilters();

            if (filtersPanel != null) {
                QueryParameters params = filtersPanel.getQueryParameters();
                validate(params);
                params.forEach((k, v) -> filters.add(getReport().findFilter(k), getFilterValue(v)));
            }


            this.reportData = service.execute(report, filters, dataSource);
            if (reportData.isEmpty()) {
                UIMessages.showMessage(messages.get("noresult"), MessageType.WARNING);
            } else {
                UIMessages.showMessage(getReportData().getSize() + " " + messages.get("results"));
            }

            if (reportData.getSize() > MAX_RESULT_TO_DISPLAY) {
                UIMessages.showQuestion("El resultado de la consulta es muy grande (" + getReportData().getSize() + ") para visualizarse. Desea exportarlo a excel?", this::export);
            } else {
                if (report.getAutofields()) {
                    buildAutoColumns();
                }
                updateDataView();
            }

        } catch (ValidationError e) {
            UIMessages.showMessage(e.getMessage(), MessageType.ERROR);
        } catch (Exception e) {
            if (e.getMessage().contains("interrupted")) {
                Messagebox.show("La consulta demora mucho tiempo en procesarse, por favor utilice otros filtros" + " o intente mas tarde. Por ejemplo, si esta usando un rango de fechas reduzca la diferencia.", "Error al Consultar", Messagebox.OK, Messagebox.ERROR);
            } else {
                Messagebox.show(e.getMessage());
                e.printStackTrace();
            }
        }

    }

    public void validate(final QueryParameters params) {
        List<ReportFilter> requiredFilters = report.getRequiredFilters();
        ReportFilter filter = requiredFilters.stream().filter(it -> !params.containsKey(it.getName())).findFirst().orElse(null);

        if (filter != null) {
            throw new ValidationError(messages.get("errorfiltersRequired", filter.getLabel()));
        }

    }

    public Object getFilterValue(Object filterValue) {
        if (filterValue instanceof QueryCondition condition) {
            if (condition.getValue() instanceof ReportFilterOption opt) {
                return opt.getValue();
            } else if (condition.getValue() instanceof Enum enumeration && report.getQueryLang().equals("sql")) {
                return enumeration.ordinal();
            } else if (condition.getValue() instanceof Identifiable identifiable) {
                return identifiable.getId();
            } else {
                return condition.getValue();
            }
        } else {
            return filterValue;
        }

    }

    public void export() {
        File file = null;
        if (report.getExportWithoutFormat()) {
            file = new ExcelReportDataExporter(report).export(reportData);
        } else {
            file = new ExcelFormattedReportDataExporter(report, filters).export(reportData);
        }

        if (file != null) {
            try {
                Filedownload.save(file, "application/excel");
            } catch (FileNotFoundException e) {
                UIMessages.showMessage("Error al exportar", MessageType.ERROR);
            }
        }

    }

    public void updateDataView() {

        List<String> fieldsNames = report.isAutofields()
                ? reportData.getFieldNames()
                : report.getFields().stream().map(ReportField::getName).toList();

        dataView.getItems().clear();
        Map<String, Object> totals = new HashMap<>();
        int count = 0;

        for (var data : reportData.getEntries()) {
            Listitem row = new Listitem();
            dataView.appendChild(row);

            count++;
            Listcell cellCount = new Listcell(Integer.toString(count));
            cellCount.setSclass("grey lighten-2");
            cellCount.setStyle("font-weight: bold");
            cellCount.setParent(row);

            for (String fieldName : fieldsNames) {
                Object cellData = data.getValues().get(fieldName);

                // Compute Totals
                if (cellData instanceof Number) {
                    Object fieldTotal = totals.get(fieldName);
                    if (fieldTotal == null) {
                        if (cellData instanceof BigDecimal) {
                            fieldTotal = BigDecimal.ZERO;
                        } else if (cellData instanceof Double) {
                            fieldTotal = 0.0;
                        } else {
                            fieldTotal = 0;
                        }
                    }
                    totals.put(fieldName, ((Number) fieldTotal).doubleValue() + ((Number) cellData).doubleValue());
                }

                Listcell cell = new Listcell();
                cell.setAttribute("result", cellData);

                Label cellValue = new Label();

                ReportField reportField = report.findField(fieldName);
                if (reportField != null) {
                    switch (reportField.getDataType()) {
                        case CURRENCY:
                            if (cellData instanceof Number) {
                                cellData = Formatters.formatCurrency((Number) cellData);
                            }
                            break;
                    }
                    cell.setSclass(reportField.getCellStyle());
                    if (reportField.isUpperCase()) {
                        cellData = cellData != null ? cellData.toString().toUpperCase() : null;
                    }
                }

                cellValue.setValue(cellData != null ? cellData.toString() : null);
                cell.appendChild(cellValue);
                row.appendChild(cell);
            }
        }

        // Render totals
        for (Map.Entry<String, Object> entry : totals.entrySet()) {
            Listfooter footer = dataView.getListfoot().getChildren().stream()
                    .filter(child -> entry.getKey().equals(((Listfooter) child).getAttribute("reportFieldName")))
                    .map(child -> (Listfooter) child)
                    .findFirst()
                    .orElse(null);

            if (footer != null) {
                Object footerValue = entry.getValue();
                if (footerValue != null) {
                    ReportField reportField = report.findField(entry.getKey());
                    if (reportField != null) {
                        switch (reportField.getDataType()) {
                            case CURRENCY:
                                footerValue = Formatters.formatCurrency((Number) footerValue);
                                break;
                            case NUMBER:
                                if (footerValue instanceof Double) {
                                    footerValue = Formatters.formatDecimal((Double) footerValue);
                                } else {
                                    footerValue = Formatters.formatInteger((Integer) footerValue);
                                }
                                break;
                        }
                        footer.setStyle(reportField.getCellStyle());
                    }
                    footer.setLabel(footerValue.toString());
                }
            }
        }

        try {
            updateChartView();
        } catch (Exception e) {
            UIMessages.showMessage(messages.get("errorCharting") + ": " + e.getMessage(), MessageType.ERROR);
            if (layout.getEast() != null) {
                layout.getEast().detach();
            }
        }
    }

    public void updateChartView() {
        if (report.isChartable() && report.getCharts() != null && chartsContainer != null) {
            chartsContainer.getChildren().clear();
            currentCharts = new ArrayList<>();

            Vlayout chartLayout = new Vlayout();
            chartsContainer.appendChild(chartLayout);

            for (var c : report.getCharts()) {
                CategoryChartjsData data = new CategoryChartjsData();
                data.getDataset().setColorPalette(ChartjsDashboardWidget.MATERIAL_COLORS);

                if (c.isGrouped()) {
                    Map<String, Number> groups = new HashMap<>();
                    for (var entry : reportData.getEntries()) {
                        String label = entry.getValues().get(c.getLabelField()).toString();
                        Number value = (Number) entry.getValues().get(c.getValueField());
                        if (value == null) {
                            value = 0;
                        }

                        Number sum = groups.get(label);
                        sum = (sum == null) ? value : sum.doubleValue() + value.doubleValue();
                        groups.put(label, sum);
                    }
                    groups.forEach(data::add);
                } else {
                    for (var entry : reportData.getEntries()) {
                        String label = entry.getValues().get(c.getLabelField()).toString();
                        Number value = (Number) entry.getValues().get(c.getValueField());
                        data.add(label, value);
                    }
                }

                Chartjs chart = new Chartjs();
                chart.setType(c.getType());
                chart.setData(data);
                chart.setTitle(c.getTitle());


                currentCharts.add(chart);

                chartLayout.appendChild(chart);
                chartLayout.appendChild(new Hr());
            }
        }
    }

    private void buildAutoColumns() {
        dataView.getListhead().getChildren().clear();
        dataView.getListfoot().getChildren().clear();

        addColumnNumber();

        for (String fieldName : reportData.getFieldNames()) {
            Listheader col = new Listheader(
                    StringUtils.addSpaceBetweenWords(StringUtils.capitalizeAllWords(fieldName))
            );
            col.setAttribute("reportFieldName", fieldName);
            col.setSort("auto");

            ReportField reportField = report.getFields().stream()
                    .filter(field -> field.getName().equals(fieldName))
                    .findFirst()
                    .orElse(null);

            if (reportField != null) {
                setupColumn(reportField, col);
            } else {
                col.addEventListener(Events.ON_SORT, event -> {
                    SortEvent sortEvent = (SortEvent) event;
                    reportData.sort(fieldName, sortEvent.isAscending());
                    updateDataView();
                });
            }

            dataView.getListhead().appendChild(col);
            createFooter(fieldName);
        }
    }

    private void addColumnNumber() {
        Listheader colNum = new Listheader("N.");
        colNum.setSclass("grey color-white");
        dataView.getListhead().appendChild(colNum);

        Listfooter footNum = new Listfooter();
        dataView.getListfoot().appendChild(footNum);
    }

    private void setupColumn(final ReportField reportField, Listheader col) {
        col.getAttributes().put("reportField", reportField);
        col.getAttributes().put("reportFieldName", reportField.getName());
        col.setAlign(reportField.getAlign().name());
        col.setLabel(reportField.getLabel());
        col.setWidth(reportField.getWidth());
        col.setSclass(reportField.getColumnStyle());


        col.addEventListener(Events.ON_SORT, (SortEvent evt) -> {
            getReportData().sort(reportField.getName(), evt.isAscending());
            updateDataView();
        });
    }

    public Button getExecuteButton() {
        return executeButton;
    }

    public Button getExportButton() {
        return exportButton;
    }

    public Button getReloadButton() {
        return reloadButton;
    }

    public Report getReport() {
        return report;
    }

    public List<Chartjs> getCurrentCharts() {
        return currentCharts;
    }

    public ReportData getReportData() {
        return reportData;
    }

    public Borderlayout getLayout() {
        return layout;
    }

    public Listbox getDataView() {
        return dataView;
    }

    public void addAction(Action action) {
        actions.add(action);
    }

    public void renderActions() {
        final ButtonActionRenderer renderer = new ButtonActionRenderer();
        renderer.setZclass("btn btn-default");
        actions.forEach(action -> {
            buttons.appendChild(Actions.render(renderer, action, ReportViewer.this));
        });

    }

    @Override
    public ActionEvent buildActionEvent(Object source, Map<String, Object> params) {
        return new ActionEvent(report, this, params);
    }



}
