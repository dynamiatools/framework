package tools.dynamia.templates;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replaces variables in text. Supported forms:
 * <ul>
 *   <li>{@code ${name}} and {@code {{name}}}</li>
 *   <li>conditional text: {@code ${paid ? 'PAID' : 'PENDING'}} (the condition is an {@code if} test)</li>
 *   <li>arithmetic: {@code ${d.qty * d.price}}, {@code ${(a + b) / 2}} (a missing amount counts as 0)</li>
 *   <li>property paths: {@code ${d.product.name}} (Map keys and public getters)</li>
 *   <li>filters: {@code ${total|currency:0}}, {@code ${date|date:dd/MM/yyyy}}, {@code ${name|upper|default:-}}</li>
 *   <li>the short form {@code $d.name}, only when {@code d} is a defined variable</li>
 *   <li>blocks: {@code {{#if cond}}...{{else}}...{{/if}}} and {@code {{#each items as item}}...{{/each}}}; inside
 *   {@code each} the {@code loop} variable has {@code index}, {@code number}, {@code count}, {@code first}, {@code last}</li>
 * </ul>
 * Unknown variables are left as they are; null values become empty text.
 */
public class DynamiaTemplateEngine {

    private final Locale defaultLocale;

    /** Uses the JVM default locale for {@code upper}, {@code lower}, {@code number}, {@code currency} and {@code date}. */
    public DynamiaTemplateEngine() {
        this(null);
    }

    /**
     * @param defaultLocale used by the locale-aware filters when the data has no {@code locale}; null means the JVM
     *                      default. A {@code locale} variable (Locale or language tag) always wins.
     */
    public DynamiaTemplateEngine(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    /**
     * One pass over the text, so values inserted by a replacement are never scanned again. A {@code $} right before
     * {@code {{name}}} is a currency symbol, not the start of {@code ${...}}.
     */
    private static final Pattern VARIABLE = Pattern.compile(
            "\\$\\{(?!\\{)([^}]+)}|\\{\\{([^}]+)}}|\\$([A-Za-z_]\\w*(?:\\.[A-Za-z_]\\w*)+)");

    public String evaluate(String template, Map<String, Object> data) {
        if (template == null) {
            return "";
        }
        Map<String, Object> params = data;
        if (defaultLocale != null && !data.containsKey(TemplateExpressions.LOCALE_VAR)) {
            params = new HashMap<>(data);
            params.put(TemplateExpressions.LOCALE_VAR, defaultLocale);
        }
        if (TemplateBlocks.hasBlocks(template)) {
            var nodes = TemplateBlocks.parse(template);
            if (nodes != null) {
                return TemplateBlocks.render(nodes, params, this::substitute);
            }
        }
        return substitute(template, params);
    }

    /** Replaces the variables of a text that has no block tags. */
    private String substitute(String template, Map<String, Object> vars) {
        Matcher m = VARIABLE.matcher(template);
        var sb = new StringBuilder();
        while (m.find()) {
            boolean shortForm = m.group(3) != null;
            var expression = shortForm ? m.group(3) : m.group(1) != null ? m.group(1) : m.group(2);
            if (!shortForm && TemplateExpressions.isTernary(expression)) {
                // ${cond ? 'a' : 'b'}: the || of a condition is not a filter separator
                var chosen = TemplateExpressions.ternary(expression, vars);
                m.appendReplacement(sb, Matcher.quoteReplacement(chosen == null ? "" : String.valueOf(chosen)));
                continue;
            }
            var parts = shortForm ? new String[]{expression} : expression.split("\\|");
            Object value;
            if (!vars.containsKey(parts[0].trim()) && TemplateExpressions.isArithmetic(parts[0])) {
                // ${d.qty * d.price}: a sum, difference, product or quotient of numbers and variables
                value = TemplateExpressions.arithmetic(parts[0], name -> TemplateExpressions.resolve(vars, name));
            } else {
                value = TemplateExpressions.resolve(vars, parts[0]);
            }
            String replacement;
            if (value == TemplateExpressions.UNRESOLVED) {
                replacement = m.group();
            } else {
                if (parts.length > 1) {
                    value = TemplateExpressions.applyFilters(value, java.util.Arrays.copyOfRange(parts, 1, parts.length), vars);
                }
                replacement = value == null ? "" : String.valueOf(value);
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
