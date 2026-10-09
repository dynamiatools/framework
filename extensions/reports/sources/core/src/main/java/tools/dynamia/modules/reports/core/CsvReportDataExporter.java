package tools.dynamia.modules.reports.core;

import tools.dynamia.modules.reports.api.v2.ReportColumn;
import tools.dynamia.modules.reports.core.api.ReportColumns;
import tools.dynamia.modules.reports.core.api.ReportValues;
import tools.dynamia.modules.reports.core.domain.Report;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Exports a report as UTF-8 CSV (RFC 4180): comma separated, quoted when needed, CRLF line ends, header row with the
 * column labels. Dates are ISO text and numbers are written without formatting.
 */
public class CsvReportDataExporter implements ReportDataExporter<byte[]> {

    private final Report report;

    /**
     * Creates an exporter for the given report.
     *
     * @param report the report that provides the column definitions
     */
    public CsvReportDataExporter(Report report) {
        this.report = report;
    }

    @Override
    public byte[] export(ReportData data) {
        List<ReportColumn> columns = ReportColumns.of(report, data);
        StringBuilder csv = new StringBuilder();
        append(csv, columns.stream().map(ReportColumn::label).toList());
        for (var entry : data.getEntries()) {
            var row = ReportValues.row(entry, data);
            append(csv, columns.stream().map(c -> row.get(c.name())).toList());
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void append(StringBuilder csv, List<?> cells) {
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) {
                csv.append(',');
            }
            csv.append(escape(cells.get(i)));
        }
        csv.append("\r\n");
    }

    static String escape(Object cell) {
        if (cell == null) {
            return "";
        }
        String text = cell instanceof java.math.BigDecimal bd ? bd.toPlainString() : cell.toString();
        // formulas typed by a user must not run when the file is opened in a spreadsheet
        if (!text.isEmpty() && "=+@".indexOf(text.charAt(0)) >= 0 || text.startsWith("-") && !(cell instanceof Number)) {
            text = "'" + text;
        }
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
