package tools.dynamia.modules.reports.core.services;

import org.springframework.transaction.annotation.Transactional;
import tools.dynamia.modules.reports.core.ReportData;
import tools.dynamia.modules.reports.core.ReportDataSource;
import tools.dynamia.modules.reports.core.ReportFilters;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.ReportGroup;

import java.io.File;
import java.util.List;

/**
 * Main service for managing and executing reports in Dynamia Reports.
 * Provides methods to run, import, export, and query reports, as well as access report models and groups.
 * This interface should be implemented by the business layer responsible for report logic.
 */
public interface ReportsService {
    /**
     * Executes a report with the specified filters and data source.
     * @param report Report to execute
     * @param filters Filters applied to the report
     * @param datasource Data source for the report
     * @return Result of the report execution
     */
    ReportData execute(Report report, ReportFilters filters, ReportDataSource datasource);

    /**
     * Loads the report model by its unique identifier.
     * @param id Report identifier
     * @return Instance of the report
     */
    Report loadReportModel(Long id);

    /**
     * Retrieves the list of active reports available in the system.
     * @return List of active reports
     */
    List<Report> findActives();

    /**
     * Retrieves the list of active reports associated with a specific group.
     * @param reportGroup Report group
     * @return List of active reports in the group
     */
    List<Report> findActivesByGroup(ReportGroup reportGroup);

    /**
     * Finds a report by its unique endpoint.
     * @param endpoint Report endpoint
     * @return Found report or null if it does not exist
     */
    Report findByEndpoint(String endpoint);

    /**
     * Finds a report by group and endpoint, using a transaction.
     * @param group Report group
     * @param endpoint Report endpoint
     * @return Found report or null if it does not exist
     */
    @Transactional
    Report findByEndpoint(String group, String endpoint);

    /**
     * Exports the report model to a file.
     * @param report Report to export
     * @return File generated with the report model
     */
    File exportReport(Report report);

    /**
     * Imports a report from a file.
     * @param file File containing the report model
     * @return Imported report
     */
    Report importReport(File file);

    /**
     * Retrieves the list of exportable reports, optionally including system reports.
     * @param includeSystem Indicates whether to include system reports
     * @return List of exportable reports
     */
    List<Report> findExportableReports(boolean includeSystem);
}
