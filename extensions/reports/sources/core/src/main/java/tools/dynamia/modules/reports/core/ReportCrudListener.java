package tools.dynamia.modules.reports.core;

import tools.dynamia.domain.util.CrudServiceListenerAdapter;
import tools.dynamia.integration.sterotypes.Listener;
import tools.dynamia.modules.reports.core.domain.Report;

@Listener
public class ReportCrudListener extends CrudServiceListenerAdapter<Report> {
    @Override
    public void afterCreate(Report entity) {
        Reports.clearCache();
    }

    @Override
    public void afterUpdate(Report entity) {
        Reports.clearCache();
    }

    @Override
    public void afterDelete(Report entity) {
        Reports.clearCache();
    }

}
