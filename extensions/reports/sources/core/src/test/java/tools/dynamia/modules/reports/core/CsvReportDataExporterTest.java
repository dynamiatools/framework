package tools.dynamia.modules.reports.core;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvReportDataExporterTest {

    @Test
    void quotesCellsWithSeparatorsQuotesAndLineBreaks() {
        assertEquals("plain", CsvReportDataExporter.escape("plain"));
        assertEquals("\"a,b\"", CsvReportDataExporter.escape("a,b"));
        assertEquals("\"say \"\"hi\"\"\"", CsvReportDataExporter.escape("say \"hi\""));
        assertEquals("\"line1\nline2\"", CsvReportDataExporter.escape("line1\nline2"));
        assertEquals("", CsvReportDataExporter.escape(null));
    }

    @Test
    void numbersAreWrittenWithoutScientificNotation() {
        assertEquals("1000000000", CsvReportDataExporter.escape(new BigDecimal("1E+9")));
        assertEquals("-5", CsvReportDataExporter.escape(-5));
    }

    @Test
    void spreadsheetFormulasAreDefused() {
        assertEquals("'=SUM(A1:A9)", CsvReportDataExporter.escape("=SUM(A1:A9)"));
        assertEquals("'+57 300", CsvReportDataExporter.escape("+57 300"));
        assertEquals("'@cmd", CsvReportDataExporter.escape("@cmd"));
        assertEquals("'-1+1", CsvReportDataExporter.escape("-1+1"));
    }
}
