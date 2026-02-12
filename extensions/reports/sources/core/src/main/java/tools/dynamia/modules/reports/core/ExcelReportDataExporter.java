package tools.dynamia.modules.reports.core;

import java.io.File;
import java.util.Comparator;

import tools.dynamia.commons.StringUtils;
import tools.dynamia.reports.ReportExporterException;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.ReportField;
import tools.dynamia.reports.excel.ExcelFileWriter;

public class ExcelReportDataExporter implements ReportDataExporter<File> {

    private Report report;

    public ExcelReportDataExporter() {
    }

    public ExcelReportDataExporter(Report report) {
        this.report = report;
    }

    @Override
    public File export(ReportData reportData) {
        try {
            File file = File.createTempFile(report.getName().replace(" ", "_") + "_", ".xlsx");
            ExcelFileWriter exporter = new ExcelFileWriter(file);
            // columns
            exportColumns(reportData, exporter);
            exportRows(reportData, exporter);
            exporter.write();
            exporter.close();

            return file;
        } catch (Exception e) {
            throw new ReportExporterException("Error exporting report " + report.getName(), e);
        }
    }

    private void exportRows(ReportData reportData, ExcelFileWriter exporter) {
        for (ReportDataEntry data : reportData.getEntries()) {
            exporter.newRow();
            for (String f : reportData.getFieldNames()) {
                ReportField reportField = report.getFields().stream().filter(field -> field.getName().equals(f)).findFirst().orElse(null);
                Object value = data.getValues().get(f);
                exporter.addCell(value);
            }
        }
    }

    private void exportColumns(ReportData reportData, ExcelFileWriter exporter) {
        if (report.isAutofields()) {
            for (String f : reportData.getFieldNames()) {
                ReportField reportField = report.getFields().stream().filter(field -> field.getName().equals(f)).findFirst().orElse(null);
                if (reportField != null) {
                    exporter.addCell(reportField.getLabel());
                } else {
                    exporter.addCell(StringUtils.capitalizeAllWords(StringUtils.addSpaceBetweenWords(f)));
                }
            }
        } else {
            report.getFields().stream().sorted(Comparator.comparingInt(ReportField::getOrder)).forEach(f -> exporter.addCell(f.getLabel()));
        }
    }
}
