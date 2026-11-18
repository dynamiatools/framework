package tools.dynamia.templates;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * The filters that are not part of the expression engine itself: the text of lists and the ones added by
 * applications. {@code upper}, {@code number}, {@code sum} and the other basic ones live in {@link TemplateExpressions}
 * and cannot be replaced.
 */
public final class TemplateFilters {

    /** Names of the filters the engine has built in. */
    private static final Set<String> RESERVED = Set.of("upper", "lower", "trim", "default", "number", "currency", "date",
            "count", "sum", "avg", "min", "max", "raw");
    private static final Pattern NAME = Pattern.compile("[A-Za-z][A-Za-z0-9_]*");

    private static final Map<String, TemplateFilter> FILTERS = new ConcurrentHashMap<>();
    private static volatile boolean discovered;

    static {
        register(new ListFilter("join", null, null));
        register(new ListFilter("list", null, null));
        register(new ListFilter("list_dots", "\u2022 ", null));
        register(new ListFilter("list_number", null, ""));
    }

    private TemplateFilters() {
    }

    /**
     * @throws IllegalArgumentException when the name is not valid, is one of the built-in filters or is taken
     */
    public static void register(TemplateFilter filter) {
        var name = filter.name();
        if (name == null || !NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid filter name: " + name);
        }
        if (RESERVED.contains(name)) {
            throw new IllegalArgumentException("'" + name + "' is a built-in filter and cannot be replaced");
        }
        if (FILTERS.putIfAbsent(name, filter) != null) {
            throw new IllegalArgumentException("There is already a filter named '" + name + "'");
        }
    }

    public static void unregister(String name) {
        FILTERS.remove(name);
    }

    /** The filter with that name (registered, or listed as a service), or null. */
    static TemplateFilter find(String name) {
        discover();
        return FILTERS.get(name);
    }

    /** The names of the registered filters, built-in ones of the engine not included. */
    public static Set<String> names() {
        discover();
        return Set.copyOf(FILTERS.keySet());
    }

    private static void discover() {
        if (discovered) {
            return;
        }
        synchronized (FILTERS) {
            if (discovered) {
                return;
            }
            discovered = true;
            for (var filter : ServiceLoader.load(TemplateFilter.class)) {
                try {
                    register(filter);
                } catch (IllegalArgumentException e) {
                    // a service that clashes with another filter is skipped, the template language keeps working
                }
            }
        }
    }

    /**
     * Lists as text. The argument is the property of each item to print ({@code tags|list:name}); without it the item
     * itself is printed.
     * <ul>
     *   <li>{@code list}: {@code a, b, c}</li>
     *   <li>{@code join:sep}: {@code a-b-c} with {@code join:-}; {@code \n} is a line break</li>
     *   <li>{@code list_dots}: one line per item, starting with a bullet (an unordered list)</li>
     *   <li>{@code list_number}: one line per item, {@code 1. a} (an ordered list)</li>
     * </ul>
     * A value that is not a list comes out as it is, a missing value stays missing and an empty list is empty text.
     */
    private record ListFilter(String name, String bullet, String numbered) implements TemplateFilter {

        @Override
        public Object apply(Object value, String argument, Map<String, Object> variables) {
            if (value == null) {
                return null;
            }
            var items = items(value);
            if (items == null) {
                return value;
            }
            boolean join = name.equals("join");
            var field = join ? null : argument == null || argument.isBlank() ? null : argument.trim();
            var texts = new ArrayList<String>();
            for (var item : items) {
                var picked = field == null ? item : TemplateExpressions.resolve(wrap(item), "x." + field);
                if (picked != null && picked != TemplateExpressions.UNRESOLVED) {
                    texts.add(text(picked));
                }
            }
            if (bullet != null) {
                return String.join("\n", texts.stream().map(t -> bullet + t).toList());
            }
            if (numbered != null) {
                var out = new ArrayList<String>();
                for (int i = 0; i < texts.size(); i++) {
                    out.add((i + 1) + ". " + texts.get(i));
                }
                return String.join("\n", out);
            }
            var separator = join ? (argument == null || argument.isBlank() ? ", " : argument.trim().replace("\\n", "\n")) : ", ";
            return String.join(separator, texts);
        }

        private static Map<String, Object> wrap(Object item) {
            var map = new java.util.HashMap<String, Object>();
            map.put("x", item);
            return map;
        }

        private static List<Object> items(Object value) {
            if (value instanceof Iterable<?> iterable) {
                var out = new ArrayList<Object>();
                iterable.forEach(out::add);
                return out;
            }
            if (value instanceof Object[] array) {
                return java.util.Arrays.asList(array);
            }
            return null;
        }

        private static String text(Object value) {
            return value instanceof BigDecimal d ? d.stripTrailingZeros().toPlainString() : String.valueOf(value);
        }
    }
}
