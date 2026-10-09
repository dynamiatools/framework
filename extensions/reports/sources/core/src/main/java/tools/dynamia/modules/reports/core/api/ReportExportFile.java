package tools.dynamia.modules.reports.core.api;

/**
 * A generated export file.
 *
 * @param filename    suggested file name
 * @param contentType MIME type
 * @param content     file bytes
 * @param truncated   true if the report reached the maximum rows and the file does not have all the data
 */
public record ReportExportFile(String filename, String contentType, byte[] content, boolean truncated) {
}
