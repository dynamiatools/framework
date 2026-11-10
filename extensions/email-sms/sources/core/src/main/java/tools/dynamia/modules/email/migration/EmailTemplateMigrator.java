package tools.dynamia.modules.email.migration;

import org.springframework.stereotype.Component;
import tools.dynamia.modules.email.domain.EmailTemplate;
import tools.dynamia.modules.email.domain.EmailTemplateEngine;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Moves an {@link EmailTemplate} from Velocity to the Dynamia engine and the block editor, and back.
 * <p>
 * Migrating converts the syntax of every text field ({@link VelocityConverter}) and wraps the HTML in a design made of
 * one HTML block, so nothing about the look of the email changes. It only happens when everything is portable;
 * otherwise the template is left untouched and the {@link Report} says why. Before changing anything it saves a
 * snapshot of the original fields, and {@link #revert} puts them back.
 * <p>
 * The migrator only changes the entity it is given: the caller saves it.
 *
 * <pre>{@code
 * var report = migrator.migrate(template);
 * if (report.applied()) {
 *     crudService.save(template);
 * }
 * }</pre>
 */
@Component
public class EmailTemplateMigrator {

    /** Shown with every report: code that evaluates the content by itself will not understand a migrated template. */
    static final String DIRECT_EVALUATION_WARNING = "Applications that read the template content and evaluate it with Velocity "
            + "themselves, instead of using EmailService.render, will not understand a migrated template.";

    private static final JsonMapper JSON = new JsonMapper();

    private record Field(String name, Function<EmailTemplate, String> getter, BiConsumer<EmailTemplate, String> setter) {
    }

    private static final List<Field> FIELDS = List.of(
            new Field("content", EmailTemplate::getContent, EmailTemplate::setContent),
            new Field("subject", EmailTemplate::getSubject, EmailTemplate::setSubject),
            new Field("to", EmailTemplate::getTo, EmailTemplate::setTo),
            new Field("cc", EmailTemplate::getCc, EmailTemplate::setCc),
            new Field("bcc", EmailTemplate::getBcc, EmailTemplate::setBcc),
            new Field("replyTo", EmailTemplate::getReplyTo, EmailTemplate::setReplyTo),
            new Field("smsNumber", EmailTemplate::getSmsNumber, EmailTemplate::setSmsNumber),
            new Field("smsText", EmailTemplate::getSmsText, EmailTemplate::setSmsText));

    /**
     * What could not be converted in one field.
     *
     * @param field   the template field ({@code content}, {@code subject}, ...)
     * @param kind    a short code such as {@code SET} or {@code METHOD_CALL}
     * @param line    the line inside the field, starting at 1
     * @param snippet the offending text
     * @param reason  why it cannot be converted
     */
    public record FieldIssue(String field, String kind, int line, String snippet, String reason) {
    }

    /**
     * The outcome of {@link #analyze} or {@link #migrate}.
     *
     * @param alreadyMigrated true when the template already uses the Dynamia engine
     * @param portable        true when every field converts without leaving anything in Velocity
     * @param applied         true when {@link #migrate} changed the template
     * @param converted       the converted text of every non empty field; empty when already migrated
     * @param issues          what keeps the template in Velocity
     * @param warnings        things to know even when the migration is possible
     */
    public record Report(boolean alreadyMigrated, boolean portable, boolean applied, Map<String, String> converted,
                         List<FieldIssue> issues, List<String> warnings) {
    }

    /**
     * The outcome of {@link #revert}.
     *
     * @param reverted     true when the template was restored
     * @param designEdited true when the design was edited after the migration (restoring loses those edits)
     * @param reason       why nothing was reverted, null when it was
     */
    public record RevertResult(boolean reverted, boolean designEdited, String reason) {
    }

    /**
     * Looks at what a migration would do without changing the template.
     *
     * @param template the template to analyze
     * @return the report, with {@code applied} false
     */
    public Report analyze(EmailTemplate template) {
        return run(template, false);
    }

    /**
     * Migrates the template when everything is portable: backs up the original fields, converts every text field, sets
     * the engine to Dynamia (without escaping, as Velocity never escaped) and builds the design with one HTML block.
     *
     * @param template the template to change; untouched when it is not portable or already migrated
     * @return the report; {@code applied} tells if the template changed
     */
    public Report migrate(EmailTemplate template) {
        return run(template, true);
    }

    private Report run(EmailTemplate template, boolean apply) {
        if (template.isDynamia()) {
            return new Report(true, false, false, Map.of(), List.of(), List.of());
        }
        var converted = new LinkedHashMap<String, String>();
        var issues = new ArrayList<FieldIssue>();
        var warnings = new ArrayList<String>();
        for (var field : FIELDS) {
            var original = field.getter().apply(template);
            if (original == null || original.isBlank()) {
                continue;
            }
            var result = VelocityConverter.convert(original);
            converted.put(field.name(), result.text());
            result.issues().forEach(i -> issues.add(new FieldIssue(field.name(), i.kind(), i.line(), i.snippet(), i.reason())));
            int literal = original.indexOf("{{");
            if (literal >= 0) {
                // plain text in Velocity, but the new engine would read it as a variable
                int line = 1 + (int) original.substring(0, literal).chars().filter(c -> c == '\n').count();
                issues.add(new FieldIssue(field.name(), "LITERAL_BRACES", line, "{{",
                        "A literal '{{' is plain text in Velocity but the new engine reads it as a variable"));
            }
        }
        warnings.add(DIRECT_EVALUATION_WARNING);
        boolean portable = issues.isEmpty() && converted.containsKey("content");
        if (converted.get("content") == null) {
            warnings.add("The template has no content.");
        }
        if (!portable || !apply) {
            return new Report(false, portable, false, converted, issues, warnings);
        }

        template.setBackupContent(snapshot(template));
        template.setBackupEngine(template.getEngine());
        for (var field : FIELDS) {
            if (converted.containsKey(field.name())) {
                field.setter().accept(template, converted.get(field.name()));
            }
        }
        template.setEngine(EmailTemplateEngine.DYNAMIA);
        template.setEscapeValues(false);
        template.setDesign(htmlBlockDesign(template.getContent()));
        // the design stored in the snapshot is compared at revert time to know if it was edited afterwards
        template.setBackupContent(withMigratedDesign(template.getBackupContent(), template.getDesign()));
        return new Report(false, true, true, converted, issues, warnings);
    }

    /**
     * Puts the template back as it was before {@link #migrate}.
     *
     * @param template  the template to restore
     * @param confirmed true to accept losing the edits made to the design after the migration
     * @return what happened; nothing changes when the template has no backup or the design was edited and the
     * restore is not confirmed
     */
    public RevertResult revert(EmailTemplate template, boolean confirmed) {
        if (!template.hasBackup() || template.getBackupContent() == null) {
            return new RevertResult(false, false, "The template has no backup to restore");
        }
        JsonNode snapshot;
        try {
            snapshot = JSON.readTree(template.getBackupContent());
        } catch (RuntimeException e) {
            return new RevertResult(false, false, "The backup is not readable");
        }
        var migratedDesign = snapshot.path("migratedDesign").asString(null);
        boolean edited = migratedDesign != null && !migratedDesign.equals(template.getDesign());
        if (edited && !confirmed) {
            return new RevertResult(false, true, "The design was edited after the migration; confirm to lose those edits");
        }
        for (var field : FIELDS) {
            var value = snapshot.path("fields").path(field.name());
            field.setter().accept(template, value.isNull() || value.isMissingNode() ? null : value.asString());
        }
        template.setEngine(template.getBackupEngine());
        template.setEscapeValues(snapshot.path("escapeValues").asBoolean(false));
        template.setDesign(snapshot.path("design").asString(null));
        template.setBackupContent(null);
        template.setBackupEngine(null);
        return new RevertResult(true, edited, null);
    }

    private static String snapshot(EmailTemplate template) {
        var root = JSON.createObjectNode();
        var fields = root.putObject("fields");
        for (var field : FIELDS) {
            fields.put(field.name(), field.getter().apply(template));
        }
        root.put("escapeValues", template.isEscapeValues());
        root.put("design", template.getDesign());
        return JSON.writeValueAsString(root);
    }

    private static String withMigratedDesign(String snapshot, String design) {
        var root = (ObjectNode) JSON.readTree(snapshot);
        root.put("migratedDesign", design);
        return JSON.writeValueAsString(root);
    }

    /**
     * The block editor document of a migrated template: one HTML block holding the whole content, so the email looks
     * exactly as before. The editor sends a full HTML document as it is and wraps a fragment.
     */
    static String htmlBlockDesign(String html) {
        var root = JSON.createObjectNode();
        root.put("version", 1);
        var block = root.putArray("blocks").addObject();
        block.put("id", "html-1");
        block.put("type", "html");
        var props = block.putObject("props");
        props.put("html", html);
        props.put("fullDocument", html != null && html.toLowerCase().contains("<html"));
        return JSON.writeValueAsString(root);
    }
}
