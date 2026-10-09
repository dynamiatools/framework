package tools.dynamia.modules.reports.core.api;

import tools.dynamia.modules.reports.core.ReportDataSource;
import tools.dynamia.modules.reports.core.ReportFilters;
import tools.dynamia.modules.reports.core.ReportData;
import tools.dynamia.modules.reports.core.ReportsSettings;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.ReportChart;
import tools.dynamia.modules.reports.core.domain.ReportGroup;
import tools.dynamia.modules.reports.core.services.ReportsService;
import tools.dynamia.modules.reports.core.services.impl.ReportsServiceImpl;
import tools.dynamia.modules.saas.api.AccountServiceAPI;

import javax.sql.DataSource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * An in-memory {@link ReportsService} over a real H2 database, for the API tests.
 */
public final class ApiFixtures {

    private ApiFixtures() {
    }

    public static class InMemoryReports implements ReportsService {
        public final List<Report> reports = new ArrayList<>();
        final DataSource db = tools.dynamia.modules.reports.core.TestDbAccess.salesDatabase();
        final ReportsServiceImpl delegate;
        public final ReportsSettings settings = new ReportsSettings();

        public InMemoryReports() {
            delegate = new ReportsServiceImpl(mock(AccountServiceAPI.class));
            delegate.setSettings(settings);
        }

        public Report add(Long id, String group, String name, String sql) {
            var g = new ReportGroup();
            g.setName(group);
            g.setEndpointName(group.toLowerCase());
            var report = tools.dynamia.modules.reports.core.TestDbAccess.sqlReport(name, sql);
            report.setId(id);
            report.setGroup(g);
            reports.add(report);
            return report;
        }

        public Report chart(Report report, String title, String type, String label, String value, boolean grouped) {
            var chart = new ReportChart();
            chart.setReport(report);
            chart.setTitle(title);
            chart.setType(type);
            chart.setLabelField(label);
            chart.setValueField(value);
            chart.setGrouped(grouped);
            report.getCharts().add(chart);
            report.setChartable(true);
            return report;
        }

        @Override
        public ReportData execute(Report report, ReportFilters filters, ReportDataSource datasource) {
            return delegate.execute(report, filters, datasource);
        }

        @Override
        public ReportData execute(Report report, ReportFilters filters, ReportDataSource datasource, int maxRows) {
            return delegate.execute(report, filters, datasource, maxRows);
        }

        @Override
        public ReportDataSource datasource(Report report) {
            return new ReportDataSource("test", db);
        }

        @Override
        public Report loadReportModel(Long id) {
            return reports.stream().filter(r -> id.equals(r.getId())).findFirst().orElse(null);
        }

        @Override
        public List<Report> findActives() {
            return reports.stream().filter(Report::isActive).toList();
        }

        @Override
        public List<Report> findActivesByGroup(ReportGroup reportGroup) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Report findByEndpoint(String endpoint) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Report findByEndpoint(String group, String endpoint) {
            throw new UnsupportedOperationException();
        }

        public String lastImported;

        @Override
        public File exportReport(Report report) {
            return delegate.exportReport(report);
        }

        @Override
        public Report importReport(File file) {
            try {
                lastImported = java.nio.file.Files.readString(file.toPath());
            } catch (java.io.IOException e) {
                throw new IllegalStateException(e);
            }
            var imported = new Report();
            imported.setId(99L);
            return imported;
        }

        @Override
        public List<Report> findExportableReports(boolean includeSystem) {
            throw new UnsupportedOperationException();
        }
    }
}
