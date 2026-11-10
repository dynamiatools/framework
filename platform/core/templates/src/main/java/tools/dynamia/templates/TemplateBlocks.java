package tools.dynamia.templates;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

/**
 * The two block tags of the template language, parsed into a small tree and rendered without running any code:
 * <pre>{@code
 * {{#if paid && total > 0}} ... {{else}} ... {{/if}}
 * {{#each items as item}} ${item.name} (${loop.number} of ${loop.count}) {{/each}}
 * }</pre>
 * The {@code #if} condition uses the same operators as the {@code ${cond ? a : b}} ternary. Inside {@code #each} the
 * alias holds the current item and {@code loop} holds {@code index} (from 0), {@code number} (from 1),
 * {@code count}, {@code first} and {@code last}.
 * <p>
 * The tags are read from the template only, never from the values inserted, and the work is capped (nesting depth and
 * total iterations), so a template cannot loop for ever or blow up the output. A template whose tags do not pair up
 * is not a block template at all: {@link #parse} returns null and the text is left as it is.
 */
final class TemplateBlocks {

    /** Deepest nesting of blocks accepted. */
    static final int MAX_DEPTH = 10;
    /** Iterations of all the {@code #each} of one evaluation; the rest are skipped. */
    static final int MAX_ITERATIONS = 10_000;

    private static final Pattern TAG = Pattern.compile("\\{\\{\\s*(#if|#each|else|/if|/each)(?:\\s+([^}]*?))?\\s*}}");
    private static final Pattern EACH = Pattern.compile("(.+?)\\s+as\\s+([A-Za-z_]\\w*)");

    private TemplateBlocks() {
    }

    sealed interface Node permits Text, If, Each {
    }

    record Text(String text) implements Node {
    }

    record If(String condition, List<Node> then, List<Node> otherwise) implements Node {
    }

    record Each(String source, String alias, List<Node> body) implements Node {
    }

    /** Whether the text has a block tag worth parsing. */
    static boolean hasBlocks(String template) {
        return template.contains("{{#");
    }

    private static final class Frame {
        final String kind;
        final String argument;
        final String alias;
        List<Node> then;
        List<Node> current = new ArrayList<>();

        Frame(String kind, String argument, String alias) {
            this.kind = kind;
            this.argument = argument;
            this.alias = alias;
        }
    }

    /**
     * @return the tree, or null when a tag is unbalanced, an {@code #each} has no {@code as alias}, an {@code #if} has
     * no condition or the nesting is too deep
     */
    static List<Node> parse(String template) {
        var root = new ArrayList<Node>();
        Deque<Frame> stack = new ArrayDeque<>();
        var m = TAG.matcher(template);
        int last = 0;
        while (m.find()) {
            var out = stack.isEmpty() ? root : stack.peek().current;
            if (m.start() > last) {
                out.add(new Text(template.substring(last, m.start())));
            }
            last = m.end();
            var tag = m.group(1);
            var arg = m.group(2) == null ? "" : m.group(2).trim();
            switch (tag) {
                case "#if" -> {
                    if (arg.isEmpty() || stack.size() >= MAX_DEPTH) {
                        return null;
                    }
                    stack.push(new Frame("if", arg, null));
                }
                case "#each" -> {
                    var e = EACH.matcher(arg);
                    if (!e.matches() || stack.size() >= MAX_DEPTH) {
                        return null;
                    }
                    stack.push(new Frame("each", e.group(1).trim(), e.group(2)));
                }
                case "else" -> {
                    var top = stack.peek();
                    if (top == null || !top.kind.equals("if") || top.then != null) {
                        return null;
                    }
                    top.then = top.current;
                    top.current = new ArrayList<>();
                }
                default -> { // /if or /each
                    var top = stack.peek();
                    if (top == null || !top.kind.equals(tag.substring(1))) {
                        return null;
                    }
                    stack.pop();
                    var parent = stack.isEmpty() ? root : stack.peek().current;
                    if (top.kind.equals("if")) {
                        parent.add(top.then == null
                                ? new If(top.argument, top.current, List.of())
                                : new If(top.argument, top.then, top.current));
                    } else {
                        parent.add(new Each(top.argument, top.alias, top.current));
                    }
                }
            }
        }
        if (!stack.isEmpty()) {
            return null;
        }
        if (last < template.length()) {
            root.add(new Text(template.substring(last)));
        }
        return root;
    }

    /**
     * @param text replaces the variables of a text node, given the data of that point of the template
     */
    static String render(List<Node> nodes, Map<String, Object> vars, BiFunction<String, Map<String, Object>, String> text) {
        var out = new StringBuilder();
        render(nodes, vars, text, out, new int[]{0});
        return out.toString();
    }

    private static void render(List<Node> nodes, Map<String, Object> vars,
                               BiFunction<String, Map<String, Object>, String> text, StringBuilder out, int[] iterations) {
        for (var node : nodes) {
            switch (node) {
                case Text t -> out.append(text.apply(t.text(), vars));
                case If i -> render(TemplateExpressions.test(i.condition(), vars) ? i.then() : i.otherwise(),
                        vars, text, out, iterations);
                case Each e -> {
                    var items = items(TemplateExpressions.resolve(vars, e.source()));
                    int count = items.size();
                    for (int n = 0; n < count && iterations[0] < MAX_ITERATIONS; n++, iterations[0]++) {
                        var scope = new HashMap<>(vars);
                        scope.put(e.alias(), items.get(n));
                        scope.put("loop", Map.of("index", n, "number", n + 1, "count", count,
                                "first", n == 0, "last", n == count - 1));
                        render(e.body(), scope, text, out, iterations);
                    }
                }
            }
        }
    }

    /** The items of a list, a set or an array; anything else, including a missing variable, has none. */
    private static List<Object> items(Object value) {
        var list = new ArrayList<Object>();
        if (value instanceof Iterable<?> it) {
            for (var item : it) {
                if (list.size() >= MAX_ITERATIONS) {
                    break;
                }
                list.add(item);
            }
        } else if (value != null && value != TemplateExpressions.UNRESOLVED && value.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(value); i++) {
                list.add(Array.get(value, i));
            }
        }
        return list;
    }
}
