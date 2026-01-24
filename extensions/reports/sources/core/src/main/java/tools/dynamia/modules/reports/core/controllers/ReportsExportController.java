package tools.dynamia.modules.reports.core.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.dynamia.commons.DateTimeUtils;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.modules.reports.api.ReportDTO;
import tools.dynamia.modules.reports.core.NestedMapReportDataExporter;
import tools.dynamia.modules.reports.core.ReportFilterOption;
import tools.dynamia.modules.reports.core.ReportFilters;
import tools.dynamia.modules.reports.core.ReportsUtils;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.ReportFilter;
import tools.dynamia.modules.reports.core.services.ReportsService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/reports", produces = "application/json")
public class ReportsExportController {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATA_TIME_FORMAT = "yyyy-MM-dd hh:mm:ss";
    public static final String TIME_FORMAT = "hh:mm:ss";
    private final ReportsService reportsService;

    public ReportsExportController(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    @GetMapping(value = "", produces = "application/json")
    public ResponseEntity<List<ReportDTO>> getReports() {
        List<ReportDTO> dtos = reportsService.findExportableReports(false).stream().map(Report::toDTO).toList();
        return ResponseEntity.ok(dtos);
    }


    @GetMapping(value = "/{group}/{endpoint}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getReport(@PathVariable("group") String group, @PathVariable("endpoint") String endpoint, HttpServletRequest request) {
        List<ReportFilterOption> options = new ArrayList<>();
        request.getParameterNames().asIterator().forEachRemaining(p -> {
            String value = request.getParameter(p);
            if (value != null && !value.isBlank()) {
                options.add(new ReportFilterOption(p, value));
            }
        });
        ReportFilters filters = new ReportFilters(options);
        return getReport(group, endpoint, filters);
    }


    @PostMapping(value = "/{group}/{endpoint}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getReport(@PathVariable("group") String group, @PathVariable("endpoint") String endpoint, @RequestBody(required = false) ReportFilters filters) {
        try {
            Report report = group != null ? reportsService.findByEndpoint(group, endpoint) : reportsService.findByEndpoint(endpoint);
            if (report == null) {
                return ResponseEntity.notFound().build();
            }

            if (!report.isActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .header("X-Error-Message", "Report [" + report.getName() + "] is not active")
                        .body(Map.of("error", "Report [" + report.getName() + "] is not active", "valid", false));

            }

            if (!report.getExportEndpoint()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .header("X-Error-Message", "Report [" + report.getName() + "] is not exported as endpoint")
                        .body(Map.of("error", "Report [" + report.getName() + "] is not exported as endpoint", "valid", false));
            }


            var loadedFilters = loadFilters(report, filters);
            validateFilters(report, loadedFilters);
            var datasource = ReportsUtils.findDatasource(report);

            var reportData = reportsService.execute(report, loadedFilters, datasource);
            var map = new NestedMapReportDataExporter().export(reportData);

            return ResponseEntity.ok(map);
        } catch (ValidationError e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Message", e.getMessage())
                    .body(Map.of("error", e.getMessage(), "valid", false));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Error-Message", "Endpoint [" + endpoint + "] error: " + e.getMessage())
                    .body(Map.of("error", "Endpoint [" + endpoint + "] error: " + e.getMessage(), "valid", false));
        }

    }

    private void validateFilters(Report report, ReportFilters loadedFilters) {
        List<ReportFilter> requiredFilters = report.getRequiredFilters();
        if (requiredFilters != null && !requiredFilters.isEmpty()) {

            if (loadedFilters.isEmpty()) {
                throw new ValidationError("Filters Required:" + requiredFilters);
            }

            requiredFilters.forEach(f -> {
                if (!loadedFilters.exists(f.getName())) {
                    String format = switch (f.getDataType()) {
                        case BOOLEAN -> "true or false";
                        case CURRENCY, NUMBER -> "a number";
                        case ENUM -> "one of " + Arrays.toString(listEnumValues(f.getEnumClassName()));
                        case ENTITY -> "id";
                        case DATE -> "with format " + DATE_FORMAT;
                        case DATE_TIME -> "with format " + DATA_TIME_FORMAT;
                        case TIME -> " with format " + TIME_FORMAT;
                        case TEXT -> "";
                    };
                    throw new ValidationError("Filter Required [" + f.getName() + "] " + " of type [" + f.getDataType() + "] " + format);
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
            return switch (filter.getDataType()) {
                case BOOLEAN -> "true".equalsIgnoreCase(value.toString());
                case ENUM -> convertToEnum(filter.getEnumClassName(), value);
                case NUMBER, CURRENCY -> new BigDecimal(value.toString());
                case ENTITY -> convertToEntity(filter.getEntityClassName(), value);
                case DATE -> DateTimeUtils.parse(value.toString(), DATE_FORMAT);
                case DATE_TIME -> DateTimeUtils.parse(value.toString(), DATA_TIME_FORMAT);
                case TIME -> DateTimeUtils.parse(value.toString(), TIME_FORMAT);
                case TEXT -> value.toString();
                default -> value;
            };
        } catch (Exception e) {
            return null;
        }
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
