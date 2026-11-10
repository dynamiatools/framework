package tools.dynamia.templates;

import java.beans.Introspector;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Safe expression support for templates: property paths, display filters and conditions.
 * <p>
 * Templates are edited by customers, so nothing here evaluates code: a path walks Map keys and
 * public bean getters only (never {@code getClass}), filters come from a fixed list and a condition
 * is a comparison of two operands joined by {@code &&} / {@code ||}.
 */
final class TemplateExpressions {

    static final String LOCALE_VAR = "locale";

    /** Marker for a root variable that does not exist (as opposed to one that exists and is null). */
    static final Object UNRESOLVED = new Object();

    private TemplateExpressions() {
    }

    // ---------------------------------------------------------------- paths

    /**
     * @return the value, null when the root exists but the value is null/missing, or UNRESOLVED when the
     * root variable is not defined.
     */
    static Object resolve(Map<String, Object> vars, String path) {
        // a variable whose own name has a dot ("empresa.nombre") wins, as it always did
        if (vars.containsKey(path.trim())) {
            return vars.get(path.trim());
        }
        var parts = path.trim().split("\\.");
        if (!vars.containsKey(parts[0])) {
            return UNRESOLVED;
        }
        Object current = vars.get(parts[0]);
        for (int i = 1; i < parts.length && current != null; i++) {
            current = property(current, parts[i]);
        }
        return current;
    }

    private static Object property(Object target, String name) {
        if (target instanceof Map<?, ?> map) {
            return safe(map.get(name));
        }
        if (isBlocked(target.getClass())) {
            return null;
        }
        try {
            for (var pd : Introspector.getBeanInfo(target.getClass()).getPropertyDescriptors()) {
                var getter = pd.getReadMethod();
                if (getter != null && pd.getName().equals(name) && isReadable(getter)) {
                    return safe(getter.invoke(target));
                }
            }
            // records: name()
            if (target.getClass().isRecord()) {
                var method = target.getClass().getMethod(name);
                if (method.getParameterCount() == 0 && isReadable(method)) {
                    return safe(method.invoke(target));
                }
            }
        } catch (Exception e) {
            // not readable: behaves like a missing property
        }
        return null;
    }

    /** Public getters of public classes, except what every object has (getClass, getDeclaringClass...). */
    private static boolean isReadable(java.lang.reflect.Method getter) {
        var owner = getter.getDeclaringClass();
        return java.lang.reflect.Modifier.isPublic(owner.getModifiers())
                && owner != Object.class && owner != Enum.class && owner != Record.class
                && !isBlocked(getter.getReturnType());
    }

    /**
     * Types a template must never reach: reflection, class loading, files, threads and processes. Data are meant to be
     * Maps, beans, records and plain values; walking into these would leak information about the server.
     */
    private static boolean isBlocked(Class<?> type) {
        return Class.class.isAssignableFrom(type) || ClassLoader.class.isAssignableFrom(type)
                || Module.class.isAssignableFrom(type) || ModuleLayer.class.isAssignableFrom(type)
                || Package.class.isAssignableFrom(type) || java.security.ProtectionDomain.class.isAssignableFrom(type)
                || Thread.class.isAssignableFrom(type) || ThreadGroup.class.isAssignableFrom(type)
                || Process.class.isAssignableFrom(type) || ProcessHandle.class.isAssignableFrom(type)
                || Runtime.class.isAssignableFrom(type) || java.io.File.class.isAssignableFrom(type)
                || java.nio.file.Path.class.isAssignableFrom(type) || java.lang.reflect.AccessibleObject.class.isAssignableFrom(type)
                || java.lang.reflect.Member.class.isAssignableFrom(type) || type.getName().startsWith("java.lang.invoke.")
                || type.getName().startsWith("jdk.internal.") || type.getName().startsWith("sun.");
    }

    private static Object safe(Object value) {
        return value != null && isBlocked(value.getClass()) ? null : value;
    }

    // ------------------------------------------------------------- ternary

    /** Position of the first character of {@code c} that is outside quotes and parentheses, from {@code from}, or -1. */
    private static int topLevel(String text, char c, int from) {
        int depth = 0;
        char quote = 0;
        for (int i = from; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (quote != 0) {
                if (ch == quote) {
                    quote = 0;
                }
            } else if (ch == '\'' || ch == '"') {
                quote = ch;
            } else if (ch == '(') {
                depth++;
            } else if (ch == ')') {
                depth--;
            } else if (depth == 0 && ch == c) {
                return i;
            }
        }
        return -1;
    }

    /** Whether the text is {@code condition ? a : b} (the condition may use the same operators as an {@code if} test). */
    static boolean isTernary(String text) {
        if (text == null) {
            return false;
        }
        int q = topLevel(text, '?', 0);
        return q > 0 && ternaryColon(text, q) > q;
    }

    /** The colon that matches the question mark at {@code q}, skipping the ones of nested ternaries. */
    private static int ternaryColon(String text, int q) {
        int nested = 0;
        int from = q + 1;
        while (true) {
            int colon = topLevel(text, ':', from);
            int question = topLevel(text, '?', from);
            if (colon < 0) {
                return -1;
            }
            if (question >= 0 && question < colon) {
                nested++;
                from = question + 1;
            } else if (nested > 0) {
                nested--;
                from = colon + 1;
            } else {
                return colon;
            }
        }
    }

    /**
     * {@code ${d.paid ? 'PAID' : 'PENDING'}}: the first branch when the condition holds, else the second. Branches are
     * quoted text, numbers, variables or another ternary. Filters are not applied after it.
     */
    static Object ternary(String text, Map<String, Object> vars) {
        int q = topLevel(text, '?', 0);
        int colon = ternaryColon(text, q);
        var holds = test(text.substring(0, q), vars);
        return branch(text.substring(holds ? q + 1 : colon + 1, holds ? colon : text.length()).trim(), vars);
    }

    private static Object branch(String text, Map<String, Object> vars) {
        if (isTernary(text)) {
            return ternary(text, vars);
        }
        var value = operand(text, vars);
        return value;
    }

    // ----------------------------------------------------------- arithmetic

    private static final java.util.regex.Pattern ARITHMETIC = java.util.regex.Pattern.compile(
            "(?s).*([\\w.)]\\s*[*/+\\-]\\s*[\\w.(\\-]|^\\s*\\(.*\\)\\s*[*/+\\-]).*");

    /** Whether the text looks like {@code qty * price} or {@code (a + b) / 2}: operators between operands. */
    static boolean isArithmetic(String text) {
        return text != null && ARITHMETIC.matcher(text).matches();
    }

    /**
     * Evaluates {@code + - * /} with parentheses over numbers and variable paths, with no code involved. A variable that
     * is null or not a number counts as 0 (a blank amount adds nothing); dividing by zero gives null (empty text).
     *
     * @return the number, null when it cannot be computed, or UNRESOLVED when a variable is not defined at all
     */
    static Object arithmetic(String expression, java.util.function.Function<String, Object> resolver) {
        try {
            var parser = new Arithmetic(expression, resolver);
            var value = parser.expression();
            parser.end();
            return value == null ? null : plainNumber(value);
        } catch (Arithmetic.Unresolved e) {
            return UNRESOLVED;
        } catch (ArithmeticException | IllegalArgumentException e) {
            return null;
        }
    }

    private static BigDecimal plainNumber(BigDecimal value) {
        var stripped = value.stripTrailingZeros();
        return stripped.scale() < 0 ? stripped.setScale(0) : stripped;
    }

    /** A small recursive descent parser: expression, term, factor. */
    private static final class Arithmetic {
        static final class Unresolved extends RuntimeException {
            Unresolved() {
                super(null, null, false, false);
            }
        }

        private final String text;
        private final java.util.function.Function<String, Object> resolver;
        private int pos;

        Arithmetic(String text, java.util.function.Function<String, Object> resolver) {
            this.text = text;
            this.resolver = resolver;
        }

        BigDecimal expression() {
            var value = term();
            while (true) {
                skip();
                if (peek() == '+') {
                    pos++;
                    value = combine(value, term(), '+');
                } else if (peek() == '-') {
                    pos++;
                    value = combine(value, term(), '-');
                } else {
                    return value;
                }
            }
        }

        private BigDecimal term() {
            var value = factor();
            while (true) {
                skip();
                if (peek() == '*') {
                    pos++;
                    value = combine(value, factor(), '*');
                } else if (peek() == '/') {
                    pos++;
                    value = combine(value, factor(), '/');
                } else {
                    return value;
                }
            }
        }

        private BigDecimal factor() {
            skip();
            char c = peek();
            if (c == '(') {
                pos++;
                var value = expression();
                skip();
                if (peek() != ')') {
                    throw new IllegalArgumentException("Missing )");
                }
                pos++;
                return value;
            }
            if (c == '-') {
                pos++;
                var value = factor();
                return value == null ? null : value.negate();
            }
            int start = pos;
            if (Character.isDigit(c)) {
                while (pos < text.length() && (Character.isDigit(text.charAt(pos)) || text.charAt(pos) == '.')) {
                    pos++;
                }
                return new BigDecimal(text.substring(start, pos));
            }
            if (Character.isLetter(c) || c == '_') {
                while (pos < text.length() && (Character.isLetterOrDigit(text.charAt(pos)) || text.charAt(pos) == '_' || text.charAt(pos) == '.')) {
                    pos++;
                }
                var value = resolver.apply(text.substring(start, pos));
                if (value == UNRESOLVED) {
                    throw new Unresolved();
                }
                return number(value);
            }
            throw new IllegalArgumentException("Unexpected " + c);
        }

        private static BigDecimal number(Object value) {
            if (value == null) {
                return BigDecimal.ZERO;
            }
            if (value instanceof BigDecimal b) {
                return b;
            }
            if (value instanceof Number n) {
                return new BigDecimal(n.toString());
            }
            try {
                return new BigDecimal(String.valueOf(value).trim());
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }

        private static BigDecimal combine(BigDecimal a, BigDecimal b, char op) {
            if (a == null || b == null) {
                return null;
            }
            return switch (op) {
                case '+' -> a.add(b);
                case '-' -> a.subtract(b);
                case '*' -> a.multiply(b);
                default -> b.signum() == 0 ? null : a.divide(b, 10, java.math.RoundingMode.HALF_UP);
            };
        }

        private void skip() {
            while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
                pos++;
            }
        }

        private char peek() {
            return pos < text.length() ? text.charAt(pos) : '\0';
        }

        void end() {
            skip();
            if (pos < text.length()) {
                throw new IllegalArgumentException("Unexpected " + text.charAt(pos));
            }
        }
    }

    // -------------------------------------------------------------- filters

    /** Applies a chain such as {@code upper|default:-}; unknown filters are ignored. */
    static Object applyFilters(Object value, String[] filters, Map<String, Object> vars) {
        Object current = value;
        for (var filter : filters) {
            var f = filter.trim();
            var colon = f.indexOf(':');
            var name = colon < 0 ? f : f.substring(0, colon).trim();
            var arg = colon < 0 ? null : f.substring(colon + 1);
            current = applyFilter(current, name, arg, vars);
        }
        return current;
    }

    private static Object applyFilter(Object value, String name, String arg, Map<String, Object> vars) {
        switch (name) {
            case "upper":
                return value == null ? null : String.valueOf(value).toUpperCase(locale(vars));
            case "lower":
                return value == null ? null : String.valueOf(value).toLowerCase(locale(vars));
            case "trim":
                return value == null ? null : String.valueOf(value).trim();
            case "default":
                return value == null || String.valueOf(value).isBlank() ? arg : value;
            case "number":
                return number(value, arg == null || arg.isBlank() ? "#,##0.##" : arg, vars);
            case "currency":
                return number(value, decimalsPattern(arg), vars);
            case "date":
                return date(value, arg == null || arg.isBlank() ? "dd/MM/yyyy" : arg, vars);
            case "count", "sum", "avg", "min", "max":
                return aggregate(value, name, arg);
            default:
                return custom(value, name, arg, vars);
        }
    }

    /** A filter registered with {@link TemplateFilters}; an unknown one, or one that fails, lets the value through. */
    private static Object custom(Object value, String name, String arg, Map<String, Object> vars) {
        var filter = TemplateFilters.find(name);
        if (filter == null) {
            return value;
        }
        try {
            return filter.apply(value, arg, vars);
        } catch (RuntimeException e) {
            return value;
        }
    }

    /**
     * {@code ${items|sum:price}}, {@code count}, {@code avg:price}, {@code min:price}, {@code max:price} over a list:
     * the totals a report prints at its end or at the end of a group. Items that are not numbers are skipped (the
     * average is over the numbers); the average, minimum and maximum of nothing are empty.
     */
    private static Object aggregate(Object value, String name, String field) {
        if (!(value instanceof Iterable<?> items)) {
            return value;
        }
        int count = 0;
        int numeric = 0;
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal min = null;
        BigDecimal max = null;
        for (var item : items) {
            count++;
            if (field == null || field.isBlank()) {
                continue;
            }
            var scope = java.util.Map.<String, Object>of("i", item == null ? "" : item);
            Object raw = isArithmetic(field)
                    ? arithmetic(field, name2 -> resolve(scope, "i." + name2))
                    : resolve(scope, "i." + field.trim());
            if (raw == UNRESOLVED || raw == null) {
                continue;
            }
            try {
                var n = raw instanceof BigDecimal b ? b : new BigDecimal(String.valueOf(raw).trim());
                numeric++;
                sum = sum.add(n);
                min = min == null || n.compareTo(min) < 0 ? n : min;
                max = max == null || n.compareTo(max) > 0 ? n : max;
            } catch (NumberFormatException skipped) {
                // not a number: not part of the total
            }
        }
        return switch (name) {
            case "count" -> count;
            case "sum" -> sum;
            case "avg" -> numeric == 0 ? null : plain(sum.divide(BigDecimal.valueOf(numeric), 6, java.math.RoundingMode.HALF_UP));
            case "min" -> min;
            default -> max;
        };
    }

    /** Without trailing zeros and without exponent: 10, not 1E+1. */
    private static BigDecimal plain(BigDecimal value) {
        var stripped = value.stripTrailingZeros();
        return stripped.scale() < 0 ? stripped.setScale(0) : stripped;
    }

    private static String decimalsPattern(String decimals) {
        int n = 2;
        try {
            if (decimals != null && !decimals.isBlank()) {
                n = Math.max(0, Math.min(6, Integer.parseInt(decimals.trim())));
            }
        } catch (NumberFormatException ignored) {
            // keep the default
        }
        return n == 0 ? "#,##0" : "#,##0." + "0".repeat(n);
    }

    private static Object number(Object value, String pattern, Map<String, Object> vars) {
        if (value == null) {
            return null;
        }
        try {
            var number = value instanceof Number n ? n : new BigDecimal(String.valueOf(value).trim());
            return new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(locale(vars))).format(number);
        } catch (RuntimeException e) {
            return value;
        }
    }

    private static Object date(Object value, String pattern, Map<String, Object> vars) {
        if (value == null) {
            return null;
        }
        try {
            var formatter = DateTimeFormatter.ofPattern(pattern, locale(vars));
            var zone = ZoneId.systemDefault();
            if (value instanceof LocalDate d) {
                return formatter.format(d);
            } else if (value instanceof LocalDateTime d) {
                return formatter.format(d);
            } else if (value instanceof Instant i) {
                return formatter.format(i.atZone(zone));
            } else if (value instanceof Date d) {
                return formatter.format(d.toInstant().atZone(zone));
            } else if (value instanceof CharSequence text) {
                // a date that came as JSON (the editor's sample data, a REST call) is an ISO string
                var parsed = parseIso(text.toString().trim());
                if (parsed != null) {
                    return formatter.format(parsed);
                }
            }
        } catch (RuntimeException e) {
            // bad pattern or unsupported field: show the raw value
        }
        return value;
    }

    /** An ISO date ({@code 2026-11-01}), date and time ({@code 2026-11-01T14:30:00}) or instant ({@code ...Z}); null for anything else. */
    private static java.time.temporal.TemporalAccessor parseIso(String text) {
        if (text.length() < 10 || !Character.isDigit(text.charAt(0))) {
            return null;
        }
        try {
            if (text.length() == 10) {
                return LocalDate.parse(text);
            }
            if (text.endsWith("Z") || text.matches(".*[+-]\\d\\d:\\d\\d$")) {
                return java.time.OffsetDateTime.parse(text).atZoneSameInstant(ZoneId.systemDefault());
            }
            return LocalDateTime.parse(text);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Locale locale(Map<String, Object> vars) {
        var l = vars.get(LOCALE_VAR);
        if (l instanceof Locale locale) {
            return locale;
        }
        if (l instanceof String tag && !tag.isBlank()) {
            return Locale.forLanguageTag(tag.replace('_', '-'));
        }
        return Locale.getDefault();
    }

    // ----------------------------------------------------------- conditions

    /**
     * Evaluates {@code a == b}, {@code a != b}, {@code a > b}, {@code a >= b}, {@code a < b},
     * {@code a <= b}, a single operand (truthiness) or {@code !operand}, combined with {@code &&} and
     * {@code ||} (no parentheses; {@code &&} binds tighter). Operands are paths, numbers or quoted text.
     */
    static boolean test(String expression, Map<String, Object> vars) {
        if (expression == null || expression.isBlank()) {
            return true;
        }
        for (var orPart : expression.split("\\|\\|")) {
            boolean all = true;
            for (var andPart : orPart.split("&&")) {
                if (!atom(andPart.trim(), vars)) {
                    all = false;
                    break;
                }
            }
            if (all) {
                return true;
            }
        }
        return false;
    }

    private static boolean atom(String atom, Map<String, Object> vars) {
        for (var op : new String[]{"==", "!=", ">=", "<=", ">", "<"}) {
            int i = atom.indexOf(op);
            if (i > 0) {
                var left = operand(atom.substring(0, i), vars);
                var right = operand(atom.substring(i + op.length()), vars);
                return compare(left, right, op);
            }
        }
        if (atom.startsWith("!")) {
            return !truthy(operand(atom.substring(1), vars));
        }
        return truthy(operand(atom, vars));
    }

    private static Object operand(String text, Map<String, Object> vars) {
        var t = text.trim();
        if (t.length() >= 2 && (t.startsWith("'") && t.endsWith("'") || t.startsWith("\"") && t.endsWith("\""))) {
            return t.substring(1, t.length() - 1);
        }
        t = unwrap(t);
        if (t.matches("-?\\d+(\\.\\d+)?")) {
            return new BigDecimal(t);
        }
        if ("true".equals(t) || "false".equals(t)) {
            return Boolean.valueOf(t);
        }
        var value = resolve(vars, t);
        return value == UNRESOLVED ? null : value;
    }

    private static boolean compare(Object left, Object right, String op) {
        BigDecimal a = decimal(left);
        BigDecimal b = decimal(right);
        int cmp;
        if (a != null && b != null) {
            cmp = a.compareTo(b);
        } else {
            var l = left == null ? "" : String.valueOf(left);
            var r = right == null ? "" : String.valueOf(right);
            cmp = l.compareTo(r);
        }
        return switch (op) {
            case "==" -> cmp == 0;
            case "!=" -> cmp != 0;
            case ">" -> cmp > 0;
            case ">=" -> cmp >= 0;
            case "<" -> cmp < 0;
            default -> cmp <= 0;
        };
    }

    private static BigDecimal decimal(Object o) {
        if (o instanceof BigDecimal d) {
            return d;
        }
        if (o instanceof Number n) {
            return new BigDecimal(n.toString());
        }
        if (o instanceof String s && s.trim().matches("-?\\d+(\\.\\d+)?")) {
            return new BigDecimal(s.trim());
        }
        return null;
    }

    static boolean truthy(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof Number n) {
            return new BigDecimal(n.toString()).signum() != 0;
        }
        if (value instanceof java.util.Collection<?> c) {
            return !c.isEmpty();
        }
        var s = String.valueOf(value).trim();
        return !s.isEmpty() && !"false".equalsIgnoreCase(s) && !"0".equals(s);
    }

    /**
     * The name inside a whole {@code ${name}} or {@code {{name}}} (trimmed); any other text comes back as it is. Both
     * styles are the same thing: {@code <list value="{{items}}">} is {@code <list value="${items}">}.
     */
    public static String unwrap(String text) {
        if (text == null) {
            return null;
        }
        var t = text.trim();
        if (t.startsWith("${") && t.endsWith("}") && t.length() >= 3) {
            return t.substring(2, t.length() - 1).trim();
        }
        if (t.startsWith("{{") && t.endsWith("}}") && t.length() >= 4) {
            return t.substring(2, t.length() - 2).trim();
        }
        return text;
    }
}
