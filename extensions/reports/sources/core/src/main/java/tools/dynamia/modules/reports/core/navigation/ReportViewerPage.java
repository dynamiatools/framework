package tools.dynamia.modules.reports.core.navigation;

import tools.dynamia.navigation.Page;

/**
 * Navigation page of the reports viewer. Its type ({@code ReportViewerPage}, the simple class name sent by the
 * navigation API) tells a Vue shell to render its native reports viewer. The page keeps the path of the legacy ZK view
 * so a ZK shell can still open it.
 */
public class ReportViewerPage extends Page {

    public ReportViewerPage(String id, String name, String legacyPath) {
        super(id, name, legacyPath);
    }
}
