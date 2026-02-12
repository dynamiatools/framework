package tools.dynamia.modules.reports.core;

import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import tools.dynamia.commons.DateTimeUtils;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.Containers;
import tools.dynamia.reports.ReportExporterException;
import tools.dynamia.modules.reports.api.ReportGlobalParameterProvider;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.ReportField;
import tools.dynamia.modules.reports.core.domain.ReportFilter;
import tools.dynamia.modules.reports.core.domain.enums.DataType;
import tools.dynamia.modules.reports.core.domain.enums.TextAlign;
import tools.dynamia.templates.SimpleTemplateEngine;
import tools.dynamia.templates.TemplateEngine;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ExcelFormattedReportDataExporter implements ReportDataExporter<File> {

    public static final String DATE_PATTERN = "yyyy-MM-dd";
    private Report report;
    private ReportFilters filters;
    private Map<String, Object> globalParams = new HashMap<>();

    private SXSSFWorkbook workbook;
    private SXSSFSheet sheet;



    public ExcelFormattedReportDataExporter(Report report, ReportFilters filters) {
        this.report = report;
        this.filters = filters;
        Containers.get().findObjects(ReportGlobalParameterProvider.class).forEach(provider -> globalParams.putAll(provider.getParams()));
    }

    public File export(ReportData reportData) {
        try {
            File file = File.createTempFile(report.getName().replace(" ", "_") + "_", ".xlsx");
            this.workbook = new SXSSFWorkbook(200);
            workbook.setCompressTempFiles(true);

            sheet = workbook.createSheet(report.getName());
            sheet.createFreezePane(0, 5);
            exportTitle();
            exportFilters();
            exportColumns(reportData);
            exportRows(reportData);
            workbook.write(new FileOutputStream(file));
            workbook.close();

            return file;
        } catch (Exception e) {
            throw new ReportExporterException("Error exporting report " + report.getName(), e);
        }
    }


    private void exportTitle() {

        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);


        org.apache.poi.ss.usermodel.CellStyle style = workbook.createCellStyle();
        style.setFont(font);

        SXSSFRow row = sheet.createRow(0);
        org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(report.getName().toUpperCase());
        cell.setCellStyle(style);

        row = sheet.createRow(1);
        cell = row.createCell(0);
        cell.setCellType(CellType.STRING);

        String title = report.getTitle();
        if (title == null) {
            title = (String) globalParams.get("DEFAULT_REPORT_TITLE");
        }
        cell.setCellValue(parse(title));
        cell.setCellStyle(style);

        String subtitle = report.getSubtitle();
        if (subtitle == null) {
            subtitle = (String) globalParams.get("DEFAULT_REPORT_SUBTITLE");
        }

        row = sheet.createRow(2);
        cell = row.createCell(0);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(parse(subtitle));
        cell.setCellStyle(style);
    }

    private void addParam(String name, Object value) {
        globalParams.put(name, value);
    }

    private String parse(String s) {
        if (s != null) {
            TemplateEngine engine = Containers.get().findObject(TemplateEngine.class);
            if (engine == null) {
                engine = new SimpleTemplateEngine();
            }
            return engine.evaluate(s, globalParams);
        } else {
            return "";
        }
    }

    private void exportFilters() {

        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);


        org.apache.poi.ss.usermodel.CellStyle style = workbook.createCellStyle();
        style.setFont(font);

        int lastCell = 0;
        SXSSFRow row = sheet.createRow(3);

        for (ReportFilter filter : report.getFilters()) {
            Object value = filters.getValue(filter.getName());
            if (filter != null && value != null) {
                String filterValue = formatFilterValue(filter, value);
                if (filterValue != null) {
                    org.apache.poi.ss.usermodel.Cell label = row.createCell(lastCell++, CellType.STRING);
                    label.setCellValue(filter.getLabel());
                    label.setCellStyle(style);
                    row.createCell(lastCell++, CellType.STRING).setCellValue(filterValue);
                }
            }
        }
    }

    private String formatFilterValue(ReportFilter filter, Object value) {
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Date) {
            return DateTimeUtils.format((Date) value, DATE_PATTERN);
        } else if (value instanceof Number && filter.getDataType() == DataType.ENTITY) {
            return loadEntityFilter(((Number) value).longValue(), filter.getEntityClassName());
        } else if (DomainUtils.isEntity(value) || value instanceof Enum) {
            return value.toString();
        } else if (filter.getDataType() == DataType.ENUM) {
            return value.toString();
        }


        return null;
    }

    private String loadEntityFilter(long id, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return DomainUtils.lookupCrudService().find(clazz, id).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void exportRows(ReportData reportData) {
        int rowNum = 5;

        org.apache.poi.ss.usermodel.CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.setAlignment(HorizontalAlignment.RIGHT);
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat(6)));

        org.apache.poi.ss.usermodel.CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd"));

        org.apache.poi.ss.usermodel.CellStyle centerStyle = workbook.createCellStyle();
        centerStyle.setAlignment(HorizontalAlignment.CENTER);


        for (ReportDataEntry data : reportData.getEntries()) {
            SXSSFRow row = sheet.createRow(rowNum++);
            int colNum = 0;
            for (String f : reportData.getFieldNames()) {
                org.apache.poi.ss.usermodel.Cell cell = row.createCell(colNum++);

                ReportField reportField = report.getFields().stream().filter(field -> field.getName().equals(f)).findFirst().orElse(null);
                Object value = data.getValues().get(f);
                if (value == null) {
                    value = "";
                }

                if (value instanceof Number number) {
                    cell.setCellValue(number.doubleValue());
                } else if (value instanceof Date date) {
                    cell.setCellValue(date);
                    cell.setCellStyle(dateStyle);
                } else {
                    cell.setCellValue(value.toString());
                }

                if (reportField != null && reportField.getDataType() == DataType.CURRENCY) {
                    if (reportField.getFormat() != null) {
                        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat(reportField.getFormat()));
                    }
                    cell.setCellStyle(currencyStyle);
                }

                if (reportField != null && reportField.getAlign() == TextAlign.CENTER) {
                    cell.setCellStyle(centerStyle);
                }

            }
        }
    }

    private void exportColumns(ReportData reportData) {

        SXSSFRow row = sheet.createRow(4);
        if (report.isAutofields()) {
            int column = 0;
            for (String f : reportData.getFieldNames()) {
                ReportField reportField = report.getFields().stream().filter(field -> field.getName().equals(f)).findFirst().orElse(null);

                if (reportField != null) {
                    addColumn(reportField.getLabel(), reportField.getAlign(), column++, row);
                } else {
                    String label = StringUtils.capitalizeAllWords(StringUtils.addSpaceBetweenWords(f));
                    addColumn(label, TextAlign.LEFT, column++, row);
                }
            }
        } else {
            int column = 0;
            for (ReportField f : report.getFields()) {
                addColumn(f.getLabel(), f.getAlign(), column++, row);
            }
        }
    }

    private void addColumn(String title, TextAlign align, int index, SXSSFRow row) {
        org.apache.poi.ss.usermodel.Cell cell = row.createCell(index, CellType.STRING);
        cell.setCellValue(title);

        org.apache.poi.ss.usermodel.CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.valueOf(align.name()));


        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        cell.setCellStyle(style);


    }
}
