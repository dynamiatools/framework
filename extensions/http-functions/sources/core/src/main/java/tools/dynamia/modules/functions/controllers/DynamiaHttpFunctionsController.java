package tools.dynamia.modules.functions.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.modules.functions.FunctionExecutionException;
import tools.dynamia.modules.functions.FunctionInactiveException;
import tools.dynamia.modules.functions.FunctionNotFoundException;
import tools.dynamia.modules.functions.FunctionResult;
import tools.dynamia.modules.functions.services.DynamiaHttpFunctionsService;

import java.util.Map;

/**
 * HTTP entry point for {@link tools.dynamia.modules.functions.domain.DynamiaHttpFunction}s. Exposes
 * functions as callable HTTP endpoints under {@code /api/dynamia/fx/{functionName}}, resolving the
 * requested version (via the {@code X-Dynamia-Version} header or the {@code v} query parameter,
 * defaulting to the highest active version) and returning either a structured JSON response or the raw
 * binary payload produced by the function.
 *
 * @author Mario A. Serrano Leones
 */
@RestController
@RequestMapping("/api/dynamia/fx")
public class DynamiaHttpFunctionsController {

    private static final String VERSION_HEADER = "X-Dynamia-Version";

    private final DynamiaHttpFunctionsService functionsService;

    public DynamiaHttpFunctionsController(DynamiaHttpFunctionsService functionsService) {
        this.functionsService = functionsService;
    }

    /**
     * Invokes a function by name, optionally selecting a specific version.
     *
     * @param functionName  the function name, e.g. {@code WhatsApp.sendMessage}
     * @param versionHeader optional version requested via the {@value #VERSION_HEADER} header
     * @param versionParam  optional version requested via the {@code v} query parameter
     * @param request       the call request containing the function parameters
     * @return {@code 200} with the function result, {@code 400} on validation errors, {@code 404} when
     * the function/version is not found, or {@code 500} on execution errors
     */
    @PostMapping(value = "/{functionName}")
    public ResponseEntity<?> call(@PathVariable String functionName,
                                   @RequestHeader(value = VERSION_HEADER, required = false) Integer versionHeader,
                                   @RequestParam(value = "v", required = false) Integer versionParam,
                                   @RequestBody(required = false) FunctionCallRequest request) {

        Integer version = versionHeader != null ? versionHeader : versionParam;
        Map<String, Object> params = request != null ? request.getParams() : Map.of();

        try {
            FunctionResult result = functionsService.call(functionName, version, params);
            return toResponseEntity(result);
        } catch (FunctionNotFoundException | FunctionInactiveException e) {
            return errorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (ValidationError e) {
            return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (FunctionExecutionException e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private ResponseEntity<?> toResponseEntity(FunctionResult result) {
        if (result.isBinary()) {
            MediaType contentType = MediaType.parseMediaType(result.getContentType());
            return ResponseEntity.ok().contentType(contentType).body(result.getBinaryData());
        }
        return ResponseEntity.ok(Map.of("success", true, "data", result.getData() != null ? result.getData() : Map.of()));
    }

    private ResponseEntity<Map<String, Object>> errorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .header("X-Error-Message", message)
                .body(Map.of("success", false, "error", message));
    }

    /**
     * Request payload accepted by the function call endpoint.
     */
    public static class FunctionCallRequest {

        private Map<String, Object> params;

        public Map<String, Object> getParams() {
            return params;
        }

        public void setParams(Map<String, Object> params) {
            this.params = params;
        }
    }
}
