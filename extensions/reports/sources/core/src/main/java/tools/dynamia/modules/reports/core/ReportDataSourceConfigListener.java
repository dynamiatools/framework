package tools.dynamia.modules.reports.core;

import tools.dynamia.domain.util.CrudServiceListenerAdapter;
import tools.dynamia.integration.sterotypes.Listener;
import tools.dynamia.modules.reports.core.domain.ReportDataSourceConfig;

/**
 * Closes the connection pool of a datasource when it is changed or deleted.
 */
@Listener
public class ReportDataSourceConfigListener extends CrudServiceListenerAdapter<ReportDataSourceConfig> {

    @Override
    public void afterUpdate(ReportDataSourceConfig entity) {
        ReportDataSourcePools.evict(entity);
    }

    @Override
    public void afterDelete(ReportDataSourceConfig entity) {
        ReportDataSourcePools.evict(entity);
    }
}
