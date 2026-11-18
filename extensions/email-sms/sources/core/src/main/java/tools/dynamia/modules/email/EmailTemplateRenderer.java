package tools.dynamia.modules.email;

import org.springframework.stereotype.Component;
import tools.dynamia.modules.email.domain.EmailTemplate;
import tools.dynamia.templates.DynamiaTemplateEngine;
import tools.dynamia.templates.TemplateEngine;

import java.util.Map;

/**
 * Renders the text fields of an {@link EmailTemplate} with the engine the template is written in: Velocity (through the
 * platform {@link TemplateEngine}, what every template did before the block editor) or the safe
 * {@link DynamiaTemplateEngine}. It is the one place that decides, so the email body, the subject, the addresses and
 * the SMS text of a template always use the same language.
 *
 * <pre>{@code
 * String subject = renderer.render(template, template.getSubject(), model);
 * String html = renderer.renderHtml(template, template.getContent(), model);
 * }</pre>
 */
@Component
public class EmailTemplateRenderer {

    private final TemplateEngine velocity;
    private final DynamiaTemplateEngine dynamia = new DynamiaTemplateEngine();
    private final DynamiaTemplateEngine dynamiaEscaping = new DynamiaTemplateEngine(null, true);

    /**
     * @param velocity the platform engine used by Velocity templates
     */
    public EmailTemplateRenderer(TemplateEngine velocity) {
        this.velocity = velocity;
    }

    /**
     * Renders a plain text field (subject, addresses, reply-to, SMS). Values are never HTML-escaped.
     *
     * @param template the template the text belongs to; it decides the engine
     * @param text     the text to render, may be null or blank (returned as it is)
     * @param context  the data for the variables
     * @return the rendered text
     */
    public String render(EmailTemplate template, String text, Map<String, Object> context) {
        return render(template, text, context, false);
    }

    /**
     * Renders an HTML body. Values are HTML-escaped when the template asks for it ({@code escapeValues}), which only
     * templates built in the block editor do; trusted HTML is printed with the {@code raw} filter.
     *
     * @param template the template the HTML belongs to; it decides the engine and the escaping
     * @param html     the HTML to render, may be null or blank (returned as it is)
     * @param context  the data for the variables
     * @return the rendered HTML
     */
    public String renderHtml(EmailTemplate template, String html, Map<String, Object> context) {
        return render(template, html, context, template.isEscapeValues());
    }

    private String render(EmailTemplate template, String text, Map<String, Object> context, boolean escape) {
        if (text == null || text.isBlank()) {
            return text;
        }
        if (template.isDynamia()) {
            var data = context == null ? Map.<String, Object>of() : context;
            return (escape ? dynamiaEscaping : dynamia).evaluate(text, data);
        }
        return velocity.evaluate(text, context);
    }
}
