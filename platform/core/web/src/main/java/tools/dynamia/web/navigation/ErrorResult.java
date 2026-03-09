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

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Standardized JSON error response body returned by the REST API for all error conditions.
 *
 * <p>Every field in this record is included in the serialized JSON response so that API clients
 * receive a predictable, self-describing error envelope regardless of the failure type.
 * The {@code details} map is omitted when empty to keep simple error responses concise.</p>
 *
 * <h2>JSON example — validation error</h2>
 * <pre>{@code
 * {
 *   "timestamp": "2026-03-08T14:32:01.123Z",
 *   "status": 422,
 *   "error": "VALIDATION_ERROR",
 *   "message": "Name is required",
 *   "path": "/api/users",
 *   "details": {
 *     "invalidProperty": "name",
 *     "invalidValue": "null"
 *   }
 * }
 * }</pre>
 *
 * <h2>JSON example — not found</h2>
 * <pre>{@code
 * {
 *   "timestamp": "2026-03-08T14:32:05.456Z",
 *   "status": 404,
 *   "error": "NOT_FOUND",
 *   "message": "Invalid Path users/unknown",
 *   "path": "/api/users/unknown"
 * }
 * }</pre>
 *
 * <h2>Error codes</h2>
 * <table border="1">
 *   <tr><th>Code</th><th>HTTP Status</th><th>Meaning</th></tr>
 *   <tr><td>{@code NOT_FOUND}</td><td>404</td><td>Navigation path not registered</td></tr>
 *   <tr><td>{@code ACCESS_DENIED}</td><td>403</td><td>User lacks access to the requested page</td></tr>
 *   <tr><td>{@code VALIDATION_ERROR}</td><td>422</td><td>Entity failed business / bean-validation rules</td></tr>
 *   <tr><td>{@code BAD_REQUEST}</td><td>400</td><td>Invalid request parameter or body</td></tr>
 *   <tr><td>{@code INTERNAL_ERROR}</td><td>500</td><td>Unexpected server-side failure</td></tr>
 * </table>
 *
 * @author Mario A. Serrano Leones
 * @see RestApiExceptionHandler
 */
public class ErrorResult {

    /** ISO-8601 timestamp of when the error occurred, set automatically on construction. */
    private final String timestamp;

    /** HTTP status code (e.g. {@code 404}, {@code 422}). */
    private final int status;

    /** Machine-readable error code (e.g. {@code "NOT_FOUND"}, {@code "VALIDATION_ERROR"}). */
    private final String error;

    /** Human-readable description of the error, safe to display to the end user. */
    private final String message;

    /** The request URI that triggered the error. */
    private final String path;

    /**
     * Optional map of additional error details (e.g. the invalid field name and value for
     * validation errors). Omitted from JSON serialization when empty.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final Map<String, String> details = new LinkedHashMap<>();

    /**
     * Constructs a new {@code ErrorResult}.
     *
     * @param status  the HTTP status code
     * @param error   the machine-readable error code
     * @param message the human-readable error message
     * @param path    the request URI that triggered the error
     */
    public ErrorResult(int status, String error, String message, String path) {
        this.timestamp = Instant.now().toString();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    /**
     * Adds an entry to the {@code details} map.
     *
     * @param key   the detail key (e.g. {@code "invalidProperty"})
     * @param value the detail value
     * @return this instance for method chaining
     */
    public ErrorResult addDetail(String key, String value) {
        details.put(key, value);
        return this;
    }

    /**
     * Returns the ISO-8601 timestamp of when the error was created.
     *
     * @return the timestamp string
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the HTTP status code.
     *
     * @return the status code
     */
    public int getStatus() {
        return status;
    }

    /**
     * Returns the machine-readable error code.
     *
     * @return the error code
     */
    public String getError() {
        return error;
    }

    /**
     * Returns the human-readable error message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the request URI that triggered the error.
     *
     * @return the request path
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the optional details map. May be empty but is never {@code null}.
     *
     * @return the details map
     */
    public Map<String, String> getDetails() {
        return details;
    }
}

