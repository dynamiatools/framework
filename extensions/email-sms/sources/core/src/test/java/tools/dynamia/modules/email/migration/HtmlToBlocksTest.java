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
        var result = HtmlToBlocks.convert("<h1>Hi</h1><table><tr><td colspan=\"2\">A</td></tr><tr><td>B</td></tr></table><div class=\"x\"><span>y</span></div><p>Bye</p>");
        var b = blocks(result);

        assertEquals(3, result.blocks());
        assertEquals(1, result.htmlBlocks());
        assertEquals("html", b.get(1).path("type").asString());
        assertTrue(b.get(1).path("props").path("html").asString().startsWith("<table><tbody><tr><td colspan=\"2\">A</td></tr><tr><td>B</td></tr></tbody></table>"));
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

    @Test
    void aTableOfOneRowBecomesColumnsWhoseCellsAreConvertedToo() {
        var result = HtmlToBlocks.convert("<h1>Hi</h1><table><tr><td><h3>Help</h3><p>Call us</p></td><td><img src=\"https://x.com/a.png\"></td></tr></table><p>Bye</p>");
        var b = blocks(result);
        var columns = b.get(1);

        assertEquals(3, b.size());
        assertEquals("columns", columns.path("type").asString());
        assertEquals(2, columns.path("children").size());
        assertEquals("column", columns.path("children").get(0).path("type").asString());
        assertEquals(50, columns.path("children").get(0).path("props").path("width").asInt());
        assertEquals("heading", columns.path("children").get(0).path("children").get(0).path("type").asString());
        assertEquals("text", columns.path("children").get(0).path("children").get(1).path("type").asString());
        assertEquals("image", columns.path("children").get(1).path("children").get(0).path("type").asString());
        assertEquals(5, result.blocks());
        assertEquals(0, result.htmlBlocks());
        var ids = new java.util.HashSet<String>();
        new JsonMapper().readTree(result.design()).findValues("id").forEach(n -> assertTrue(ids.add(n.asString()), "duplicated id " + n));
    }

    @Test
    void declaredPercentagesAreKeptAndOtherTablesStayHtml() {
        var b = blocks(HtmlToBlocks.convert("<p>x</p><table><tr><td width=\"30%\">A</td><td style=\"width:70%\">B</td></tr></table>"));
        assertEquals(30, b.get(1).path("children").get(0).path("props").path("width").asInt());
        assertEquals(70, b.get(1).path("children").get(1).path("props").path("width").asInt());

        var nested = blocks(HtmlToBlocks.convert("<p>x</p><table><tr><td><table><tr><td>a</td></tr></table></td><td>B</td></tr></table>"));
        assertEquals("html", nested.get(1).path("type").asString());
    }

    @Test
    void aTableOfSeveralRowsOfPlainCellsBecomesATableBlock() {
        var result = HtmlToBlocks.convert("<p>Lines</p><table><thead><tr><th>Item</th><th>Price</th></tr></thead><tbody><tr><td>Dune</td><td>${price}</td></tr><tr><td>Emma &amp; Co</td><td>5</td></tr></tbody></table>");
        var table = blocks(result).get(1);

        assertEquals("table", table.path("type").asString());
        assertEquals(true, table.path("props").path("header").asBoolean());
        assertEquals(3, table.path("props").path("rows").size());
        assertEquals("Item", table.path("props").path("rows").get(0).get(0).asString());
        assertEquals("${price}", table.path("props").path("rows").get(1).get(1).asString());
        assertEquals("Emma & Co", table.path("props").path("rows").get(2).get(0).asString());
        assertEquals(0, result.htmlBlocks());
    }

    @Test
    void aTableWithoutHeaderCellsHasNoHeaderAndRichOrWideTablesStayHtml() {
        var plain = blocks(HtmlToBlocks.convert("<p>x</p><table><tr><td>a</td></tr><tr><td>b</td></tr></table>")).get(1);
        assertEquals("table", plain.path("type").asString());
        assertEquals(false, plain.path("props").path("header").asBoolean(true));

        assertEquals("html", blocks(HtmlToBlocks.convert("<p>x</p><table><tr><td><b>a</b></td></tr><tr><td>b</td></tr></table>")).get(1).path("type").asString());
        var wide = "<p>x</p><table><tr>" + "<td>c</td>".repeat(9) + "</tr><tr>" + "<td>c</td>".repeat(9) + "</tr></table>";
        assertEquals("html", blocks(HtmlToBlocks.convert(wide)).get(1).path("type").asString());
    }
}
