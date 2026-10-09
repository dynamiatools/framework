package tools.dynamia.modules.reports.core.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.modules.reports.api.v2.ReportRunRequest;
import tools.dynamia.modules.reports.core.ReportNotFoundException;
import tools.dynamia.modules.reports.core.api.ReportsApiService;
import tools.dynamia.modules.reports.core.security.ReportAccessDeniedException;
import tools.dynamia.web.navigation.ErrorResult;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

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
public class ReportsApiController {

    public static final String PATH = "/api/reports/v2";
    private static final LoggingService LOGGER = LoggingService.get(ReportsApiController.class);

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

    // ---- errors

    /**
     * Maps a missing report or filter to a 404 response.
     */
    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<ErrorResult> notFound(ReportNotFoundException e, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", e.getMessage(), request);
    }

    /**
     * Maps an access policy denial to a 403 response.
     */
    @ExceptionHandler(ReportAccessDeniedException.class)
    public ResponseEntity<ErrorResult> denied(HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied to this report", request);
    }

    /**
     * Maps invalid filters or sorting to a 400 response that names the problem.
     */
    @ExceptionHandler(ValidationError.class)
    public ResponseEntity<ErrorResult> invalid(ValidationError e, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", e.getMessage(), request);
    }

    /**
     * Maps a malformed request body to a 400 response.
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResult> unreadable(HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "The request body is not valid", request);
    }

    /**
     * Errors Spring MVC already knows how to describe (unsupported media type, missing parameter...) keep their
     * status instead of becoming a 500.
     */
    @ExceptionHandler({org.springframework.web.HttpMediaTypeNotSupportedException.class,
            org.springframework.web.HttpRequestMethodNotSupportedException.class,
            org.springframework.web.bind.MissingServletRequestParameterException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class})
    /**
     * Keeps the status of errors Spring MVC already classifies.
     */
    public ResponseEntity<ErrorResult> framework(Exception e, HttpServletRequest request) {
        HttpStatus status = e instanceof org.springframework.web.ErrorResponse er
                ? HttpStatus.valueOf(er.getStatusCode().value()) : HttpStatus.BAD_REQUEST;
        return error(status, "INVALID_REQUEST", status.getReasonPhrase(), request);
    }

    /**
     * Maps any other failure to a 500 response without details; the cause is logged with a reference id.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResult> failed(Exception e, HttpServletRequest request) {
        String reference = UUID.randomUUID().toString();
        LOGGER.error("Error in reports API " + request.getRequestURI() + " reference " + reference, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResult(500, "REPORT_ERROR", "The report could not be executed", request.getRequestURI())
                        .addDetail("reference", reference));
    }

    private static boolean authenticated(HttpServletRequest request) {
        return request.getUserPrincipal() != null;
    }

    private static ResponseEntity<ErrorResult> unauthorized(HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication required", request);
    }

    private static ResponseEntity<ErrorResult> error(HttpStatus status, String code, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ErrorResult(status.value(), code, message, request.getRequestURI()));
    }
}
