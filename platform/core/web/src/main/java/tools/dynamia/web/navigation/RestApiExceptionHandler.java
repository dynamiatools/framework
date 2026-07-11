/*
 * Copyright (C) 2026 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.web.navigation;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.navigation.NavigationNotAllowedException;
import tools.dynamia.navigation.PageNotFoundException;

/**
 * Centralized exception handler for all REST API endpoints under {@code /api/**}.
 *
 * <p>Intercepts exceptions thrown by {@link RestNavigationController} and translates them
 * into consistent, machine-readable JSON error responses using the {@link ErrorResult} structure.
 * This keeps controller methods free of repetitive try/catch blocks and guarantees a uniform
 * error contract for API clients.</p>
 *
 * <h2>Handled exceptions and HTTP status mapping</h2>
 * <table border="1">
 *   <tr><th>Exception</th><th>HTTP Status</th><th>Reason</th></tr>
 *   <tr><td>{@link PageNotFoundException}</td><td>404 Not Found</td><td>The requested path does not map to any registered {@link tools.dynamia.crud.CrudPage}</td></tr>
 *   <tr><td>{@link NavigationNotAllowedException}</td><td>403 Forbidden</td><td>The current user lacks access to the requested page</td></tr>
 *   <tr><td>{@link ValidationError}</td><td>422 Unprocessable Entity</td><td>The submitted entity failed business / bean-validation rules</td></tr>
 *   <tr><td>{@link IllegalArgumentException}</td><td>400 Bad Request</td><td>A request parameter or body could not be parsed or is semantically invalid</td></tr>
 *   <tr><td>{@link Exception} (catch-all)</td><td>500 Internal Server Error</td><td>Any unexpected server-side failure</td></tr>
 * </table>
 *
 * <p>All responses carry {@code Content-Type: application/json} and a body that conforms to
 * {@link ErrorResult}.</p>
 *
 * @author Mario A. Serrano Leones
 * @see ErrorResult
 * @see RestNavigationController
 */
@RestControllerAdvice(basePackages = "tools.dynamia.web.navigation")
public class RestApiExceptionHandler {

    private static final LoggingService logger = new SLF4JLoggingService(RestApiExceptionHandler.class);

    // -------------------------------------------------------------------------
    // 404 — resource / path not found
    // -------------------------------------------------------------------------

    /**
     * Handles {@link PageNotFoundException}, which is thrown when the request URI does not
     * resolve to any registered {@link tools.dynamia.crud.CrudPage} in the navigation structure.
     *
     * @param ex      the exception carrying the "not found" message
     * @param request the current HTTP request
     * @return a {@code 404 Not Found} JSON response
     */
    @ExceptionHandler(PageNotFoundException.class)
    public ResponseEntity<ErrorResult> handlePageNotFound(PageNotFoundException ex, HttpServletRequest request) {
        logger.warn("Page not found: " + request.getRequestURI() + " - " + ex.getMessage());
        return errorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request);
    }

    // -------------------------------------------------------------------------
    // 403 — access denied
    // -------------------------------------------------------------------------

    /**
     * Handles {@link NavigationNotAllowedException}, thrown when
     * {@link tools.dynamia.navigation.NavigationRestrictions#verifyAccess} denies the caller
     * access to the requested page.
     *
     * @param ex      the access-denied exception
     * @param request the current HTTP request
     * @return a {@code 403 Forbidden} JSON response
     */
    @ExceptionHandler(NavigationNotAllowedException.class)
    public ResponseEntity<ErrorResult> handleAccessDenied(NavigationNotAllowedException ex, HttpServletRequest request) {
        logger.warn("Access denied to: " + request.getRequestURI() + " - " + ex.getMessage());
        return errorResponse(HttpStatus.FORBIDDEN, "ACCESS_DENIED", ex.getMessage(), request);
    }

    // -------------------------------------------------------------------------
    // 422 — validation / business-rule failure
    // -------------------------------------------------------------------------

    /**
     * Handles {@link ValidationError}, thrown by domain validators or bean-validation constraints
     * when the submitted entity does not satisfy business rules.
     *
     * <p>The response body includes the {@code invalidProperty} and {@code invalidValue} fields
     * from the {@link ValidationError} when they are available.</p>
     *
     * @param ex      the validation exception
     * @param request the current HTTP request
     * @return a {@code 422 Unprocessable Entity} JSON response
     */
    @ExceptionHandler(ValidationError.class)
    public ResponseEntity<ErrorResult> handleValidationError(ValidationError ex, HttpServletRequest request) {
        logger.warn("Validation error on: " + request.getRequestURI() + " - " + ex.getMessage());
        ErrorResult error = new ErrorResult(
                422,
                "VALIDATION_ERROR",
                ex.getMessage(),
                request.getRequestURI()
        );
        if (ex.getInvalidProperty() != null) {
            error.addDetail("invalidProperty", ex.getInvalidProperty());
        }
        if (ex.getInvalidValue() != null) {
            error.addDetail("invalidValue", String.valueOf(ex.getInvalidValue()));
        }
        return ResponseEntity
                .status(HttpStatus.valueOf(422))
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }

    // -------------------------------------------------------------------------
    // 400 — bad request / illegal argument
    // -------------------------------------------------------------------------

    /**
     * Handles {@link IllegalArgumentException}, raised when a request parameter or body
     * value cannot be parsed or is semantically invalid (e.g., a non-numeric value for a
     * numeric field).
     *
     * @param ex      the illegal-argument exception
     * @param request the current HTTP request
     * @return a {@code 400 Bad Request} JSON response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResult> handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        logger.warn("Bad request on: " + request.getRequestURI() + " - " + ex.getMessage());
        return errorResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), request);
    }

    // -------------------------------------------------------------------------
    // 500 — catch-all
    // -------------------------------------------------------------------------

    /**
     * Catch-all handler for any unexpected exception not covered by the more specific handlers above.
     * The full stack trace is logged at ERROR level while only a generic message is returned to
     * the client to avoid leaking internal implementation details.
     *
     * @param ex      the unexpected exception
     * @param request the current HTTP request
     * @return a {@code 500 Internal Server Error} JSON response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResult> handleGenericException(Exception ex, HttpServletRequest request) {
        logger.error("Unexpected error on: " + request.getRequestURI(), ex);
        return errorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "An unexpected error occurred. Please contact the system administrator.",
                request
        );
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    /**
     * Builds a {@link ResponseEntity} carrying an {@link ErrorResult} with
     * {@code Content-Type: application/json}.
     *
     * @param status  the HTTP status to use
     * @param code    the machine-readable error code
     * @param message the human-readable error message
     * @param request the current HTTP request (used to populate the {@code path} field)
     * @return the fully constructed error response
     */
    private static ResponseEntity<ErrorResult> errorResponse(HttpStatus status, String code,
                                                              String message, HttpServletRequest request) {
        ErrorResult error = new ErrorResult(status.value(), code, message, request.getRequestURI());
        return ResponseEntity
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }
}



