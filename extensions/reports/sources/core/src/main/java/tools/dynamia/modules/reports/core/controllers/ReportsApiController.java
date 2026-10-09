package tools.dynamia.modules.reports.core.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.dynamia.modules.reports.api.v2.ReportRunRequest;
import tools.dynamia.modules.reports.core.api.ReportsApiService;

import java.nio.charset.StandardCharsets;

/**
 * REST API to build report UIs: catalog, definition (filters, columns, charts), filter options, execution with paging
 * and sorting, and exports.
 * <pre>
 * GET  /api/reports/v2/catalog
 * GET  /api/reports/v2/{id}
 * GET  /api/reports/v2/{id}/filters/{filter}/options?q=&amp;limit=
 * POST /api/reports/v2/{id}/run
 * POST /api/reports/v2/{id}/export?format=xlsx|csv|pdf
 * </pre>
 * The caller must be authenticated. Only active reports of the current account (and the system account) that pass
 * the report access policies are visible; anything else answers {@code 404}. Errors use {@link ErrorResult}.
 * <p>
 * The older {@code /api/reports/{group}/{endpoint}} endpoints of {@link ReportsExportController} keep working.
 */
@RestController
@RequestMapping(ReportsApiController.PATH)
public class ReportsApiController extends AbstractReportsApiController {

    public static final String PATH = "/api/reports/v2";

    private final ReportsApiService service;

    /**
     * Creates the controller.
     * 
     * @param service the service that implements the operations
     */
    public ReportsApiController(ReportsApiService service) {
        this.service = service;
    }

    /**
     * Lists the reports the user can run, grouped.
     * 
     * @param request the current request
     * @return the catalog, or 401 if the caller is not authenticated
     */
    @GetMapping(value = "/catalog", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> catalog(HttpServletRequest request) {
        return authenticated(request) ? ResponseEntity.ok(service.catalog()) : unauthorized(request);
    }

    /**
     * Describes a report: filters, columns, charts and export formats.
     * 
     * @param id      the report id
     * @param request the current request
     * @return the definition, 401, 403 or 404
     */
    @GetMapping(value = "/{id:\\d+}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> definition(@PathVariable Long id, HttpServletRequest request) {
        return authenticated(request) ? ResponseEntity.ok(service.definition(id)) : unauthorized(request);
    }

    @GetMapping(value = "/{id:\\d+}/filters/{filter}/options", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> options(@PathVariable Long id, @PathVariable String filter,
                                     @RequestParam(required = false) String q,
                                     @RequestParam(required = false) Integer limit, HttpServletRequest request) {
        return authenticated(request) ? ResponseEntity.ok(service.filterOptions(id, filter, q, limit)) : unauthorized(request);
    }

    @PostMapping(value = "/{id:\\d+}/run", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> run(@PathVariable Long id, @RequestBody(required = false) ReportRunRequest body,
                                 HttpServletRequest request) {
        if (!authenticated(request)) {
            return unauthorized(request);
        }
        return ResponseEntity.ok(service.run(id, body != null ? body : ReportRunRequest.ofFilters(null)));
    }

    @PostMapping("/{id:\\d+}/export")
    public ResponseEntity<?> export(@PathVariable Long id, @RequestParam(defaultValue = "xlsx") String format,
                                    @RequestBody(required = false) ReportRunRequest body, HttpServletRequest request) {
        if (!authenticated(request)) {
            return unauthorized(request);
        }
        var file = service.export(id, body != null ? body : ReportRunRequest.ofFilters(null), format);
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(file.contentType()));
        headers.setContentDisposition(ContentDisposition.attachment().filename(file.filename(), StandardCharsets.UTF_8).build());
        headers.setContentLength(file.content().length);
        if (file.truncated()) {
            headers.add("X-Report-Truncated", "true");
            headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "X-Report-Truncated, Content-Disposition");
        }
        return new ResponseEntity<>(file.content(), headers, HttpStatus.OK);
    }
}
