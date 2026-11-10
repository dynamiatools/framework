package tools.dynamia.modules.email.migration;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts the Velocity syntax of an email template to the syntax of the Dynamia template engine, only when it can do
 * it without changing the meaning:
 * <ul>
 *   <li>{@code $name}, {@code ${name}}, {@code $!name}, {@code $a.b.c} become {@code ${name}}, {@code ${a.b.c}}</li>
 *   <li>{@code #if(cond) #elseif(cond) #else #end} become {@code {{#if cond}} {{else}}{{#if cond}} {{else}} {{/if}}}</li>
 *   <li>{@code #foreach($i in $list) #end} becomes {@code {{#each list as i}} {{/each}}}; {@code $velocityCount} and
 *   {@code $foreach.count, index, first, last, hasNext} become {@code loop.number, index, first, last}</li>
 *   <li>{@code $TEMPLATE_CONTENT} becomes {@code {{ TEMPLATE_CONTENT | raw }}}, the HTML of the child template</li>
 *   <li>{@code ## line} and {@code #* block *#} comments are dropped</li>
 * </ul>
 * Everything else that Velocity can do (set, macros, parse, include, evaluate, method calls, indexes, parentheses in
 * conditions, ranges, escapes) is reported as an {@link Issue} and the text is copied as it was: a template with any
 * issue must stay a Velocity template.
 *
 * <pre>{@code
 * var result = VelocityConverter.convert("Hi $user.name #if($vip)VIP#end");
 * result.text();      // Hi ${user.name} {{#if vip}}VIP{{/if}}
 * result.portable();  // true
 * }</pre>
 */
public final class VelocityConverter {

    /**
     * A construct that cannot be converted.
     *
     * @param kind    a short code, for example {@code SET}, {@code MACRO}, {@code METHOD_CALL}
     * @param line    the line of the text, starting at 1
     * @param snippet the offending text, shortened
     * @param reason  what to do or why it is not portable
     */
    public record Issue(String kind, int line, String snippet, String reason) {
    }

    /**
     * @param text   the converted text; when there are issues the parts that could not be converted are copied as they were
     * @param issues what could not be converted, empty when the text is fully portable
     */
    public record Conversion(String text, List<Issue> issues) {
        /**
         * @return true when nothing had to be left in Velocity
         */
        public boolean portable() {
            return issues.isEmpty();
        }
    }

    private static final Set<String> NEEDS_ARGS = Set.of("if", "elseif", "foreach", "set", "macro", "parse", "include",
            "evaluate", "define");
    private static final Set<String> NO_ARGS = Set.of("else", "end", "break", "stop");
    private static final Pattern IDENT = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private static final Pattern FOREACH = Pattern.compile(
            "^\\s*\\$!?\\{?([A-Za-z_]\\w*)}?\\s+in\\s+\\$!?\\{?([A-Za-z_]\\w*(?:\\.[A-Za-z_]\\w*)*)}?\\s*$");

    private VelocityConverter() {
    }

    /**
     * @param text a Velocity text; null and blank are returned as they are
     * @return the converted text and the issues found
     */
    public static Conversion convert(String text) {
        if (text == null || text.isBlank()) {
            return new Conversion(text, List.of());
        }
        return new Converter(text).run();
    }

    /** An open #if, #foreach or (not convertible, closed by the same #end) #macro or #define. */
    private static final class Frame {
        final boolean each;
        final boolean verbatim;
        int elseifs;

        Frame(boolean each, boolean verbatim) {
            this.each = each;
            this.verbatim = verbatim;
        }
    }

    private static final class Converter {
        private final String s;
        private final int n;
        private int i;
        private final StringBuilder out = new StringBuilder();
        private final List<Issue> issues = new ArrayList<>();
        private final Deque<Frame> stack = new ArrayDeque<>();

        Converter(String s) {
            this.s = s;
            this.n = s.length();
        }

        Conversion run() {
            while (i < n) {
                char c = s.charAt(i);
                if (c == '\\' && i + 1 < n && (s.charAt(i + 1) == '$' || s.charAt(i + 1) == '#')) {
                    issue("ESCAPE", i, 2, "A backslash escape works differently in Velocity; keep this template in Velocity");
                    out.append(c).append(s.charAt(i + 1));
                    i += 2;
                } else if (c == '#' && hash()) {
                    continue;
                } else if (c == '$' && dollar()) {
                    continue;
                } else {
                    out.append(c);
                    i++;
                }
            }
            if (!stack.isEmpty()) {
                issue("UNBALANCED", n - 1, 1, "A #if or #foreach is not closed with #end");
            }
            return new Conversion(out.toString(), List.copyOf(issues));
        }

        private void issue(String kind, int at, int length, String reason) {
            int line = 1;
            for (int k = 0; k < at && k < n; k++) {
                if (s.charAt(k) == '\n') {
                    line++;
                }
            }
            var snippet = s.substring(Math.min(at, n), Math.min(n, at + Math.max(1, length)))
                    .replaceAll("\\s+", " ").trim();
            if (snippet.length() > 60) {
                snippet = snippet.substring(0, 57) + "...";
            }
            issues.add(new Issue(kind, line, snippet, reason));
        }

        // ---------------------------------------------------------- directives

        private boolean hash() {
            if (s.startsWith("##", i)) {
                while (i < n && s.charAt(i) != '\n') {
                    i++;
                }
                return true;
            }
            if (s.startsWith("#*", i)) {
                int end = s.indexOf("*#", i + 2);
                i = end < 0 ? n : end + 2;
                return true;
            }
            int j = i + 1;
            boolean braces = j < n && s.charAt(j) == '{';
            if (braces) {
                j++;
            }
            var m = IDENT.matcher(s);
            m.region(j, n);
            if (!m.lookingAt()) {
                return false;
            }
            var name = m.group();
            j = m.end();
            if (braces) {
                if (j >= n || s.charAt(j) != '}') {
                    return false;
                }
                j++;
            }
            if (NO_ARGS.contains(name)) {
                return noArgsDirective(name, j);
            }
            int k = j;
            if (NEEDS_ARGS.contains(name)) {
                while (k < n && Character.isWhitespace(s.charAt(k))) {
                    k++;
                }
            }
            if (k >= n || s.charAt(k) != '(') {
                return false; // "#fff", "#top": plain text
            }
            int close = matching(k, '(', ')');
            if (close < 0) {
                issue("SYNTAX", i, n - i, "Unclosed directive");
                return false;
            }
            var args = s.substring(k + 1, close);
            if (!NEEDS_ARGS.contains(name)) {
                issue("MACRO", i, close + 1 - i, "A macro call needs Velocity");
                copy(close + 1);
                return true;
            }
            return argsDirective(name, args, close + 1);
        }

        private boolean noArgsDirective(String name, int next) {
            switch (name) {
                case "else" -> {
                    var top = stack.peek();
                    if (top == null || top.each || top.verbatim) {
                        issue("UNBALANCED", i, next - i, "#else without #if");
                        copy(next);
                    } else {
                        out.append("{{else}}");
                        i = next;
                    }
                }
                case "end" -> {
                    var top = stack.poll();
                    if (top == null) {
                        issue("UNBALANCED", i, next - i, "#end without #if or #foreach");
                        copy(next);
                    } else if (top.verbatim) {
                        copy(next); // the end of a macro, which was already reported
                    } else {
                        out.append(top.each ? "{{/each}}" : "{{/if}}".repeat(1 + top.elseifs));
                        i = next;
                    }
                }
                default -> {
                    issue(name.toUpperCase(), i, next - i, "#" + name + " has no equivalent");
                    copy(next);
                }
            }
            return true;
        }

        private boolean argsDirective(String name, String args, int next) {
            switch (name) {
                case "if" -> {
                    var cond = condition(args, i);
                    if (cond == null) {
                        copy(next);
                    } else {
                        out.append("{{#if ").append(cond).append("}}");
                        i = next;
                    }
                    stack.push(new Frame(false, false));
                }
                case "elseif" -> {
                    var top = stack.peek();
                    var cond = condition(args, i);
                    if (top == null || top.each || top.verbatim || cond == null) {
                        if (top == null || top.each || top.verbatim) {
                            issue("UNBALANCED", i, next - i, "#elseif without #if");
                        }
                        copy(next);
                    } else {
                        top.elseifs++;
                        out.append("{{else}}{{#if ").append(cond).append("}}");
                        i = next;
                    }
                }
                case "foreach" -> {
                    var m = FOREACH.matcher(args);
                    if (m.matches()) {
                        var list = path(m.group(2), true);
                        if (list == null) {
                            copy(next);
                        } else {
                            out.append("{{#each ").append(list).append(" as ").append(m.group(1)).append("}}");
                            i = next;
                        }
                    } else {
                        issue("FOREACH", i, next - i, "Only '#foreach($item in $list)' can be converted (no ranges or method calls)");
                        copy(next);
                    }
                    stack.push(new Frame(true, false));
                }
                default -> {
                    issue(name.toUpperCase(), i, next - i, "#" + name + " has no equivalent");
                    copy(next);
                    if (name.equals("macro") || name.equals("define")) {
                        stack.push(new Frame(false, true));
                    }
                }
            }
            return true;
        }

        private void copy(int to) {
            out.append(s, i, to);
            i = to;
        }

        /** The index of the bracket that closes the one at {@code open}, skipping quoted text, or -1. */
        private int matching(int open, char up, char down) {
            int depth = 0;
            char quote = 0;
            for (int k = open; k < n; k++) {
                char ch = s.charAt(k);
                if (quote != 0) {
                    if (ch == quote) {
                        quote = 0;
                    }
                } else if (ch == '\'' || ch == '"') {
                    quote = ch;
                } else if (ch == up) {
                    depth++;
                } else if (ch == down && --depth == 0) {
                    return k;
                }
            }
            return -1;
        }

        // ---------------------------------------------------------- references

        /**
         * Maps a dotted path to the new engine, or returns null (and records an issue) when it has no equivalent.
         *
         * @param inText true for a printed value, false inside a condition
         */
        private String path(String dotted, boolean inText) {
            if (dotted.equals("velocityCount")) {
                return "loop.number";
            }
            if (dotted.startsWith("foreach.")) {
                return switch (dotted.substring(8)) {
                    case "count" -> "loop.number";
                    case "index" -> "loop.index";
                    case "first" -> "loop.first";
                    case "last" -> "loop.last";
                    default -> {
                        issue("FOREACH", i, dotted.length() + 1, "$" + dotted + " has no equivalent");
                        yield null;
                    }
                };
            }
            return dotted;
        }

        private boolean dollar() {
            int j = i + 1;
            if (j < n && s.charAt(j) == '!') {
                j++;
            }
            boolean formal = j < n && s.charAt(j) == '{';
            if (formal) {
                j++;
            }
            var m = IDENT.matcher(s);
            m.region(j, n);
            if (!m.lookingAt()) {
                if (formal) {
                    issue("LITERAL", i, 3, "A literal ${ is plain text in Velocity but the new engine reads it as a variable");
                }
                return false; // "$5", "$ ": plain text
            }
            int segments = 1;
            j = m.end();
            while (j + 1 < n && s.charAt(j) == '.') {
                var next = IDENT.matcher(s);
                next.region(j + 1, n);
                if (!next.lookingAt()) {
                    break;
                }
                j = next.end();
                segments++;
            }
            var dotted = s.substring(m.start(), j);
            if (formal) {
                if (j >= n || s.charAt(j) != '}') {
                    int close = s.indexOf('}', j);
                    int end = close < 0 ? n : close + 1;
                    issue("EXPRESSION", i, end - i, "Only a plain ${name.path} can be converted");
                    copy(end);
                    return true;
                }
                j++;
            } else if (j < n && s.charAt(j) == '(' && segments > 1) {
                int close = matching(j, '(', ')');
                int end = close < 0 ? n : close + 1;
                issue("METHOD_CALL", i, end - i, "A method call can only run in Velocity");
                copy(end);
                return true;
            } else if (j < n && s.charAt(j) == '[') {
                int close = matching(j, '[', ']');
                int end = close < 0 ? n : close + 1;
                issue("INDEX", i, end - i, "An index can only be read in Velocity");
                copy(end);
                return true;
            }
            var mapped = dotted.equals("TEMPLATE_CONTENT") ? null : path(dotted, true);
            if (dotted.equals("TEMPLATE_CONTENT")) {
                out.append("{{ TEMPLATE_CONTENT | raw }}");
            } else if (mapped == null) {
                copy(j);
                return true;
            } else {
                out.append("${").append(mapped).append('}');
            }
            i = j;
            return true;
        }

        // ---------------------------------------------------------- conditions

        /**
         * Converts the condition of an #if or #elseif; returns null (and records an issue) when it uses anything the
         * Dynamia engine does not have: parentheses, word operators, method calls, interpolated strings.
         */
        private String condition(String raw, int at) {
            var c = raw.trim()
                    .replaceAll("!\\s*\\$!?\\{?foreach\\.hasNext}?", "\\$foreach.last")
                    .replaceAll("\\$!?\\{?foreach\\.hasNext}?", "!\\$foreach.last");
            var result = new StringBuilder();
            int k = 0;
            int len = c.length();
            while (k < len) {
                char ch = c.charAt(k);
                if (Character.isWhitespace(ch)) {
                    result.append(ch);
                    k++;
                } else if (ch == '$') {
                    int j = k + 1;
                    if (j < len && c.charAt(j) == '!') {
                        j++;
                    }
                    boolean formal = j < len && c.charAt(j) == '{';
                    if (formal) {
                        j++;
                    }
                    var m = Pattern.compile("[A-Za-z_]\\w*(?:\\.[A-Za-z_]\\w*)*").matcher(c);
                    m.region(j, len);
                    if (!m.lookingAt()) {
                        return conditionIssue(at, raw, "Unsupported text in the condition");
                    }
                    j = m.end();
                    if (formal) {
                        if (j >= len || c.charAt(j) != '}') {
                            return conditionIssue(at, raw, "Only plain ${name.path} can be converted");
                        }
                        j++;
                    }
                    if (j < len && (c.charAt(j) == '(' || c.charAt(j) == '[')) {
                        return conditionIssue(at, raw, "A method call or index in a condition can only run in Velocity");
                    }
                    var mapped = path(m.group(), false);
                    if (mapped == null) {
                        return null;
                    }
                    result.append(mapped);
                    k = j;
                } else if (ch == '\'' || ch == '"') {
                    int close = c.indexOf(ch, k + 1);
                    if (close < 0 || ch == '"' && c.substring(k, close).matches("(?s).*[$#].*")) {
                        return conditionIssue(at, raw, "A double quoted string with $ is interpolated by Velocity");
                    }
                    result.append(c, k, close + 1);
                    k = close + 1;
                } else if ("&&".equals(c.substring(k, Math.min(len, k + 2))) || "||".equals(c.substring(k, Math.min(len, k + 2)))
                        || "==".equals(c.substring(k, Math.min(len, k + 2))) || "!=".equals(c.substring(k, Math.min(len, k + 2)))
                        || ">=".equals(c.substring(k, Math.min(len, k + 2))) || "<=".equals(c.substring(k, Math.min(len, k + 2)))) {
                    result.append(c, k, k + 2);
                    k += 2;
                } else if (ch == '!' || ch == '>' || ch == '<') {
                    result.append(ch);
                    k++;
                } else if (Character.isDigit(ch) || ch == '-' && k + 1 < len && Character.isDigit(c.charAt(k + 1))) {
                    int j = k + 1;
                    while (j < len && (Character.isDigit(c.charAt(j)) || c.charAt(j) == '.')) {
                        j++;
                    }
                    result.append(c, k, j);
                    k = j;
                } else if (Character.isLetter(ch)) {
                    var word = IDENT.matcher(c);
                    word.region(k, len);
                    word.lookingAt();
                    if (!Set.of("true", "false", "null").contains(word.group())) {
                        return conditionIssue(at, raw, "'" + word.group() + "' is not supported in a condition");
                    }
                    result.append(word.group());
                    k = word.end();
                } else {
                    return conditionIssue(at, raw, "'" + ch + "' (parentheses or other operators) is not supported in a condition");
                }
            }
            var converted = result.toString().trim();
            if (converted.isEmpty()) {
                return conditionIssue(at, raw, "Empty condition");
            }
            return converted;
        }

        private String conditionIssue(int at, String raw, String reason) {
            issue("CONDITION", at, raw.length() + 6, reason);
            return null;
        }
    }
}
