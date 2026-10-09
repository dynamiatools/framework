package tools.dynamia.modules.reports.core.api;

import tools.dynamia.commons.StringUtils;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.sterotypes.Service;
import tools.dynamia.modules.reports.api.v2.FilterOption;
import tools.dynamia.modules.reports.api.v2.ReportCatalogGroup;
import tools.dynamia.modules.reports.api.v2.ReportDefinition;
import tools.dynamia.modules.reports.api.v2.ReportFilterDefinition;
import tools.dynamia.modules.reports.api.v2.ReportRunRequest;
import tools.dynamia.modules.reports.api.v2.ReportRunResult;
import tools.dynamia.modules.reports.api.v2.ReportSummary;
import tools.dynamia.modules.reports.core.CsvReportDataExporter;
import tools.dynamia.modules.reports.core.ExcelFormattedReportDataExporter;
import tools.dynamia.modules.reports.core.ExcelReportDataExporter;
import tools.dynamia.modules.reports.core.PdfReportDataExporter;
import tools.dynamia.modules.reports.core.ReportData;
import tools.dynamia.modules.reports.core.ReportFilterValues;
import tools.dynamia.modules.reports.core.ReportFilters;
import tools.dynamia.modules.reports.core.ReportNotFoundException;
import tools.dynamia.modules.reports.core.ReportsException;
import tools.dynamia.modules.reports.core.ReportsUtils;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.ReportFilter;
import tools.dynamia.modules.reports.core.domain.enums.DataType;
import tools.dynamia.modules.reports.core.security.ReportAccess;
import tools.dynamia.modules.reports.core.services.ReportsService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * UI-agnostic operations of the reports REST API: catalog, definitions, filter options, execution with paging and
 * sorting, charts and file exports. It has no dependency on any UI technology, so every front end (Vue, ZK, mobile)
 * uses the same behaviour. Every operation applies the account scoping, the report state and the access policies.
 */
@Service
public class ReportsApiService {

    public static final List<String> EXPORT_FORMATS = List.of("xlsx", "csv", "pdf");
    public static final int MAX_OPTIONS = 500;
    public static final int MAX_PAGE_SIZE = 10_000;

    private final ReportsService reportsService;

    public ReportsApiService(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    protected CrudService crudService() {
        return DomainUtils.lookupCrudService();
    }

    // ---- catalog and definition

    /**
     * Active reports the current user can run, grouped and sorted by group name.
     */
    public List<ReportCatalogGroup> catalog() {
        Map<String, List<Report>> groups = new TreeMap<>();
        for (Report report : ReportAccess.filter(reportsService.findActives())) {
            groups.computeIfAbsent(report.getGroup().getName(), k -> new ArrayList<>()).add(report);
        }
        List<ReportCatalogGroup> catalog = new ArrayList<>();
        groups.forEach((name, reports) -> catalog.add(new ReportCatalogGroup(name, reports.get(0).getGroup().getEndpointName(),
                reports.stream().map(this::summary).toList())));
        return catalog;
    }

    public ReportDefinition definition(Long id) {
        Report report = visibleReport(id);
        List<ReportFilterDefinition> filters = report.getFilters().stream()
                .sorted(java.util.Comparator.comparingInt(ReportFilter::getOrder))
                .map(this::filterDefinition).toList();
        return new ReportDefinition(summary(report), report.isAutofields(), ReportColumns.declared(report), filters,
                ReportCharts.definitions(report), EXPORT_FORMATS);
    }

    // ---- filter options

    /**
     * Options of a filter with predefined values.
     *
     * @param q     optional text to search in the labels
     * @param limit maximum options, capped at {@value #MAX_OPTIONS}
     */
    public List<FilterOption> filterOptions(Long id, String filterName, String q, Integer limit) {
        Report report = visibleReport(id);
        ReportFilter filter = report.findFilter(filterName);
        if (filter == null) {
            throw new ReportNotFoundException("Filter not found: " + filterName);
        }
        int max = limit == null || limit <= 0 ? MAX_OPTIONS : Math.min(limit, MAX_OPTIONS);
        String text = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);

        List<FilterOption> options = switch (optionsSource(filter)) {
            case ReportFilterDefinition.STATIC -> staticOptions(filter);
            case ReportFilterDefinition.ENUM, ReportFilterDefinition.QUERY -> loadedOptions(report, filter);
            case ReportFilterDefinition.ENTITY -> entityOptions(filter, text, max);
            default -> List.of();
        };
        return options.stream().filter(o -> text.isEmpty() || o.label().toLowerCase(Locale.ROOT).contains(text))
                .limit(max).toList();
    }

    private List<FilterOption> staticOptions(ReportFilter filter) {
        if (filter.getDataType() == DataType.BOOLEAN && (filter.getValues() == null || filter.getValues().isBlank())) {
            return List.of(new FilterOption(true, "true"), new FilterOption(false, "false"));
        }
        return Arrays.stream(filter.getValues().split(",")).map(String::trim).filter(v -> !v.isEmpty())
                .map(v -> new FilterOption(v, v)).toList();
    }

    private List<FilterOption> loadedOptions(Report report, ReportFilter filter) {
        try {
            return filter.loadOptions(reportsService.datasource(report)).stream()
                    .map(o -> new FilterOption(enumOrSimple(o.getValue()), String.valueOf(o.getName()))).toList();
        } catch (ReportsException e) {
            return List.of();
        }
    }

    private static Object enumOrSimple(Object value) {
        return ReportValues.simplify(value);
    }

    private List<FilterOption> entityOptions(ReportFilter filter, String text, int max) {
        // only entities explicitly published by an EntityFilterProvider can be listed
        if (ReportsUtils.findEntityFilterProvider(filter.getEntityClassName()) == null) {
            return List.of();
        }
        try {
            Class<?> type = Class.forName(filter.getEntityClassName());
            var params = new QueryParameters();
            if (!text.isEmpty()) {
                params.add("name", QueryConditions.like(text));
            }
            params.paginate(max);
            List<?> entities;
            try {
                entities = crudService().find(type, params);
            } catch (RuntimeException e) {
                // the entity has no name property to search: list without searching
                entities = crudService().find(type, new QueryParameters().paginate(max));
            }
            return entities.stream().map(e -> new FilterOption(tools.dynamia.commons.ObjectOperations.invokeGetMethod(e, "id"),
                    String.valueOf(e))).toList();
        } catch (ClassNotFoundException e) {
            return List.of();
        }
    }

    // ---- run

    public ReportRunResult run(Long id, ReportRunRequest request) {
        Report report = visibleReport(id);
        ReportFilters filters = filters(report, request);
        long start = System.currentTimeMillis();
        ReportData data = reportsService.execute(report, filters);
        sort(data, request);
        long duration = System.currentTimeMillis() - start;

        int total = data.getSize();
        int size = request.size() == null || request.size() <= 0 ? total : Math.min(request.size(), MAX_PAGE_SIZE);
        int page = request.page() == null || request.page() < 0 || request.size() == null || request.size() <= 0 ? 0 : request.page();
        int from = (int) Math.min((long) page * size, total);
        int to = Math.min(from + size, total);

        List<Map<String, Object>> rows = new ArrayList<>();
        for (var entry : data.getEntries().subList(from, to)) {
            rows.add(ReportValues.row(entry, data));
        }
        return new ReportRunResult(ReportColumns.of(report, data), rows, total, page, size, data.isTruncated(), duration,
                ReportCharts.build(report, data));
    }

    // ---- export

    public ReportExportFile export(Long id, ReportRunRequest request, String format) {
        String type = format == null ? "xlsx" : format.toLowerCase(Locale.ROOT);
        if (!EXPORT_FORMATS.contains(type)) {
            throw new ValidationError("Unsupported export format [" + format + "]. Use one of " + EXPORT_FORMATS);
        }
        Report report = visibleReport(id);
        ReportFilters filters = filters(report, request);
        ReportData data = reportsService.execute(report, filters);
        sort(data, request);

        String name = StringUtils.simplifiedString(report.getName()) + "-" + LocalDate.now() + "." + type;
        return switch (type) {
            case "csv" -> new ReportExportFile(name, "text/csv; charset=UTF-8", new CsvReportDataExporter(report).export(data), data.isTruncated());
            case "pdf" -> new ReportExportFile(name, "application/pdf", new PdfReportDataExporter(report).export(data), data.isTruncated());
            default -> new ReportExportFile(name, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    excel(report, filters, data), data.isTruncated());
        };
    }

    private static byte[] excel(Report report, ReportFilters filters, ReportData data) {
        File file = report.getExportWithoutFormat()
                ? new ExcelReportDataExporter(report).export(data)
                : new ExcelFormattedReportDataExporter(report, filters).export(data);
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new ReportsException("Error reading the exported file of report " + report.getName(), e);
        } finally {
            file.delete();
        }
    }

    // ---- helpers

    private ReportFilters filters(Report report, ReportRunRequest request) {
        ReportFilters filters = ReportFilterValues.load(report, request.filters());
        ReportFilterValues.validateRequired(report, filters);
        return filters;
    }

    private static void sort(ReportData data, ReportRunRequest request) {
        if (request.sort() == null || request.sort().isBlank()) {
            return;
        }
        if (data.getFieldNames() == null || !data.getFieldNames().contains(request.sort())) {
            throw new ValidationError("Cannot sort by [" + request.sort() + "]: it is not a column of the report");
        }
        data.sort(request.sort(), !"desc".equalsIgnoreCase(request.direction()));
    }

    /**
     * Loads the report only if it is an active report of the current account (or the system account) that the user can
     * access. Otherwise it behaves as if the report did not exist.
     */
    private Report visibleReport(Long id) {
        boolean visible = reportsService.findActives().stream().anyMatch(r -> Objects.equals(r.getId(), id));
        if (!visible) {
            throw ReportNotFoundException.report(id);
        }
        Report report = reportsService.loadReportModel(id);
        if (report == null || !report.isActive()) {
            throw ReportNotFoundException.report(id);
        }
        ReportAccess.check(report);
        return report;
    }

    private ReportSummary summary(Report report) {
        return new ReportSummary(report.getId(), report.getName(), report.getTitle(), report.getSubtitle(),
                report.getDescription(), report.getGroup() != null ? report.getGroup().getName() : null,
                report.isChartable(), !isEmpty(report), report.getFullEndpoint());
    }

    private static boolean isEmpty(Report report) {
        try {
            return report.getFilters() == null || report.getFilters().isEmpty();
        } catch (RuntimeException e) {
            // lazy collection of a report loaded for the catalog: the filters are read from the definition
            return false;
        }
    }

    private ReportFilterDefinition filterDefinition(ReportFilter filter) {
        DataType type = filter.getDataType() == null ? DataType.TEXT : filter.getDataType();
        String format = switch (type) {
            case DATE -> ReportFilterValues.DATE_FORMAT;
            case DATE_TIME -> ReportFilterValues.DATE_TIME_FORMAT;
            case TIME -> ReportFilterValues.TIME_FORMAT;
            default -> null;
        };
        return new ReportFilterDefinition(filter.getName(), filter.getLabel(), type.name(), filter.isRequired(),
                filter.getHideLabel(), filter.getDefaultValue(), filter.getOrder(), optionsSource(filter), format);
    }

    static String optionsSource(ReportFilter filter) {
        if (filter.getDataType() == DataType.ENUM && filter.getEnumClassName() != null && !filter.getEnumClassName().isBlank()) {
            return ReportFilterDefinition.ENUM;
        }
        if (filter.getDataType() == DataType.ENTITY && filter.getEntityClassName() != null && !filter.getEntityClassName().isBlank()) {
            return ReportFilterDefinition.ENTITY;
        }
        if (filter.getQueryValues() != null && !filter.getQueryValues().isBlank()) {
            return ReportFilterDefinition.QUERY;
        }
        if ((filter.getValues() != null && !filter.getValues().isBlank()) || filter.getDataType() == DataType.BOOLEAN) {
            return ReportFilterDefinition.STATIC;
        }
        return ReportFilterDefinition.NONE;
    }
}
