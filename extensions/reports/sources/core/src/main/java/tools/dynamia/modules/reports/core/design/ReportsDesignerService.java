package tools.dynamia.modules.reports.core.design;

import tools.dynamia.commons.StringPojoParser;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.sterotypes.Service;
import tools.dynamia.modules.reports.api.v2.DataSourceTestResult;
import tools.dynamia.modules.reports.api.v2.ReportPreviewRequest;
import tools.dynamia.modules.reports.api.v2.ReportPreviewResult;
import tools.dynamia.modules.reports.core.ReportData;
import tools.dynamia.modules.reports.core.ReportDataSource;
import tools.dynamia.modules.reports.core.ReportFilters;
import tools.dynamia.modules.reports.core.ReportNotFoundException;
import tools.dynamia.modules.reports.core.ReportQueryValidator;
import tools.dynamia.modules.reports.core.ReportsException;
import tools.dynamia.modules.reports.core.ReportsSettings;
import tools.dynamia.modules.reports.core.api.ReportValues;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.ReportDataSourceConfig;
import tools.dynamia.modules.reports.core.domain.ReportFilter;
import tools.dynamia.modules.reports.core.security.ReportDesigners;
import tools.dynamia.modules.reports.core.services.ReportsService;
import tools.dynamia.modules.saas.api.AccountServiceAPI;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Operations for the report designer UI: previewing a query that is being written, moving report definitions between
 * systems and testing datasources. Every operation requires the designer permission, see {@code ReportDesigners}.
 * Previews run with the same read-only, validated and bounded execution as saved reports.
 */
@Service
public class ReportsDesignerService {

    /** Largest definition file accepted by {@link #importDefinition(String)}, in characters. */
    public static final int MAX_DEFINITION_SIZE = 1_000_000;

    private final ReportsService reportsService;
    private final ReportsSettings settings;

    /**
     * @param reportsService the reports service used to execute and move definitions
     * @param settings       the module settings
     */
    public ReportsDesignerService(ReportsService reportsService, ReportsSettings settings) {
        this.reportsService = reportsService;
        this.settings = settings;
    }

    protected CrudService crudService() {
        return DomainUtils.lookupCrudService();
    }

    /**
     * Fails unless the current user is a designer.
     *
     * @throws tools.dynamia.modules.reports.core.security.ReportAccessDeniedException if not allowed
     */
    protected void checkDesigner() {
        ReportDesigners.check();
    }

    protected Long currentAccountId() {
        var accounts = Containers.get().findObject(AccountServiceAPI.class);
        return accounts != null ? accounts.getCurrentAccountId() : null;
    }

    /**
     * Runs the query being designed and returns its first rows.
     *
     * @param request the query, language, optional datasource and parameter values
     * @return the first rows, at most {@code dynamia.reports.preview-limit}
     * @throws ValidationError if the query is not a read-only statement
     */
    public ReportPreviewResult preview(ReportPreviewRequest request) {
        checkDesigner();
        String lang = request.queryLang() == null ? "" : request.queryLang().toLowerCase();
        if (!"sql".equals(lang) && !"jpql".equals(lang)) {
            throw new ValidationError("Query language must be sql or jpql");
        }
        ReportQueryValidator.validateQuery(request.queryScript(), lang);

        var report = new Report();
        report.setName("Preview");
        report.setQueryLang(lang);
        report.setQueryScript(request.queryScript());
        report.setAccountId(currentAccountId());

        var filters = new ReportFilters();
        if (request.parameters() != null) {
            request.parameters().forEach((name, value) -> {
                if (value != null) {
                    filters.add(new ReportFilter(name), value);
                }
            });
        }

        ReportDataSource datasource = request.dataSourceId() != null
                ? new ReportDataSource(dataSource(request.dataSourceId()).getName(), dataSource(request.dataSourceId()))
                : reportsService.datasource(report);

        int limit = Math.max(1, settings.getPreviewLimit());
        long start = System.currentTimeMillis();
        ReportData data = reportsService.execute(report, filters, datasource, limit);
        long duration = System.currentTimeMillis() - start;

        List<Map<String, Object>> rows = new ArrayList<>();
        for (var entry : data.getEntries()) {
            rows.add(ReportValues.row(entry, data));
        }
        List<String> columns = data.getFieldNames() != null ? data.getFieldNames() : List.of();
        return new ReportPreviewResult(columns, rows, data.isTruncated(), duration);
    }

    /**
     * @param id a report id
     * @return the definition of the report as JSON, without ids, ready to be imported in another system
     * @throws ReportNotFoundException if the report does not exist
     */
    public String exportDefinition(Long id) {
        checkDesigner();
        Report report = reportsService.loadReportModel(id);
        if (report == null) {
            throw ReportNotFoundException.report(id);
        }
        File file = reportsService.exportReport(report);
        try {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ReportsException("Error reading the exported definition", e);
        } finally {
            file.delete();
        }
    }

    /**
     * Imports a definition. The new report is inactive, has no datasource and is validated like any saved report.
     *
     * @param json the definition JSON
     * @return the id of the new report
     */
    public Long importDefinition(String json) {
        checkDesigner();
        if (json == null || json.isBlank() || json.length() > MAX_DEFINITION_SIZE) {
            throw new ValidationError("The definition is empty or too large");
        }
        try {
            StringPojoParser.createJsonMapper().readTree(json);
        } catch (RuntimeException e) {
            throw new ValidationError("The definition is not valid JSON");
        }
        File file = null;
        try {
            file = File.createTempFile("report-import-", ".json");
            Files.writeString(file.toPath(), json, StandardCharsets.UTF_8);
            return reportsService.importReport(file).getId();
        } catch (IOException e) {
            throw new ReportsException("Error importing the definition", e);
        } finally {
            if (file != null) {
                file.delete();
            }
        }
    }

    /**
     * Tests the connection of a datasource.
     *
     * @param id the datasource id
     * @return whether the connection is valid, with a short message
     */
    public DataSourceTestResult testDataSource(Long id) {
        checkDesigner();
        ReportDataSourceConfig config = dataSource(id);
        try (var connection = ReportDataSource.newConnection(config)) {
            boolean valid = connection.isValid(5);
            return new DataSourceTestResult(valid, valid ? "Connection ok" : "The connection is not valid");
        } catch (ReportsException | ValidationError | java.sql.SQLException e) {
            String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            return new DataSourceTestResult(false, message.length() > 300 ? message.substring(0, 300) : message);
        }
    }

    private ReportDataSourceConfig dataSource(Long id) {
        ReportDataSourceConfig config = crudService().find(ReportDataSourceConfig.class, id);
        if (config == null) {
            throw new ReportNotFoundException("Datasource not found: " + id);
        }
        return config;
    }
}
