package tools.dynamia.templates;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateBlocksTest {

    private final DynamiaTemplateEngine engine = new DynamiaTemplateEngine();

    public record Line(String name, int qty) {
    }

    @Test
    void ifPicksTheThenOrElseBranch() {
        var t = "{{#if paid}}PAID{{else}}DUE{{/if}}";
        assertEquals("PAID", engine.evaluate(t, Map.of("paid", true)));
        assertEquals("DUE", engine.evaluate(t, Map.of("paid", false)));
        assertEquals("DUE", engine.evaluate(t, Map.of()));
    }

    @Test
    void ifWithoutElseAndWithOperators() {
        var t = "[{{#if total > 100 && vip}}big{{/if}}]";
        assertEquals("[big]", engine.evaluate(t, Map.of("total", 150, "vip", true)));
        assertEquals("[]", engine.evaluate(t, Map.of("total", 50, "vip", true)));
    }

    @Test
    void eachRepeatsWithTheAliasAndLoopVariables() {
        var data = Map.<String, Object>of("items", List.of(new Line("a", 1), new Line("b", 2)));
        assertEquals("1/2 a x1;2/2 b x2;",
                engine.evaluate("{{#each items as i}}${loop.number}/${loop.count} ${i.name} x{{i.qty}};{{/each}}", data));
        assertEquals("a,b", engine.evaluate(
                "{{#each items as i}}${i.name}{{#if !loop.last}},{{/if}}{{/each}}", data));
    }

    @Test
    void blocksNestAndEachScopesDoNotLeak() {
        var data = new HashMap<String, Object>();
        data.put("rows", List.of(Map.of("cells", List.of("a", "b")), Map.of("cells", List.of("c"))));
        assertEquals("<a|b|><c|>", engine.evaluate(
                "{{#each rows as r}}<{{#each r.cells as c}}${c}|{{/each}}>{{/each}}", data));
        assertEquals("${c}", engine.evaluate("${c}", data));
    }

    @Test
    void missingNullOrEmptyListsRenderNothing() {
        var t = "[{{#each items as i}}x{{/each}}]";
        assertEquals("[]", engine.evaluate(t, Map.of()));
        assertEquals("[]", engine.evaluate(t, Map.of("items", List.of())));
        assertEquals("[xx]", engine.evaluate(t, Map.of("items", new String[]{"a", "b"})));
    }

    @Test
    void unbalancedOrMalformedTagsLeaveTheTextUntouched() {
        for (var t : List.of("{{#if a}}x", "x{{/if}}", "{{#each items}}x{{/each}}", "{{#if}}x{{/if}}",
                "{{#if a}}x{{/each}}", "{{else}}", "{{#if a}}1{{else}}2{{else}}3{{/if}}")) {
            assertEquals(t, engine.evaluate(t, Map.of("a", true, "items", List.of(1))), t);
        }
    }

    @Test
    void valuesCannotInjectBlockTags() {
        assertEquals("{{#if x}}boom{{/if}}", engine.evaluate("${v}", Map.of("v", "{{#if x}}boom{{/if}}", "x", true)));
        assertEquals("{{#if x}}boom{{/if}}!", engine.evaluate("{{#if x}}${v}{{/if}}!",
                Map.of("v", "{{#if x}}boom{{/if}}", "x", true)));
    }

    @Test
    void iterationsAndDepthAreCapped() {
        var big = new ArrayList<Integer>();
        for (int i = 0; i < 200; i++) {
            big.add(i);
        }
        var out = engine.evaluate("{{#each a as x}}{{#each a as y}}.{{/each}}{{/each}}", Map.of("a", big));
        assertTrue(out.length() <= TemplateBlocks.MAX_ITERATIONS, "output " + out.length());

        var deep = "{{#if a}}".repeat(TemplateBlocks.MAX_DEPTH + 1) + "x" + "{{/if}}".repeat(TemplateBlocks.MAX_DEPTH + 1);
        assertEquals(deep, engine.evaluate(deep, Map.of("a", true)));
    }

    @Test
    void filtersAndTernariesKeepWorkingInsideBlocks() {
        assertEquals("ADA!", engine.evaluate("{{#if name}}${name|upper}{{/if}}${ok ? '!' : '?'}",
                Map.of("name", "Ada", "ok", true)));
    }
}
