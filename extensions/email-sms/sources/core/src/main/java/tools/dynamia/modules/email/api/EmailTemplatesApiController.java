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

    /** Header every changing call must carry; a page of another site cannot add it to a form post. */
    public static final String REQUESTED_WITH = "X-Requested-With";

    /**
     * Rejects anonymous users, users no policy accepts (one policy that says yes is enough, so to restrict access leave
     * {@code dynamia.email.editor-roles} empty and provide your own policy), and changing calls without the
     * {@value #REQUESTED_WITH} header. The API uses the session cookie and CSRF protection is off in the framework, so
     * the header is what keeps a cross-site form from migrating or reverting a template.
     */
    private void guard(HttpServletRequest request) {
        if (request.getUserPrincipal() == null) {
            throw new EmailApiException(401, "UNAUTHORIZED", "Authentication required");
        }
        if (policies.get().stream().noneMatch(p -> p.canEdit(request))) {
            throw new EmailApiException(403, "ACCESS_DENIED", "You cannot edit email templates");
        }
        if (!"GET".equalsIgnoreCase(request.getMethod()) && request.getHeader(REQUESTED_WITH) == null) {
            throw new EmailApiException(403, "CSRF_HEADER", "Missing the " + REQUESTED_WITH + " header");
        }
    }

    /** Creates an empty template for the editor in the current account. */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public EmailTemplatesApiService.TemplateData create(@RequestBody CreateRequest body, HttpServletRequest request) {
        guard(request);
        return service.create(body.name(), body.subject());
    }

    /** Reads a template of the current account. */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public EmailTemplatesApiService.TemplateData get(@PathVariable Long id, HttpServletRequest request) {
        guard(request);
        return service.get(id);
    }

    /** Saves the editor document and the HTML it compiled to; only for templates on the Dynamia engine. */
    @PutMapping(value = "/{id}/design", produces = MediaType.APPLICATION_JSON_VALUE)
    public EmailTemplatesApiService.TemplateData saveDesign(@PathVariable Long id, @RequestBody SaveRequest body,
                                                            HttpServletRequest request) {
        guard(request);
        return service.saveDesign(id, body.design(), body.content(), body.subject());
    }

    /** Lists the variables the model providers offer, optionally of one source. */
    @GetMapping(value = "/variables", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TemplateVariable> variables(@RequestParam(required = false) String source, HttpServletRequest request) {
        guard(request);
        return service.variables(source);
    }

    /** Renders a template, or unsaved content, with sample or given data without sending anything. */
    @PostMapping(value = "/{id}/preview", produces = MediaType.APPLICATION_JSON_VALUE)
    public EmailTemplatesApiService.Preview preview(@PathVariable Long id, @RequestBody PreviewRequest body,
                                                    HttpServletRequest request) {
        guard(request);
        return service.preview(id, body.content(), body.subject(), body.source(), body.data());
    }

    /** Sends the stored template to the address of the logged in user only. */
    @PostMapping(value = "/{id}/test-send", produces = MediaType.APPLICATION_JSON_VALUE)
    public EmailTemplatesApiService.TestResult testSend(@PathVariable Long id, @RequestBody TestSendRequest body,
                                                        HttpServletRequest request) {
        guard(request);
        var address = policies.get().stream().map(p -> p.testAddress(request)).filter(a -> a != null && !a.isBlank())
                .findFirst().orElse(null);
        return service.testSend(id, address, body.source(), body.data());
    }

    /** Analyzes (dry run, the default) or applies the migration from Velocity. */
    @PostMapping(value = "/{id}/migrate", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object migrate(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean dryRun,
                          HttpServletRequest request) {
        guard(request);
        return service.migrate(id, dryRun);
    }

    /** Restores a migrated template; losing design edits needs {@code confirm=true}. */
    @PostMapping(value = "/{id}/revert", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object revert(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean confirm,
                         HttpServletRequest request) {
        guard(request);
        return service.revert(id, confirm);
    }

    /** Maps a failure of the service to its HTTP status. */
    @ExceptionHandler(EmailApiException.class)
    public ResponseEntity<ApiError> failed(EmailApiException e) {
        return ResponseEntity.status(e.getStatus()).body(new ApiError(e.getStatus(), e.getCode(), e.getMessage()));
    }

    @ExceptionHandler({org.springframework.http.converter.HttpMessageNotReadableException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
            org.springframework.web.HttpMediaTypeNotSupportedException.class})
    /** Maps an unreadable request to 400. */
    public ResponseEntity<ApiError> invalid(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(400, "INVALID_REQUEST", "The request is not valid"));
    }

    /** Maps anything else to 500 without leaking details, with a reference for the log. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception e, HttpServletRequest request) {
        var reference = UUID.randomUUID().toString();
        LOGGER.error("Error in the email templates API " + request.getRequestURI() + " reference " + reference, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(500, "EMAIL_TEMPLATE_ERROR", "The request failed, reference " + reference));
    }
}
