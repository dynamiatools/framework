package tools.dynamia.modules.email.api;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.email.EmailMessage;
import tools.dynamia.modules.email.EmailTemplateModelProvider;
import tools.dynamia.modules.email.EmailTemplateRenderer;
import tools.dynamia.modules.email.TemplateVariable;
import tools.dynamia.modules.email.domain.EmailTemplate;
import tools.dynamia.modules.email.domain.EmailTemplateEngine;
import tools.dynamia.modules.email.migration.EmailTemplateMigrator;
import tools.dynamia.modules.email.services.EmailService;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The operations behind the template editor API. Every operation works on a template of the <b>current account only</b>
 * (a template of another account, or of the system account, is "not found"), and request supplied content is only ever
 * rendered by the Dynamia engine, never by Velocity.
 */
@Service
public class EmailTemplatesApiService {

    /** The most characters accepted for a design plus its compiled HTML. */
    public static final int MAX_CHARS = 2_000_000;
    private static final JsonMapper JSON = new JsonMapper();
    static final String EMPTY_DESIGN = "{\"version\":1,\"blocks\":[]}";

    /**
     * A template as the editor sees it.
     *
     * @param id           the template id
     * @param name         the template name
     * @param subject      the subject, may contain variables
     * @param engine       {@code VELOCITY} or {@code DYNAMIA}
     * @param escapeValues whether values are HTML-escaped
     * @param design       the editor document (JSON), null for templates without one
     * @param content      the HTML (compiled from the design for Dynamia templates)
     * @param hasBackup    whether a migration can be reverted
     * @param parentId     the layout template, may be null
     */
    public record TemplateData(Long id, String name, String subject, String engine, boolean escapeValues, String design,
                               String content, boolean hasBackup, Long parentId) {
    }

    /**
     * @param html    the rendered HTML
     * @param subject the rendered subject
     */
    public record Preview(String html, String subject) {
    }

    /**
     * @param sent    whether the email was handed to the mail server
     * @param message the reason when it was not
     */
    public record TestResult(boolean sent, String message) {
    }

    private final CrudService crudService;
    private final AccountServiceAPI accounts;
    private final EmailTemplateMigrator migrator;
    private final EmailService emailService;
    private final EmailTemplateRenderer renderer;

    /**
     * @param crudService  persistence
     * @param accounts     tells the current account
     * @param migrator     the Velocity migrator
     * @param emailService sends the test email and renders stored templates
     * @param renderer     renders request content with the Dynamia engine
     */
    public EmailTemplatesApiService(CrudService crudService, AccountServiceAPI accounts, EmailTemplateMigrator migrator,
                                    EmailService emailService, EmailTemplateRenderer renderer) {
        this.crudService = crudService;
        this.accounts = accounts;
        this.migrator = migrator;
        this.emailService = emailService;
        this.renderer = renderer;
    }

    private EmailTemplate load(Long id) {
        var accountId = accounts.getCurrentAccountId();
        var template = accountId == null || id == null ? null
                : crudService.findSingle(EmailTemplate.class, QueryParameters.with("id", id).add("accountId", accountId));
        if (template == null) {
            throw new EmailApiException(404, "NOT_FOUND", "Template not found");
        }
        return template;
    }

    private static TemplateData data(EmailTemplate t) {
        return new TemplateData(t.getId(), t.getName(), t.getSubject(), t.getEngine().name(), t.isEscapeValues(),
                t.getDesign(), t.getContent(), t.hasBackup(), t.getParent() == null ? null : t.getParent().getId());
    }

    private static void requireDynamia(EmailTemplate t) {
        if (!t.isDynamia()) {
            throw new EmailApiException(409, "NOT_MIGRATED", "This template uses Velocity; migrate it to the editor first");
        }
    }

    /**
     * @param id the template id
     * @return the template of the current account
     */
    @Transactional(readOnly = true)
    public TemplateData get(Long id) {
        return data(load(id));
    }

    /**
     * Creates an empty template for the editor in the current account: Dynamia engine, escaping values.
     *
     * @param name    the template name
     * @param subject the subject, the name when blank
     * @return the new template
     */
    @Transactional
    public TemplateData create(String name, String subject) {
        if (name == null || name.isBlank() || name.length() > 200) {
            throw new EmailApiException(400, "INVALID_REQUEST", "Enter a template name (up to 200 characters)");
        }
        var accountId = accounts.getCurrentAccountId();
        if (accountId == null) {
            throw new EmailApiException(404, "NOT_FOUND", "There is no current account");
        }
        var t = new EmailTemplate();
        t.setAccountId(accountId);
        t.setName(name.trim());
        t.setSubject(subject == null || subject.isBlank() ? name.trim() : subject);
        t.setEngine(EmailTemplateEngine.DYNAMIA);
        t.setEscapeValues(true);
        t.setDesign(EMPTY_DESIGN);
        t.setContent("<html><body></body></html>");
        return data(crudService.create(t));
    }

    /**
     * Saves the document of the editor and the HTML it compiled to. Only templates already on the Dynamia engine can
     * be saved this way.
     *
     * @param id      the template id
     * @param design  the editor document, a JSON object with {@code version} and a {@code blocks} array
     * @param content the compiled HTML
     * @param subject the new subject, kept when null or blank
     * @return the saved template
     */
    @Transactional
    public TemplateData saveDesign(Long id, String design, String content, String subject) {
        var t = load(id);
        requireDynamia(t);
        if (design == null || content == null || content.isBlank()) {
            throw new EmailApiException(400, "INVALID_REQUEST", "The design and the compiled content are required");
        }
        if ((long) design.length() + content.length() > MAX_CHARS) {
            throw new EmailApiException(413, "TOO_LARGE", "The template is too large");
        }
        try {
            var node = JSON.readTree(design);
            if (!node.isObject() || !node.path("version").isNumber() || !node.path("blocks").isArray()) {
                throw new EmailApiException(400, "INVALID_REQUEST", "The design must have a version and a blocks array");
            }
        } catch (tools.jackson.core.JacksonException e) {
            throw new EmailApiException(400, "INVALID_REQUEST", "The design is not valid JSON");
        }
        t.setDesign(design);
        t.setContent(content);
        if (subject != null && !subject.isBlank()) {
            t.setSubject(subject);
        }
        return data(crudService.save(t));
    }

    /**
     * @param source the model source to filter by, null for all
     * @return the variables the providers offer
     */
    public List<TemplateVariable> variables(String source) {
        var all = new ArrayList<TemplateVariable>();
        for (var provider : Containers.get().findObjects(EmailTemplateModelProvider.class)) {
            if (source == null || source.isBlank() || source.equals(provider.getSource())) {
                all.addAll(provider.getVariables());
            }
        }
        return all;
    }

    /**
     * Renders the template with sample or given data, without sending. With request content (an unsaved design) only
     * Dynamia templates are accepted; without it the stored template is rendered with its own engine and its layout.
     *
     * @param id      the template id
     * @param content unsaved HTML to render instead of the stored one, may be null
     * @param subject unsaved subject, may be null
     * @param source  model source for the sample data, may be null
     * @param data    the data, null to use the samples of the variables
     * @return the rendered HTML and subject
     */
    @Transactional(readOnly = true)
    public Preview preview(Long id, String content, String subject, String source, Map<String, Object> data) {
        var t = load(id);
        if ((content != null || subject != null) && !t.isDynamia()) {
            requireDynamia(t);
        }
        if (content != null && content.length() > MAX_CHARS) {
            throw new EmailApiException(413, "TOO_LARGE", "The template is too large");
        }
        var model = data != null ? data : sampleData(variables(source));
        var html = content != null ? renderer.renderHtml(t, content, model) : emailService.render(t, model);
        var renderedSubject = renderer.render(t, subject != null ? subject : t.getSubject(), model);
        return new Preview(html, renderedSubject);
    }

    /**
     * Sends the stored template to one address, with sample or given data. The template's own recipients and SMS are
     * dropped so a test never reaches customers.
     *
     * @param id      the template id
     * @param address where to send it: the address of the user, decided by the caller
     * @param source  model source for the sample data, may be null
     * @param data    the data, null to use the samples of the variables
     * @return whether it was sent
     */
    @Transactional // not read only: sending logs the message and the address
    public TestResult testSend(Long id, String address, String source, Map<String, Object> data) {
        var t = load(id);
        if (address == null || address.isBlank()) {
            throw new EmailApiException(400, "NO_ADDRESS", "Your user has no email address to send the test to");
        }
        var copy = new EmailTemplate();
        copy.setName(t.getName());
        copy.setSubject(t.getSubject());
        copy.setContent(t.getContent());
        copy.setEngine(t.getEngine());
        copy.setEscapeValues(t.isEscapeValues());
        copy.setParent(t.getParent());
        copy.setAccountId(t.getAccountId());
        var message = new EmailMessage(copy);
        message.setTemplateModel(data != null ? data : sampleData(variables(source)));
        message.addTo(address);
        var result = emailService.sendAndWait(message);
        return new TestResult(result.isSended(), result.isSended() ? null : result.getCause());
    }

    /**
     * @param id     the template id
     * @param dryRun true to only analyze
     * @return what the migration does or did; the template is saved only when it was applied
     */
    @Transactional
    public EmailTemplateMigrator.Report migrate(Long id, boolean dryRun) {
        var t = load(id);
        var report = dryRun ? migrator.analyze(t) : migrator.migrate(t);
        if (report.applied()) {
            crudService.save(t);
        }
        return report;
    }

    /**
     * @param id      the template id
     * @param confirm true to accept losing design edits made after the migration
     * @return what happened; the template is saved only when it was reverted
     */
    @Transactional
    public EmailTemplateMigrator.RevertResult revert(Long id, boolean confirm) {
        var t = load(id);
        var result = migrator.revert(t, confirm);
        if (result.reverted()) {
            crudService.save(t);
        }
        return result;
    }

    /**
     * Builds nested sample data from the variable paths: {@code user.name} becomes {@code {user: {name: ...}}}.
     *
     * @param variables the variables
     * @return the data
     */
    @SuppressWarnings("unchecked")
    static Map<String, Object> sampleData(List<TemplateVariable> variables) {
        var root = new LinkedHashMap<String, Object>();
        for (var v : variables) {
            var parts = v.name().split("\\.");
            Map<String, Object> current = root;
            boolean reachable = true;
            for (int i = 0; i < parts.length - 1 && reachable; i++) {
                var next = current.computeIfAbsent(parts[i], k -> new LinkedHashMap<String, Object>());
                if (next instanceof Map<?, ?> m) {
                    current = (Map<String, Object>) m;
                } else {
                    reachable = false;
                }
            }
            if (reachable) {
                current.put(parts[parts.length - 1], v.sample() != null ? v.sample() : "[" + v.name() + "]");
            }
        }
        return root;
    }
}
