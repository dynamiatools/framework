package tools.dynamia.modules.email.migration;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns the HTML of a migrated template into the blocks of the email editor, doing its best: headings (h1 to h3),
 * paragraphs, quotes, buttons, images, dividers, lists and tables become their own block; {@code {{#each}}} and
 * {@code {{#if}}} become Repeat and Condition blocks around the blocks between their tags; anything else is kept as an
 * HTML block, so the email still looks the same.
 *
 * <p>The whole template stays one HTML block when it cannot be split without breaking it: a {@code <style>}, a document
 * with attributes on its {@code <body>}, or control tags that do not nest properly.
 *
 * <p>The control tags are hidden in HTML comments before the HTML is parsed, because the parser moves text found between the
 * rows of a table out of the table, and put back when the HTML is written.
 */
final class HtmlToBlocks {

    private static final JsonMapper JSON = new JsonMapper();
    private static final Set<String> INLINE = Set.of("a", "b", "strong", "i", "em", "u", "span", "br", "small", "sup", "sub", "font", "s");
    private static final Pattern ALIGN = Pattern.compile("text-align\\s*:\\s*(left|center|right)", Pattern.CASE_INSENSITIVE);
    private static final Pattern COLOR = Pattern.compile("(?<![-\\w])color\\s*:\\s*(#[0-9a-fA-F]{3,8})", Pattern.CASE_INSENSITIVE);
    private static final Pattern BACKGROUND = Pattern.compile("background(?:-color)?\\s*:\\s*(#[0-9a-fA-F]{3,8})", Pattern.CASE_INSENSITIVE);
    private static final Pattern BORDER_LEFT = Pattern.compile("border-left[^;]*?(#[0-9a-fA-F]{3,8})", Pattern.CASE_INSENSITIVE);
    private static final Pattern RADIUS = Pattern.compile("border-radius\\s*:\\s*(\\d{1,2})px", Pattern.CASE_INSENSITIVE);
    private static final Pattern FONT_SIZE = Pattern.compile("font-size\\s*:\\s*(\\d{1,2})px", Pattern.CASE_INSENSITIVE);
    private static final Pattern FULL_WIDTH = Pattern.compile("width\\s*:\\s*100%", Pattern.CASE_INSENSITIVE);
    private static final Pattern BUTTON_CLASS = Pattern.compile("\\b(btn|button)\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern CONTROL = Pattern.compile("\\{\\{\\s*(?:#each\\s+[^{}]*?|#if\\s+[^{}]*?|else|/each|/if)\\s*\\}\\}");
    private static final Pattern HIDDEN_CONTROL = Pattern.compile("<!--dyn:(.*?)-->", Pattern.DOTALL);
    private static final Pattern EACH = Pattern.compile("^\\{\\{\\s*#each\\s+(\\S+)\\s+as\\s+(\\S+?)\\s*\\}\\}$");
    private static final Pattern IF = Pattern.compile("^\\{\\{\\s*#if\\s+(.+?)\\s*\\}\\}$");
    private static final Pattern ELSE = Pattern.compile("^\\{\\{\\s*else\\s*\\}\\}$");
    private static final Pattern END_EACH = Pattern.compile("^\\{\\{\\s*/each\\s*\\}\\}$");
    private static final Pattern END_IF = Pattern.compile("^\\{\\{\\s*/if\\s*\\}\\}$");
    private static final Pattern PATH = Pattern.compile("^[A-Za-z_][\\w.]*$");
    private static final Pattern ALIAS = Pattern.compile("^[A-Za-z_]\\w*$");
    private static final Pattern CONDITION = Pattern.compile("^[\\w.'\"!=<>&| \\-]+$");

    /**
     * @param design     the editor document (JSON)
     * @param blocks     how many content blocks it has
     * @param htmlBlocks how many of them are kept as HTML
     */
    record Result(String design, int blocks, int htmlBlocks) {
    }

    /** The template cannot be turned into blocks without breaking it. */
    private static final class Unsupported extends RuntimeException {
        Unsupported() {
            super(null, null, false, false);
        }
    }

    /** An open Repeat or Condition block: what is found next goes inside it. */
    private static final class Frame {
        final boolean repeat;
        final ArrayNode children;
        final ArrayNode elseChildren;
        boolean inElse;

        Frame(boolean repeat, ArrayNode children, ArrayNode elseChildren) {
            this.repeat = repeat;
            this.children = children;
            this.elseChildren = elseChildren;
        }

        ArrayNode current() {
            return inElse ? elseChildren : children;
        }
    }

    private final ArrayNode blocks = JSON.createArrayNode();
    private final Deque<Frame> frames = new ArrayDeque<>();
    /** Shared with the converters of the cells, so block ids are unique in the whole document. */
    private final int[] counter;
    /** Shared: [0] content blocks, [1] of them kept as HTML. */
    private final int[] stats;
    private final StringBuilder pendingHtml = new StringBuilder();
    private final StringBuilder pendingInline = new StringBuilder();

    private HtmlToBlocks(int[] counter, int[] stats) {
        this.counter = counter;
        this.stats = stats;
    }

    /**
     * Converts the HTML of a template into an editor document.
     *
     * @param html the HTML, already in the syntax of the new template engine
     * @return the document and how many of its blocks keep their HTML
     */
    static Result convert(String html) {
        if (html == null || html.isBlank()) {
            return single(html);
        }
        var lower = html.toLowerCase();
        if (lower.contains("<style") || lower.contains("<link")) {
            return single(html);
        }
        try {
            var fullDocument = lower.contains("<html") || lower.contains("<body");
            var hidden = hideControl(html);
            var document = fullDocument ? Jsoup.parse(hidden) : Jsoup.parseBodyFragment(hidden);
            document.outputSettings().prettyPrint(false); // keep the HTML as written, no new line breaks
            var body = document.body();
            if (fullDocument && !body.attributes().isEmpty()) {
                return single(html); // its background or font would be lost
            }
            var converter = new HtmlToBlocks(new int[1], new int[2]);
            body.childNodes().forEach(converter::node);
            converter.finish();
            if (converter.stats[0] == 0 || converter.stats[0] == converter.stats[1]) {
                return single(html);
            }
            return new Result(converter.document(), converter.stats[0], converter.stats[1]);
        } catch (Unsupported e) {
            return single(html);
        }
    }

    private static Result single(String html) {
        var root = JSON.createObjectNode();
        root.put("version", 1);
        var block = root.putArray("blocks").addObject();
        block.put("id", "html-1");
        block.put("type", "html");
        var props = block.putObject("props");
        props.put("html", html);
        props.put("fullDocument", html != null && html.toLowerCase().contains("<html"));
        return new Result(JSON.writeValueAsString(root), 1, 1);
    }

    private static String hideControl(String html) {
        return CONTROL.matcher(html).replaceAll(m -> {
            if (m.group().contains("--")) {
                throw new Unsupported(); // it would end the comment
            }
            return Matcher.quoteReplacement("<!--dyn:" + m.group() + "-->");
        });
    }

    private static String restore(String html) {
        return HIDDEN_CONTROL.matcher(html).replaceAll(m -> Matcher.quoteReplacement(m.group(1)));
    }

    private static boolean hasControl(Element e) {
        return e.outerHtml().contains("<!--dyn:");
    }

    /** True when the control tags inside the element open and close in it. */
    private static boolean balanced(Element e) {
        int depth = 0;
        var m = HIDDEN_CONTROL.matcher(e.outerHtml());
        while (m.find()) {
            var tag = m.group(1);
            if (EACH.matcher(tag).matches() || IF.matcher(tag).matches()) {
                depth++;
            } else if (END_EACH.matcher(tag).matches() || END_IF.matcher(tag).matches()) {
                depth--;
            }
            if (depth < 0) {
                return false;
            }
        }
        return depth == 0;
    }

    private String document() {
        var root = JSON.createObjectNode();
        root.put("version", 1);
        root.set("blocks", blocks);
        return JSON.writeValueAsString(root);
    }

    private ArrayNode target() {
        return frames.isEmpty() ? blocks : frames.peek().current();
    }

    private void finish() {
        flushHtml();
        if (!frames.isEmpty()) {
            throw new Unsupported(); // a {{#each}} or {{#if}} without its end
        }
    }

    // ------------------------------------------------------------ nodes

    private void node(Node node) {
        if (node instanceof Comment comment) {
            if (comment.getData().startsWith("dyn:")) {
                control(comment.getData().substring(4));
            }
            return;
        }
        if (node instanceof TextNode text) {
            if (!text.isBlank()) {
                emitHtml();
                pendingInline.append(text.outerHtml());
            }
            return;
        }
        if (!(node instanceof Element e)) {
            return;
        }
        if (hasControl(e) && !balanced(e)) {
            throw new Unsupported();
        }
        var tag = e.normalName();
        if ("a".equals(tag) && !hasControl(e) && isButton(e)) {
            flushInline();
            button(e, null);
            return;
        }
        if (INLINE.contains(tag)) {
            if (!"br".equals(tag) || pendingInline.length() > 0) {
                emitHtml();
                pendingInline.append(e.outerHtml());
            }
            return;
        }
        flushInline();
        switch (tag) {
            case "h1", "h2", "h3" -> heading(e);
            case "p" -> paragraph(e);
            case "blockquote" -> quote(e);
            case "img" -> image(e, null);
            case "hr" -> add("divider", props -> {
            });
            case "ul", "ol" -> list(e);
            case "table" -> table(e);
            default -> {
                if ("div".equals(tag) && e.children().isEmpty() && !e.text().isBlank() && e.attributes().isEmpty()) {
                    pendingInline.append(e.html()); // a div with only text and no attributes is a paragraph
                    flushInline();
                } else {
                    html(e);
                }
            }
        }
    }

    // ------------------------------------------------------------ control tags

    private void control(String tag) {
        flushHtml();
        Matcher m;
        if ((m = EACH.matcher(tag)).matches()) {
            if (!PATH.matcher(m.group(1)).matches() || !ALIAS.matcher(m.group(2)).matches()) {
                throw new Unsupported();
            }
            var source = m.group(1);
            var alias = m.group(2);
            open(true, props -> {
                props.put("source", source);
                props.put("alias", alias);
            });
        } else if ((m = IF.matcher(tag)).matches()) {
            if (!CONDITION.matcher(m.group(1)).matches()) {
                throw new Unsupported();
            }
            var expression = m.group(1);
            open(false, props -> props.put("expression", expression));
        } else if (ELSE.matcher(tag).matches()) {
            if (frames.isEmpty() || frames.peek().repeat || frames.peek().inElse) {
                throw new Unsupported();
            }
            frames.peek().inElse = true;
        } else if (END_EACH.matcher(tag).matches() || END_IF.matcher(tag).matches()) {
            var each = END_EACH.matcher(tag).matches();
            if (frames.isEmpty() || frames.peek().repeat != each) {
                throw new Unsupported();
            }
            frames.pop();
        } else {
            throw new Unsupported();
        }
    }

    private void open(boolean repeat, Consumer<ObjectNode> fill) {
        var type = repeat ? "repeat" : "condition";
        var block = target().addObject();
        block.put("id", type + "-" + (++counter[0]));
        block.put("type", type);
        fill.accept(block.putObject("props"));
        var children = block.putArray("children");
        frames.push(new Frame(repeat, children, repeat ? null : block.putArray("elseChildren")));
    }

    // ------------------------------------------------------------ blocks

    private void heading(Element e) {
        if (!e.children().isEmpty() || hasControl(e)) {
            html(e); // formatting inside would be lost in a plain-text heading
            return;
        }
        add("heading", props -> {
            props.put("text", e.text());
            props.put("level", e.normalName());
            style(e, props);
        });
    }

    private void paragraph(Element e) {
        var single = !hasControl(e) && e.children().size() == 1 && e.ownText().isBlank() ? e.child(0) : null;
        if (single != null && "img".equals(single.normalName())) {
            image(single, null);
            return;
        }
        if (single != null && "a".equals(single.normalName()) && single.children().size() == 1 && "img".equals(single.child(0).normalName())
                && single.ownText().isBlank()) {
            image(single.child(0), single.attr("href"));
            return;
        }
        if (single != null && "a".equals(single.normalName()) && isButton(single)) {
            button(single, e);
            return;
        }
        add("text", props -> {
            props.put("html", restore("<p>" + e.html() + "</p>"));
            style(e, props);
        });
    }

    private void quote(Element e) {
        if (hasControl(e)) {
            html(e);
            return;
        }
        var inner = e.html();
        var wrapped = e.children().stream().anyMatch(c -> "p".equals(c.normalName())) ? inner : "<p>" + inner + "</p>";
        add("quote", props -> {
            props.put("html", wrapped);
            var bar = BORDER_LEFT.matcher(e.attr("style"));
            if (bar.find()) {
                props.put("borderColor", bar.group(1));
            }
            style(e, props);
        });
    }

    /** A link styled as a button: a background colour and padding or display, or a {@code btn}/{@code button} class. */
    private static boolean isButton(Element a) {
        if (a.attr("href").isBlank() || a.text().isBlank() || !a.children().isEmpty()) {
            return false;
        }
        var style = a.attr("style").toLowerCase();
        return BUTTON_CLASS.matcher(a.className()).find() || BACKGROUND.matcher(style).find() && (style.contains("padding") || style.contains("display"));
    }

    private void button(Element a, Element container) {
        var style = a.attr("style");
        add("button", props -> {
            props.put("text", a.text());
            props.put("href", a.attr("href"));
            var background = BACKGROUND.matcher(style);
            props.put("backgroundColor", background.find() ? background.group(1) : "#1a73e8");
            var color = COLOR.matcher(style);
            props.put("color", color.find() ? color.group(1) : "#ffffff");
            var radius = RADIUS.matcher(style);
            if (radius.find()) {
                props.put("borderRadius", Integer.parseInt(radius.group(1)));
            }
            var size = FONT_SIZE.matcher(style);
            if (size.find()) {
                props.put("fontSize", Integer.parseInt(size.group(1)));
            }
            props.put("fullWidth", FULL_WIDTH.matcher(style).find());
            props.put("align", "left");
            style(container != null ? container : a, props);
        });
    }

    private void image(Element img, String href) {
        var src = img.attr("src");
        if (src.isBlank()) {
            html(img);
            return;
        }
        add("image", props -> {
            props.put("src", src);
            props.put("alt", img.attr("alt"));
            if (href != null && !href.isBlank()) {
                props.put("href", href);
            }
            if (img.attr("width").matches("\\d{1,4}")) {
                props.put("width", Integer.parseInt(img.attr("width")));
            }
            style(img.parent() != null && "p".equals(img.parent().normalName()) ? img.parent() : img, props);
        });
    }

    private void list(Element e) {
        if (hasControl(e)) {
            html(e);
            return;
        }
        var items = new ArrayList<String>();
        for (var li : e.children()) {
            if (!"li".equals(li.normalName()) || !li.children().isEmpty() || li.text().isBlank()) {
                html(e);
                return;
            }
            items.add(li.text());
        }
        if (items.isEmpty()) {
            html(e);
            return;
        }
        add("list", props -> {
            props.put("items", String.join("\n", items));
            props.put("ordered", "ol".equals(e.normalName()));
            style(e, props);
        });
    }

    /**
     * A table of one row with two or more cells is a layout in columns: each cell becomes a column, with its content
     * converted by the same rules. A table of several rows of plain text cells is a Table block. Any other table (spans,
     * formatting in the cells, tables inside, control tags between its rows) stays an HTML block.
     */
    private void table(Element table) {
        if (hasControl(table)) {
            html(table);
            return;
        }
        var cells = columnCells(table);
        if (cells == null) {
            var rows = dataRows(table);
            if (rows == null) {
                html(table);
                return;
            }
            var header = !table.select("thead").isEmpty() || rows.get(0).stream().allMatch(c -> "th".equals(c.normalName()));
            add("table", props -> {
                var array = props.putArray("rows");
                rows.forEach(row -> {
                    var out = array.addArray();
                    row.forEach(c -> out.add(c.text()));
                });
                props.put("header", header);
            });
            return;
        }
        flushHtml();
        var columns = target().addObject();
        columns.put("id", "columns-" + (++counter[0]));
        columns.put("type", "columns");
        var props = columns.putObject("props");
        props.put("stackOnMobile", true);
        props.put("padding", "0px");
        var children = columns.putArray("children");
        var widths = widths(cells);
        for (int i = 0; i < cells.size(); i++) {
            var column = children.addObject();
            column.put("id", "column-" + (++counter[0]));
            column.put("type", "column");
            column.putObject("props").put("width", widths[i]);
            var inner = new HtmlToBlocks(counter, stats);
            cells.get(i).childNodes().forEach(inner::node);
            inner.finish();
            column.set("children", inner.blocks);
        }
    }

    /** The rows of a data table the editor can hold: two or more rows of plain text cells, no spans, no tables inside. */
    private static List<List<Element>> dataRows(Element table) {
        if (table.getElementsByTag("table").size() != 1) {
            return null;
        }
        var rows = new ArrayList<List<Element>>();
        for (var tr : table.select("tr")) {
            var cells = new ArrayList<Element>(tr.children());
            if (cells.isEmpty() || cells.size() > 8
                    || cells.stream().anyMatch(c -> !("td".equals(c.normalName()) || "th".equals(c.normalName())) || !c.children().isEmpty()
                    || c.hasAttr("colspan") || c.hasAttr("rowspan"))) {
                return null;
            }
            rows.add(cells);
        }
        return rows.size() >= 2 && rows.size() <= 50 ? rows : null;
    }

    private static List<Element> columnCells(Element table) {
        if (table.getElementsByTag("table").size() != 1) {
            return null; // tables inside
        }
        var rows = table.select("tr");
        if (rows.size() != 1) {
            return null;
        }
        var cells = rows.first().children();
        if (cells.size() < 2 || cells.stream().anyMatch(c -> !"td".equals(c.normalName()) && !"th".equals(c.normalName()))) {
            return null;
        }
        return new ArrayList<>(cells);
    }

    /** The widths in percent: the ones the cells declare when all of them do, otherwise an equal share. */
    private static int[] widths(List<Element> cells) {
        var widths = new int[cells.size()];
        var declared = true;
        for (int i = 0; i < widths.length; i++) {
            var m = Pattern.compile("(\\d{1,3})%").matcher(cells.get(i).attr("width") + " " + cells.get(i).attr("style"));
            if (m.find() && Integer.parseInt(m.group(1)) >= 5 && Integer.parseInt(m.group(1)) <= 100) {
                widths[i] = Integer.parseInt(m.group(1));
            } else {
                declared = false;
            }
        }
        if (!declared) {
            Arrays.fill(widths, Math.max(5, 100 / widths.length));
        }
        return widths;
    }

    private void html(Element e) {
        flushInline();
        pendingHtml.append(e.outerHtml());
    }

    // ------------------------------------------------------------ output

    private void style(Element e, ObjectNode props) {
        var style = e.attr("style");
        var align = ALIGN.matcher(style);
        if (align.find()) {
            props.put("align", align.group(1).toLowerCase());
        } else if (e.attr("align").matches("(?i)left|center|right")) {
            props.put("align", e.attr("align").toLowerCase());
        }
        var color = COLOR.matcher(style);
        if (color.find()) {
            props.put("color", color.group(1));
        }
    }

    private void add(String type, Consumer<ObjectNode> fill) {
        flushHtml();
        var block = target().addObject();
        stats[0]++;
        block.put("id", type + "-" + (++counter[0]));
        block.put("type", type);
        fill.accept(block.putObject("props"));
    }

    private void flushInline() {
        if (pendingInline.length() == 0) {
            return;
        }
        var html = restore("<p>" + pendingInline.toString().trim() + "</p>");
        pendingInline.setLength(0);
        add("text", props -> props.put("html", html));
    }

    private void flushHtml() {
        flushInline();
        emitHtml();
    }

    private void emitHtml() {
        if (pendingHtml.length() == 0) {
            return;
        }
        var html = restore(pendingHtml.toString());
        pendingHtml.setLength(0);
        var block = target().addObject();
        stats[0]++;
        stats[1]++;
        block.put("id", "html-" + (++counter[0]));
        block.put("type", "html");
        var props = block.putObject("props");
        props.put("html", html);
        props.put("fullDocument", false);
    }
}
