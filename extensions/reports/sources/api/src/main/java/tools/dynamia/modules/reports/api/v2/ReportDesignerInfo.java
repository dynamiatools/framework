package tools.dynamia.modules.reports.api.v2;

/**
 * What the current user can do in the designer.
 *
 * @param allowed      true if the user can use the designer endpoints
 * @param previewLimit maximum rows a preview returns
 */
public record ReportDesignerInfo(boolean allowed, int previewLimit) {
}
