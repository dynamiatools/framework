package tools.dynamia.modules.email.migration;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlToBlocksTest {

    private static JsonNode blocks(HtmlToBlocks.Result result) {
        return new JsonMapper().readTree(result.design()).path("blocks");
    }

    @Test
    void simpleHtmlBecomesOneBlockPerElement() {
        var result = HtmlToBlocks.convert("<h1>Hola ${user.name}</h1><p>Gracias por <b>comprar</b>.</p><img src=\"https://x.com/a.png\" alt=\"Logo\" width=\"200\"><hr><h3>Detalle</h3>"
                + "<ul><li>Uno</li><li>Dos</li></ul>");
        var b = blocks(result);

        assertEquals(6, result.blocks());
        assertEquals(0, result.htmlBlocks());
        assertEquals("heading", b.get(0).path("type").asString());
        assertEquals("Hola ${user.name}", b.get(0).path("props").path("text").asString());
        assertEquals("h1", b.get(0).path("props").path("level").asString());
        assertEquals("text", b.get(1).path("type").asString());
        assertEquals("<p>Gracias por <b>comprar</b>.</p>", b.get(1).path("props").path("html").asString());
        assertEquals("image", b.get(2).path("type").asString());
        assertEquals("https://x.com/a.png", b.get(2).path("props").path("src").asString());
        assertEquals(200, b.get(2).path("props").path("width").asInt());
        assertEquals("divider", b.get(3).path("type").asString());
        assertEquals("h3", b.get(4).path("props").path("level").asString());
        assertEquals("list", b.get(5).path("type").asString());
        assertEquals("Uno\nDos", b.get(5).path("props").path("items").asString());
        assertEquals(false, b.get(5).path("props").path("ordered").asBoolean(true));
    }

    @Test
    void alignmentAndColourAreRead() {
        var b = blocks(HtmlToBlocks.convert("<h2 style=\"text-align:center;color:#ff0000\">Hi</h2><p align=\"right\">x</p>"));
        assertEquals("center", b.get(0).path("props").path("align").asString());
        assertEquals("#ff0000", b.get(0).path("props").path("color").asString());
        assertEquals("right", b.get(1).path("props").path("align").asString());
    }

    @Test
    void whatHasNoBlockStaysHtmlAndNeighboursAreMerged() {
        var result = HtmlToBlocks.convert("<h1>Hi</h1><table><tr><td>A</td><td>B</td></tr></table><div class=\"x\"><span>y</span></div><p>Bye</p>");
        var b = blocks(result);

        assertEquals(3, result.blocks());
        assertEquals(1, result.htmlBlocks());
        assertEquals("html", b.get(1).path("type").asString());
        assertTrue(b.get(1).path("props").path("html").asString().startsWith("<table><tbody><tr><td>A</td><td>B</td></tr></tbody></table>"));
        assertTrue(b.get(1).path("props").path("html").asString().contains("class=\"x\""));
        assertEquals("text", b.get(2).path("type").asString());
    }

    @Test
    void aHeadingWithFormattingAndAnImageInALinkKeepTheirMeaning() {
        var b = blocks(HtmlToBlocks.convert("<h1>Hi <b>you</b></h1><p><a href=\"https://x.com\"><img src=\"https://x.com/b.png\"></a></p>"));
        assertEquals("html", b.get(0).path("type").asString());
        assertEquals("image", b.get(1).path("type").asString());
        assertEquals("https://x.com", b.get(1).path("props").path("href").asString());
    }

    @Test
    void looseTextAndInlineElementsBecomeOneParagraph() {
        var b = blocks(HtmlToBlocks.convert("Hola <b>mundo</b> y <a href=\"https://x.com\">enlace</a><h2>T</h2>"));
        assertEquals("text", b.get(0).path("type").asString());
        assertEquals("<p>Hola <b>mundo</b> y <a href=\"https://x.com\">enlace</a></p>", b.get(0).path("props").path("html").asString());
    }

    @Test
    void controlTagsStylesAndHtmlOnlyTemplatesStayOneHtmlBlock() {
        for (var html : new String[]{"<h1>Hi</h1>{{#each items as i}}<p>${i}</p>{{/each}}", "<style>p{color:red}</style><p>Hi</p>", "<table><tr><td>x</td></tr></table>", ""}) {
            var result = HtmlToBlocks.convert(html);
            assertEquals(1, result.blocks(), html);
            assertEquals("html", blocks(result).get(0).path("type").asString(), html);
            assertEquals(html, blocks(result).get(0).path("props").path("html").asString(), html);
        }
    }
}
