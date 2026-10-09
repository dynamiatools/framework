package tools.dynamia.modules.reports.core.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.dynamia.commons.DateTimeUtils;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.modules.reports.api.ReportDTO;
import tools.dynamia.modules.reports.core.NestedMapReportDataExporter;
import tools.dynamia.modules.reports.core.ReportFilterOption;
import tools.dynamia.modules.reports.core.ReportFilters;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.ReportFilter;
import tools.dynamia.modules.reports.core.security.ReportAccess;
import tools.dynamia.modules.reports.core.security.ReportAccessDeniedException;
import tools.dynamia.modules.reports.core.services.ReportsService;
import tools.dynamia.web.navigation.ErrorResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/reports", produces = "application/json")
public class ReportsExportController {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATA_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_FORMAT = "HH:mm:ss";
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

            var loadedFilters = loadFilters(report, filters);
            validateFilters(report, loadedFilters);

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

    private void validateFilters(Report report, ReportFilters loadedFilters) {
        List<ReportFilter> requiredFilters = report.getRequiredFilters();
        if (requiredFilters != null && !requiredFilters.isEmpty()) {

            if (loadedFilters.isEmpty()) {
                throw new ValidationError("Filters Required:" + requiredFilters);
            }

            requiredFilters.forEach(f -> {
                if (!loadedFilters.exists(f.getName())) {
                    throw new ValidationError("Filter Required [" + f.getName() + "] of type [" + f.getDataType() + "] " + expectedFormat(f));
                }
            });
        }
    }

    private ReportFilters loadFilters(Report report, ReportFilters requestFilters) {
        ReportFilters loaded = new ReportFilters();
        if (requestFilters != null) {
            requestFilters.getOptions().forEach(reqOpt -> {
                if (reqOpt.getValue() != null) {
                    report.getFilters().stream().filter(f -> f.getName().equals(reqOpt.getName()))
                            .findFirst().ifPresent(f -> loaded.add(f, convertFilterValue(f, reqOpt.getValue())));
                }
            });
        }
        return loaded;
    }

    private Object convertFilterValue(ReportFilter filter, Object value) {
        try {
            Object converted = switch (filter.getDataType()) {
                case BOOLEAN -> parseBoolean(value.toString());
                case ENUM -> convertToEnum(filter.getEnumClassName(), value);
                case NUMBER, CURRENCY -> new BigDecimal(value.toString());
                case ENTITY -> convertToEntity(filter.getEntityClassName(), value);
                case DATE -> DateTimeUtils.parse(value.toString(), DATE_FORMAT);
                case DATE_TIME -> DateTimeUtils.parse(value.toString(), DATA_TIME_FORMAT);
                case TIME -> DateTimeUtils.parse(value.toString(), TIME_FORMAT);
                case TEXT -> value.toString();
            };
            if (converted == null) {
                throw new IllegalArgumentException("Cannot convert value");
            }
            return converted;
        } catch (Exception e) {
            throw new ValidationError("Invalid value for filter [" + filter.getName() + "] of type [" + filter.getDataType() + "] "
                    + expectedFormat(filter));
        }
    }

    private static Boolean parseBoolean(String value) {
        if ("true".equalsIgnoreCase(value)) {
            return Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(value)) {
            return Boolean.FALSE;
        }
        throw new IllegalArgumentException("Not a boolean");
    }

    private String expectedFormat(ReportFilter f) {
        return switch (f.getDataType()) {
            case BOOLEAN -> "(true or false)";
            case CURRENCY, NUMBER -> "(a number)";
            case ENUM -> "(one of " + Arrays.toString(listEnumValues(f.getEnumClassName())) + ")";
            case ENTITY -> "(id)";
            case DATE -> "(with format " + DATE_FORMAT + ")";
            case DATE_TIME -> "(with format " + DATA_TIME_FORMAT + ")";
            case TIME -> "(with format " + TIME_FORMAT + ")";
            case TEXT -> "";
        };
    }

    private Object convertToEntity(String entityClassName, Object value) throws ClassNotFoundException {
        try {
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            return value;
        }
    }

    private Object convertToEnum(String enumClassName, Object value) throws ClassNotFoundException {
        return Enum.valueOf((Class<Enum>) Class.forName(enumClassName), value.toString());
    }

    private Enum[] listEnumValues(String enumClassName) {
        try {
            return (Enum[]) Class.forName(enumClassName).getEnumConstants();
        } catch (Exception e) {
            return new Enum[]{};
        }

    }
}
