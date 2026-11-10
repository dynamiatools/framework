package tools.dynamia.modules.email.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.email.TemplateVariable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * REST API of the email template editor. Every call needs an authenticated user that an {@link EmailTemplatePolicy}
 * accepts, and works only on templates of the current account.
 * <ul>
 *   <li>{@code POST /api/email/templates} creates an empty template for the editor</li>
 *   <li>{@code GET /api/email/templates/{id}} reads it</li>
 *   <li>{@code PUT /api/email/templates/{id}/design} saves the design and its compiled HTML</li>
 *   <li>{@code GET /api/email/templates/variables?source=} lists the variables of the model providers</li>
 *   <li>{@code POST /api/email/templates/{id}/preview} renders it with sample data</li>
 *   <li>{@code POST /api/email/templates/{id}/test-send} sends it to the user's own address</li>
 *   <li>{@code POST /api/email/templates/{id}/migrate?dryRun=} and {@code .../revert?confirm=} move it from and to Velocity</li>
 * </ul>
 */
@RestController
@RequestMapping(EmailTemplatesApiController.PATH)
public class EmailTemplatesApiController {

    /** Base path of the API. */
    public static final String PATH = "/api/email/templates";
    private static final LoggingService LOGGER = LoggingService.get(EmailTemplatesApiController.class);

    /**
     * @param status  the HTTP status
     * @param code    a short machine readable code
     * @param message what happened
     */
    public record ApiError(int status, String code, String message) {
    }

    /**
     * @param name    the template name
     * @param subject the subject, may be null
     */
    public record CreateRequest(String name, String subject) {
    }

    /**
     * @param design  the editor document (JSON text)
     * @param content the compiled HTML
     * @param subject the new subject, may be null
     */
    public record SaveRequest(String design, String content, String subject) {
    }

    /**
     * @param content unsaved HTML to render, may be null
     * @param subject unsaved subject, may be null
     * @param source  the model source, may be null
     * @param data    the data, null for the samples
     */
    public record PreviewRequest(String content, String subject, String source, Map<String, Object> data) {
    }

    /**
     * @param source the model source, may be null
     * @param data   the data, null for the samples
     */
    public record TestSendRequest(String source, Map<String, Object> data) {
    }

    private final EmailTemplatesApiService service;
    private final Supplier<Collection<EmailTemplatePolicy>> policies;

    /**
     * @param service the operations
     */
    public EmailTemplatesApiController(EmailTemplatesApiService service) {
        this(service, () -> Containers.get().findObjects(EmailTemplatePolicy.class));
    }

    EmailTemplatesApiController(EmailTemplatesApiService service, Supplier<Collection<EmailTemplatePolicy>> policies) {
        this.service = service;
        this.policies = policies;
    }

    private void guard(HttpServletRequest request) {
        if (request.getUserPrincipal() == null) {
            throw new EmailApiException(401, "UNAUTHORIZED", "Authentication required");
        }
        if (policies.get().stream().noneMatch(p -> p.canEdit(request))) {
            throw new EmailApiException(403, "ACCESS_DENIED", "You cannot edit email templates");
        }
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public EmailTemplatesApiService.TemplateData create(@RequestBody CreateRequest body, HttpServletRequest request) {
        guard(request);
        return service.create(body.name(), body.subject());
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public EmailTemplatesApiService.TemplateData get(@PathVariable Long id, HttpServletRequest request) {
        guard(request);
        return service.get(id);
    }

    @PutMapping(value = "/{id}/design", produces = MediaType.APPLICATION_JSON_VALUE)
    public EmailTemplatesApiService.TemplateData saveDesign(@PathVariable Long id, @RequestBody SaveRequest body,
                                                            HttpServletRequest request) {
        guard(request);
        return service.saveDesign(id, body.design(), body.content(), body.subject());
    }

    @GetMapping(value = "/variables", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TemplateVariable> variables(@RequestParam(required = false) String source, HttpServletRequest request) {
        guard(request);
        return service.variables(source);
    }

    @PostMapping(value = "/{id}/preview", produces = MediaType.APPLICATION_JSON_VALUE)
    public EmailTemplatesApiService.Preview preview(@PathVariable Long id, @RequestBody PreviewRequest body,
                                                    HttpServletRequest request) {
        guard(request);
        return service.preview(id, body.content(), body.subject(), body.source(), body.data());
    }

    @PostMapping(value = "/{id}/test-send", produces = MediaType.APPLICATION_JSON_VALUE)
    public EmailTemplatesApiService.TestResult testSend(@PathVariable Long id, @RequestBody TestSendRequest body,
                                                        HttpServletRequest request) {
        guard(request);
        var address = policies.get().stream().map(p -> p.testAddress(request)).filter(a -> a != null && !a.isBlank())
                .findFirst().orElse(null);
        return service.testSend(id, address, body.source(), body.data());
    }

    @PostMapping(value = "/{id}/migrate", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object migrate(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean dryRun,
                          HttpServletRequest request) {
        guard(request);
        return service.migrate(id, dryRun);
    }

    @PostMapping(value = "/{id}/revert", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object revert(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean confirm,
                         HttpServletRequest request) {
        guard(request);
        return service.revert(id, confirm);
    }

    @ExceptionHandler(EmailApiException.class)
    public ResponseEntity<ApiError> failed(EmailApiException e) {
        return ResponseEntity.status(e.getStatus()).body(new ApiError(e.getStatus(), e.getCode(), e.getMessage()));
    }

    @ExceptionHandler({org.springframework.http.converter.HttpMessageNotReadableException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
            org.springframework.web.HttpMediaTypeNotSupportedException.class})
    public ResponseEntity<ApiError> invalid(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(400, "INVALID_REQUEST", "The request is not valid"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception e, HttpServletRequest request) {
        var reference = UUID.randomUUID().toString();
        LOGGER.error("Error in the email templates API " + request.getRequestURI() + " reference " + reference, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(500, "EMAIL_TEMPLATE_ERROR", "The request failed, reference " + reference));
    }
}
