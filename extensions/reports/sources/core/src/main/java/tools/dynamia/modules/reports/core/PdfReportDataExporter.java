package tools.dynamia.modules.reports.core;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import tools.dynamia.modules.reports.api.v2.ReportColumn;
import tools.dynamia.modules.reports.core.api.ReportColumns;
import tools.dynamia.modules.reports.core.api.ReportValues;
import tools.dynamia.modules.reports.core.domain.Report;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Exports a report as a PDF table: the title, then a header row and one row per entry. The page is landscape when the
 * report has many columns.
 */
public class PdfReportDataExporter implements ReportDataExporter<byte[]> {

    private final Report report;

    /**
     * Creates an exporter for the given report.
     *
     * @param report the report that provides the title and the column definitions
     */
    public PdfReportDataExporter(Report report) {
        this.report = report;
    }

    @Override
    public byte[] export(ReportData data) {
        List<ReportColumn> columns = ReportColumns.of(report, data);
        var out = new ByteArrayOutputStream();
        var page = columns.size() > 6 ? PageSize.A4.rotate() : PageSize.A4;
        var document = new Document(page, 28, 28, 28, 28);
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            String title = report.getTitle() != null && !report.getTitle().isBlank() ? report.getTitle() : report.getName();
            document.add(new Paragraph(title, titleFont));
            if (report.getSubtitle() != null && !report.getSubtitle().isBlank()) {
                document.add(new Paragraph(report.getSubtitle(), FontFactory.getFont(FontFactory.HELVETICA, 10)));
            }
            document.add(new Paragraph(" "));

            if (!columns.isEmpty()) {
                var table = new PdfPTable(columns.size());
                table.setWidthPercentage(100);
                table.setHeaderRows(1);
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
                Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
                for (ReportColumn column : columns) {
                    var cell = new PdfPCell(new Phrase(column.label(), headerFont));
                    cell.setBackgroundColor(new Color(51, 102, 204));
                    table.addCell(cell);
                }
                for (var entry : data.getEntries()) {
                    var row = ReportValues.row(entry, data);
                    for (ReportColumn column : columns) {
                        Object value = row.get(column.name());
                        String text = value == null ? "" : value instanceof java.math.BigDecimal bd ? bd.toPlainString() : value.toString();
                        var cell = new PdfPCell(new Phrase(column.upperCase() ? text.toUpperCase() : text, cellFont));
                        if ("RIGHT".equals(column.align())) {
                            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        } else if ("CENTER".equals(column.align())) {
                            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        }
                        table.addCell(cell);
                    }
                }
                document.add(table);
            }
            if (data.isTruncated()) {
                document.add(new Paragraph("The result was truncated at " + data.getSize() + " rows.",
                        FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8)));
            }
        } catch (DocumentException e) {
            throw new ReportsException("Error exporting report " + report.getName() + " to PDF", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
        return out.toByteArray();
    }
}
