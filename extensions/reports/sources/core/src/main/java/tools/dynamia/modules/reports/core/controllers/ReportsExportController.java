package tools.dynamia.modules.reports.core.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.modules.reports.api.ReportDTO;
import tools.dynamia.modules.reports.core.NestedMapReportDataExporter;
import tools.dynamia.modules.reports.core.ReportFilterOption;
import tools.dynamia.modules.reports.core.ReportFilterValues;
import tools.dynamia.modules.reports.core.ReportFilters;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.security.ReportAccess;
import tools.dynamia.modules.reports.core.security.ReportAccessDeniedException;
import tools.dynamia.modules.reports.core.services.ReportsService;
import tools.dynamia.web.navigation.ErrorResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;

/**
 * Original export endpoints: one URL per report ({@code /api/reports/{group}/{endpoint}}) for reports published with
 * {@code exportEndpoint}. Kept for existing integrations; UIs should use {@link ReportsApiController}.
 */
@RestController
@RequestMapping(value = "/api/reports", produces = "application/json")
public class ReportsExportController {

    public static final String DATE_FORMAT = ReportFilterValues.DATE_FORMAT;
    public static final String DATA_TIME_FORMAT = ReportFilterValues.DATE_TIME_FORMAT;
    public static final String TIME_FORMAT = ReportFilterValues.TIME_FORMAT;
    private static final LoggingService LOGGER = LoggingService.get(ReportsExportController.class);
    private final ReportsService reportsService;

    public ReportsExportController(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    @GetMapping(value = "", produces = "application/json")
    public ResponseEntity<List<ReportDTO>> getReports() {
        List<ReportDTO> dtos = ReportAccess.filter(reportsService.findExportableReports(false)).stream().map(Report::toDTO).toList();
        return ResponseEntity.ok(dtos);
    }


    @GetMapping(value = "/{group}/{endpoint}", produces = "application/json")
    public ResponseEntity<?> getReport(@PathVariable("group") String group, @PathVariable("endpoint") String endpoint, HttpServletRequest request) {
        List<ReportFilterOption> options = new ArrayList<>();
        request.getParameterNames().asIterator().forEachRemaining(p -> {
            String value = request.getParameter(p);
            if (value != null && !value.isBlank()) {
                options.add(new ReportFilterOption(p, value));
            }
        });
        ReportFilters filters = new ReportFilters(options);
        return execute(group, endpoint, filters, request);
    }


    @PostMapping(value = "/{group}/{endpoint}", produces = "application/json")
    public ResponseEntity<?> getReport(@PathVariable("group") String group, @PathVariable("endpoint") String endpoint,
                                       @RequestBody(required = false) ReportFilters filters, HttpServletRequest request) {
        return execute(group, endpoint, filters, request);
    }

    private ResponseEntity<?> execute(String group, String endpoint, ReportFilters filters, HttpServletRequest request) {
        try {
            Report report = group != null ? reportsService.findByEndpoint(group, endpoint) : reportsService.findByEndpoint(endpoint);
            if (report == null) {
                return error(HttpStatus.NOT_FOUND, "NOT_FOUND", "Report not found", request);
            }

            if (!report.isActive()) {
                return error(HttpStatus.FORBIDDEN, "REPORT_INACTIVE", "Report [" + report.getName() + "] is not active", request);
            }

            if (!report.getExportEndpoint()) {
                return error(HttpStatus.FORBIDDEN, "REPORT_NOT_EXPORTED", "Report [" + report.getName() + "] is not exported as endpoint", request);
            }

            ReportAccess.check(report);

            var loadedFilters = ReportFilterValues.load(report, rawValues(filters));
            ReportFilterValues.validateRequired(report, loadedFilters);

            var reportData = reportsService.execute(report, loadedFilters);
            var map = new NestedMapReportDataExporter().export(reportData);

            return ResponseEntity.ok(map);
        } catch (ReportAccessDeniedException e) {
            return error(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied to this report", request);
        } catch (ValidationError e) {
            return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", e.getMessage(), request);
        } catch (Exception e) {
            String reference = UUID.randomUUID().toString();
            LOGGER.error("Error executing report endpoint [" + group + "/" + endpoint + "] reference " + reference, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResult(500, "REPORT_ERROR", "The report could not be executed", request.getRequestURI())
                            .addDetail("reference", reference));
        }

    }

    private static ResponseEntity<ErrorResult> error(HttpStatus status, String code, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ErrorResult(status.value(), code, message, request.getRequestURI()));
    }

    private static Map<String, Object> rawValues(ReportFilters requestFilters) {
        Map<String, Object> raw = new LinkedHashMap<>();
        if (requestFilters != null) {
            requestFilters.getOptions().forEach(option -> {
                if (option.getName() != null && option.getValue() != null) {
                    raw.put(option.getName(), option.getValue());
                }
            });
        }
        return raw;
    }
}
