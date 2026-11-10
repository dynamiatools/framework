package tools.dynamia.modules.email.migration;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Turns the HTML of a migrated template into the blocks of the email editor, doing its best: headings (h1 to h3), paragraphs,
 * images, dividers and simple lists become their own block, and anything else is kept as an HTML block, so the email still
 * looks the same. The whole template stays one HTML block when it has {@code {{#each}}} or {@code {{#if}}} (they would be
 * split across blocks) or a {@code <style>} (it would be lost).
 */
final class HtmlToBlocks {

    private static final JsonMapper JSON = new JsonMapper();
    private static final Set<String> INLINE = Set.of("a", "b", "strong", "i", "em", "u", "span", "br", "small", "sup", "sub", "font", "s");
    private static final Pattern ALIGN = Pattern.compile("text-align\\s*:\\s*(left|center|right)", Pattern.CASE_INSENSITIVE);
    private static final Pattern COLOR = Pattern.compile("(?<![-\\w])color\\s*:\\s*(#[0-9a-fA-F]{3,8})", Pattern.CASE_INSENSITIVE);

    /**
     * @param design     the editor document (JSON)
     * @param blocks     how many blocks it has
     * @param htmlBlocks how many of them are kept as HTML
     */
    record Result(String design, int blocks, int htmlBlocks) {
    }

    private final ArrayNode blocks = JSON.createArrayNode();
    private int htmlBlocks;
    private int counter;
    private final StringBuilder pendingHtml = new StringBuilder();
    private final StringBuilder pendingInline = new StringBuilder();

    private HtmlToBlocks() {
    }

    static Result convert(String html) {
        if (html == null || html.isBlank()) {
            return single(html);
        }
        var lower = html.toLowerCase();
        if (lower.contains("{{#") || lower.contains("<style")) {
            return single(html);
        }
        var converter = new HtmlToBlocks();
        var document = Jsoup.parseBodyFragment(html);
        document.outputSettings().prettyPrint(false); // keep the HTML as written, no new line breaks
        var body = document.body();
        body.childNodes().forEach(converter::node);
        converter.flush();
        if (converter.blocks.isEmpty() || converter.blocks.size() == converter.htmlBlocks) {
            return single(html);
        }
        return new Result(converter.document(), converter.blocks.size(), converter.htmlBlocks);
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

    private String document() {
        var root = JSON.createObjectNode();
        root.put("version", 1);
        root.set("blocks", blocks);
        return JSON.writeValueAsString(root);
    }

    // ------------------------------------------------------------ nodes

    private void node(Node node) {
        if (node instanceof Comment) {
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
        var tag = e.normalName();
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
            case "img" -> image(e, null);
            case "hr" -> add("divider", props -> {
            });
            case "ul", "ol" -> list(e);
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

    private void heading(Element e) {
        if (!e.children().isEmpty()) {
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
        var single = e.children().size() == 1 && e.ownText().isBlank() ? e.child(0) : null;
        if (single != null && "img".equals(single.normalName())) {
            image(single, null);
            return;
        }
        if (single != null && "a".equals(single.normalName()) && single.children().size() == 1 && "img".equals(single.child(0).normalName())
                && single.ownText().isBlank()) {
            image(single.child(0), single.attr("href"));
            return;
        }
        add("text", props -> {
            props.put("html", "<p>" + e.html() + "</p>");
            style(e, props);
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

    private void add(String type, java.util.function.Consumer<ObjectNode> fill) {
        flushHtml();
        var block = blocks.addObject();
        block.put("id", type + "-" + (++counter));
        block.put("type", type);
        fill.accept(block.putObject("props"));
    }

    private void flushInline() {
        if (pendingInline.length() == 0) {
            return;
        }
        var html = "<p>" + pendingInline.toString().trim() + "</p>";
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
        var html = pendingHtml.toString();
        pendingHtml.setLength(0);
        var block = blocks.addObject();
        block.put("id", "html-" + (++counter));
        block.put("type", "html");
        var props = block.putObject("props");
        props.put("html", html);
        props.put("fullDocument", false);
        htmlBlocks++;
    }

    private void flush() {
        flushHtml();
    }
}
