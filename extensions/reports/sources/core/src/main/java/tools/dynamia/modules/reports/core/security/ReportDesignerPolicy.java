package tools.dynamia.modules.reports.core.security;

/**
 * Decides if the current user can use the designer API: query preview, import and export of definitions and datasource
 * tests. These operations run queries typed by the user or move report definitions between systems, so they are
 * closed unless a policy allows them. Applications register their own policies as Spring beans; the user is a designer
 * when <b>any</b> policy allows it. The default {@link RolesReportDesignerPolicy} checks
 * {@code dynamia.reports.designer-roles}.
 */
public interface ReportDesignerPolicy {

    /**
     * @return true if the current user may design reports
     */
    boolean canDesign();
}
