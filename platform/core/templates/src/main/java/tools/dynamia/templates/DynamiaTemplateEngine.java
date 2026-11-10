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
    private final boolean escapeHtml;

    /** Uses the JVM default locale for {@code upper}, {@code lower}, {@code number}, {@code currency} and {@code date}. */
    public DynamiaTemplateEngine() {
        this(null);
    }

    /**
     * @param defaultLocale used by the locale-aware filters when the data has no {@code locale}; null means the JVM
     *                      default. A {@code locale} variable (Locale or language tag) always wins.
     */
    public DynamiaTemplateEngine(Locale defaultLocale) {
        this(defaultLocale, false);
    }

    /**
     * @param defaultLocale see {@link #DynamiaTemplateEngine(Locale)}
     * @param escapeHtml    when true every printed value has {@code & < > " '} replaced by HTML entities, so data
     *                      cannot inject markup into an HTML template; a value that is trusted HTML is printed with the
     *                      {@code raw} filter ({@code ${body|raw}}). Text written in the template is never escaped.
     */
    public DynamiaTemplateEngine(Locale defaultLocale, boolean escapeHtml) {
        this.defaultLocale = defaultLocale;
        this.escapeHtml = escapeHtml;
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
                var chosenText = chosen == null ? "" : String.valueOf(chosen);
                m.appendReplacement(sb, Matcher.quoteReplacement(escapeHtml ? escape(chosenText) : chosenText));
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
                if (escapeHtml && !isRaw(parts)) {
                    replacement = escape(replacement);
                }
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /** Whether the filter chain has {@code raw}: the value is trusted HTML and is printed as it is. */
    private static boolean isRaw(String[] parts) {
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].trim().equals("raw")) {
                return true;
            }
        }
        return false;
    }

    /** Replaces {@code & < > " '} by their HTML entities, in one pass. */
    static String escape(String text) {
        StringBuilder out = null;
        for (int i = 0; i < text.length(); i++) {
            var replacement = switch (text.charAt(i)) {
                case '&' -> "&amp;";
                case '<' -> "&lt;";
                case '>' -> "&gt;";
                case '"' -> "&quot;";
                case '\'' -> "&#39;";
                default -> null;
            };
            if (replacement != null && out == null) {
                out = new StringBuilder(text.length() + 16).append(text, 0, i);
            }
            if (out != null) {
                out.append(replacement != null ? replacement : String.valueOf(text.charAt(i)));
            }
        }
        return out == null ? text : out.toString();
    }
}
