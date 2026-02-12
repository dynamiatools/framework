package tools.dynamia.modules.reports.core;

public interface ReportDataExporter<T> {
    T export(ReportData reportData);
}
