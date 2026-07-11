package tools.dynamia.modules.functions.services.impl;

import tools.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import tools.dynamia.commons.DateTimeUtils;
import tools.dynamia.commons.SimpleTemplateEngine;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.AbstractService;
import tools.dynamia.integration.sterotypes.Service;
import tools.dynamia.modules.functions.FunctionExecutionException;
import tools.dynamia.modules.functions.FunctionNotFoundException;
import tools.dynamia.modules.functions.FunctionResult;
import tools.dynamia.modules.functions.domain.DynamiaHttpFunction;
import tools.dynamia.modules.functions.domain.DynamiaHttpFunctionParameter;
import tools.dynamia.modules.functions.domain.enums.FunctionStatus;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Default {@link tools.dynamia.modules.functions.services.DynamiaHttpFunctionsService} implementation.
 * Functions are resolved through {@link tools.dynamia.domain.services.CrudService}, call parameters are
 * validated against their declared definitions, and execution is performed by rendering the function's
 * {@code url}/{@code bodyTemplate}/{@code headers} templates with the call parameters and issuing the
 * configured HTTP request.
 *
 * @author Mario A. Serrano Leones
 */
@Service
public class DynamiaHttpFunctionsServiceImpl extends AbstractService implements tools.dynamia.modules.functions.services.DynamiaHttpFunctionsService {

    private static final String DEFAULT_CONTENT_TYPE = "application/json";

    private final LoggingService logger = new SLF4JLoggingService(getClass());
    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public DynamiaHttpFunction findLatestVersion(String name) {
        checkName(name);
        List<DynamiaHttpFunction> versions = crudService().find(DynamiaHttpFunction.class,
                QueryParameters.with("name", name)
                        .add("status", FunctionStatus.ACTIVE)
                        .orderBy("functionVersion", false));
        return versions.isEmpty() ? null : versions.get(0);
    }

    @Override
    public DynamiaHttpFunction findVersion(String name, Integer version) {
        checkName(name);
        if (version == null) {
            return findLatestVersion(name);
        }
        return crudService().findSingle(DynamiaHttpFunction.class,
                QueryParameters.with("name", name).add("functionVersion", version));
    }

    @Override
    public void validateParameters(DynamiaHttpFunction function, Map<String, Object> params) {
        Map<String, Object> values = params != null ? params : Map.of();
        for (DynamiaHttpFunctionParameter parameter : function.getParameters()) {
            Object value = values.get(parameter.getName());
            if (isBlank(value) && parameter.getDefaultValue() != null) {
                value = parameter.getDefaultValue();
            }
            if (isBlank(value)) {
                if (parameter.isRequired()) {
                    throw new ValidationError("Parameter [%s] is required", parameter.getName());
                }
                continue;
            }
            coerce(parameter, value);
        }
    }

    @Override
    public FunctionResult call(String name, Map<String, Object> params) {
        return call(name, null, params);
    }

    @Override
    public FunctionResult call(String name, Integer version, Map<String, Object> params) {
        DynamiaHttpFunction function = findVersion(name, version);
        if (function == null || !function.isActive()) {
            throw new FunctionNotFoundException(name, version);
        }

        validateParameters(function, params);
        Map<String, Object> vars = mergeWithDefaults(function, params);

        try {
            return execute(function, vars);
        } catch (FunctionExecutionException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error executing function [" + name + "] version [" + function.getFunctionVersion() + "]", e);
            throw new FunctionExecutionException(name, function.getFunctionVersion(), e);
        }
    }

    /**
     * Renders and issues the HTTP request configured in the function definition, converting the response
     * into a {@link FunctionResult}.
     */
    private FunctionResult execute(DynamiaHttpFunction function, Map<String, Object> vars) {
        String url = SimpleTemplateEngine.parse(function.getUrl(), vars);
        var method = org.springframework.http.HttpMethod.valueOf(function.getMethod().name());

        RestClient.RequestBodySpec spec = restClient.method(method).uri(url);
        applyHeaders(spec, function.getHeaders(), vars);

        if (function.getBodyTemplate() != null && !function.getBodyTemplate().isBlank()) {
            String body = SimpleTemplateEngine.parse(function.getBodyTemplate(), vars);
            String contentType = function.getContentType() != null ? function.getContentType() : DEFAULT_CONTENT_TYPE;
            spec.contentType(MediaType.parseMediaType(contentType)).body(body);
        }

        ResponseEntity<byte[]> response = spec.retrieve().toEntity(byte[].class);
        return toFunctionResult(response);
    }

    /**
     * Parses the {@code Header-Name: value} lines from the function's headers template and applies them
     * (after resolving {@code ${param}} placeholders) to the outgoing request.
     */
    private void applyHeaders(RestClient.RequestBodySpec spec, String headersTemplate, Map<String, Object> vars) {
        if (headersTemplate == null || headersTemplate.isBlank()) {
            return;
        }

        for (String line : headersTemplate.split("\\r?\\n")) {
            if (line.isBlank()) {
                continue;
            }
            int separator = line.indexOf(':');
            if (separator <= 0) {
                continue;
            }
            String headerName = line.substring(0, separator).trim();
            String headerValue = SimpleTemplateEngine.parse(line.substring(separator + 1).trim(), vars);
            spec.header(headerName, headerValue);
        }
    }

    /**
     * Converts the raw HTTP response into a {@link FunctionResult}, returning parsed JSON data when the
     * response content type is JSON-compatible, or raw binary data otherwise (images, PDFs, CSV, etc.).
     */
    private FunctionResult toFunctionResult(ResponseEntity<byte[]> response) {
        byte[] body = response.getBody();
        MediaType contentType = response.getHeaders().getContentType();

        if (!response.getStatusCode().is2xxSuccessful()) {
            String message = body != null ? new String(body, StandardCharsets.UTF_8) : response.getStatusCode().toString();
            throw new RuntimeException("Function endpoint returned status " + response.getStatusCode() + ": " + message);
        }

        if (body == null || body.length == 0) {
            return FunctionResult.success(null);
        }

        if (contentType != null && contentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
            try {
                Object data = objectMapper.readValue(body, Object.class);
                return FunctionResult.success(data);
            } catch (Exception e) {
                return FunctionResult.success(new String(body, StandardCharsets.UTF_8));
            }
        }

        if (contentType != null && contentType.getType().equals("text")) {
            return FunctionResult.success(new String(body, StandardCharsets.UTF_8));
        }

        String binaryContentType = contentType != null ? contentType.toString() : "application/octet-stream";
        return FunctionResult.binary(body, binaryContentType);
    }

    /**
     * Builds the variables map used to render templates: declared parameter default values overridden by
     * the values supplied by the caller.
     */
    private Map<String, Object> mergeWithDefaults(DynamiaHttpFunction function, Map<String, Object> params) {
        Map<String, Object> vars = new LinkedHashMap<>();
        for (DynamiaHttpFunctionParameter parameter : function.getParameters()) {
            if (parameter.getDefaultValue() != null) {
                vars.put(parameter.getName(), parameter.getDefaultValue());
            }
        }
        if (params != null) {
            vars.putAll(params);
        }
        return vars;
    }

    private Object coerce(DynamiaHttpFunctionParameter parameter, Object value) {
        try {
            return switch (parameter.getType()) {
                case NUMBER -> value instanceof Number ? value : new BigDecimal(value.toString());
                case BOOLEAN -> value instanceof Boolean ? value : Boolean.parseBoolean(value.toString());
                case DATE -> DateTimeUtils.parse(value.toString(), "yyyy-MM-dd");
                case DATETIME -> DateTimeUtils.parse(value.toString(), "yyyy-MM-dd HH:mm:ss");
                default -> value.toString();
            };
        } catch (Exception e) {
            throw new ValidationError("Parameter [%s] has an invalid value for type %s", parameter.getName(), parameter.getType());
        }
    }

    private boolean isBlank(Object value) {
        return value == null || (value instanceof String s && s.isBlank());
    }

    private void checkName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationError("Function name is required");
        }
    }
}
