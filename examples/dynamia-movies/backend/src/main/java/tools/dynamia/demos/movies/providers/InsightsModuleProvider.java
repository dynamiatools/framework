package tools.dynamia.demos.movies.providers;

import tools.dynamia.crud.CrudPage;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.ReportDataSourceConfig;
import tools.dynamia.modules.reports.core.domain.ReportGroup;
import tools.dynamia.navigation.ExternalPage;
import tools.dynamia.navigation.Module;
import tools.dynamia.navigation.ModuleProvider;

/**
 * Dashboard, reports and the reports designer. The Dynamical Vue theme renders {@code CrudPage}s natively and embeds every other page with
 * {@code <dynamia-embed>}. These two are {@link ExternalPage}s that point at a JavaScript module (built from the
 * {@code frontend} project with {@code pnpm build:insights} into {@code static/insights/}): the embed imports it and
 * mounts its custom element in the backoffice page itself, so the login cookies reach {@code /api}.
 * <p>
 * The designer ({@code @dynamia-tools/reports-vue}) lists the report definitions with the platform CRUD, so it needs a
 * {@link CrudPage} of {@link Report}: {@code definitions} is that page. It has to be a visible one: the server resolves the entity of a CrudPage
 * from the navigation tree, which leaves out invisible pages and modules.
 */
@Provider
public class InsightsModuleProvider implements ModuleProvider {

    @Override
    public Module getModule() {
        return new Module("insights", "Insights")
                .icon("chart-line")
                .position(0)
                .addPage(
                        new ExternalPage("dashboard", "Dashboard", "/insights/insights.js?page=dashboard").icon("gauge"),
                        new ExternalPage("reports", "Reports", "/insights/insights.js?page=reports").icon("table"),
                        new ExternalPage("design", "Reports design", "/insights/insights.js?page=design").icon("pen"),
                        new CrudPage("definitions", "Report definitions", Report.class).icon("pen"),
                        new CrudPage("groups", "Report groups", ReportGroup.class),
                        new CrudPage("datasources", "Datasources", ReportDataSourceConfig.class)
                );
    }
}
