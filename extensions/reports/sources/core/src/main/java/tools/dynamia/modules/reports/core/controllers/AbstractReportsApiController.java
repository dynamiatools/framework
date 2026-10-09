package tools.dynamia.modules.reports.core.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.modules.reports.core.ReportNotFoundException;
import tools.dynamia.modules.reports.core.security.ReportAccessDeniedException;
import tools.dynamia.web.navigation.ErrorResult;

import java.util.UUID;

/**
 * Authentication check and error mapping shared by the reports UI and designer controllers. Every error uses
 * {@link ErrorResult}; unexpected failures return a generic message plus a reference id that is also logged.
 */
public abstract class AbstractReportsApiController {

    private static final LoggingService LOGGER = LoggingService.get(AbstractReportsApiController.class);

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

    /**
     * @param request the current request
     * @return true if the caller is authenticated
     */
    protected static boolean authenticated(HttpServletRequest request) {
        return request.getUserPrincipal() != null;
    }

    /**
     * @param request the current request
     * @return a 401 response
     */
    protected static ResponseEntity<ErrorResult> unauthorized(HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication required", request);
    }

    /**
     * Builds an error response with the platform error body.
     */
    protected static ResponseEntity<ErrorResult> error(HttpStatus status, String code, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ErrorResult(status.value(), code, message, request.getRequestURI()));
    }
}
