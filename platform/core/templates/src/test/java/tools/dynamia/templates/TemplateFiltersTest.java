package tools.dynamia.templates;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateFiltersTest {

    private final DynamiaTemplateEngine engine = new DynamiaTemplateEngine();

    private String eval(String template, Map<String, Object> data) {
        return engine.evaluate(template, data);
    }

    private static final Map<String, Object> DATA = Map.of(
            "tags", List.of("red", "green", "blue"),
            "people", List.of(Map.of("name", "Ana", "age", 31), Map.of("name", "Luis", "age", 28)),
            "none", List.of(),
            "prices", List.of(new BigDecimal("1.50"), new BigDecimal("2")),
            "word", "plain");

    // ---- the list filters

    @Test
    void listJoinsWithCommas() {
        assertEquals("red, green, blue", eval("${tags|list}", DATA));
        assertEquals("red, green, blue", eval("{{ tags | list }}", DATA));
    }

    @Test
    void anItemPropertyCanBePrinted() {
        assertEquals("Ana, Luis", eval("{{people|list:name}}", DATA));
        assertEquals("31, 28", eval("{{people|list:age}}", DATA));
    }

    @Test
    void listDotsMakesABulletedList() {
        assertEquals("• red\n• green\n• blue", eval("{{tags|list_dots}}", DATA));
        assertEquals("• Ana\n• Luis", eval("{{people|list_dots:name}}", DATA));
    }

    @Test
    void listNumberMakesANumberedList() {
        assertEquals("1. red\n2. green\n3. blue", eval("{{tags|list_number}}", DATA));
        assertEquals("1. Ana\n2. Luis", eval("{{people|list_number:name}}", DATA));
    }

    @Test
    void joinUsesTheSeparatorAndKeepsLineBreaks() {
        assertEquals("red-green-blue", eval("{{tags|join:-}}", DATA));
        assertEquals("red\ngreen\nblue", eval("{{tags|join:\\n}}", DATA));
        assertEquals("red, green, blue", eval("{{tags|join}}", DATA));
    }

    @Test
    void numbersPrintWithoutExtraZeros() {
        assertEquals("1.5, 2", eval("{{prices|list}}", DATA));
    }

    @Test
    void emptyMissingAndNonListValues() {
        assertEquals("", eval("{{none|list_dots}}", DATA));
        assertEquals("", eval("[{{none|list}}]".replace("[", "").replace("]", ""), DATA));
        assertEquals("plain", eval("{{word|list}}", DATA), "a value that is not a list comes out as it is");
        var withNull = new HashMap<String, Object>();
        withNull.put("tags", null);
        assertEquals("", eval("{{tags|list}}", withNull));
        assertEquals("{{missing|list}}", eval("{{missing|list}}", DATA), "an unknown variable stays as it was");
    }

    @Test
    void itemsWithoutTheRequestedPropertyAreSkipped() {
        var data = Map.<String, Object>of("rows", List.of(Map.of("a", "x"), Map.of("b", "y"), Map.of("a", "z")));
        assertEquals("x, z", eval("{{rows|list:a}}", data));
    }

    @Test
    void filtersChain() {
        assertEquals("RED, GREEN, BLUE", eval("{{tags|list|upper}}", DATA));
        assertEquals("3", eval("{{tags|count}}", DATA));
    }

    // ---- extending

    @Test
    void aFilterListedAsAServiceIsFound() {
        assertEquals("HELLO!", eval("{{word2|shout}}", Map.of("word2", "hello")));
        assertEquals("HELLO?", eval("{{word2|shout:?}}", Map.of("word2", "hello")));
        assertTrue(TemplateFilters.names().contains("shout"));
    }

    @Test
    void aRegisteredFilterWorksAndCanBeRemoved() {
        TemplateFilters.register(new TemplateFilter() {
            @Override
            public String name() {
                return "reverse_text";
            }

            @Override
            public Object apply(Object value, String argument, Map<String, Object> variables) {
                return new StringBuilder(String.valueOf(value)).reverse().toString();
            }
        });
        try {
            assertEquals("cba", eval("{{x|reverse_text}}", Map.of("x", "abc")));
        } finally {
            TemplateFilters.unregister("reverse_text");
        }
        assertEquals("abc", eval("{{x|reverse_text}}", Map.of("x", "abc")), "an unknown filter lets the value through");
    }

    @Test
    void builtInAndDuplicateNamesAreRefused() {
        assertThrows(IllegalArgumentException.class, () -> TemplateFilters.register(named("upper")));
        assertThrows(IllegalArgumentException.class, () -> TemplateFilters.register(named("list")));
        assertThrows(IllegalArgumentException.class, () -> TemplateFilters.register(named("9bad")));
        assertThrows(IllegalArgumentException.class, () -> TemplateFilters.register(named("a b")));
    }

    @Test
    void aFilterThatFailsPrintsTheValue() {
        TemplateFilters.register(new TemplateFilter() {
            @Override
            public String name() {
                return "explodes";
            }

            @Override
            public Object apply(Object value, String argument, Map<String, Object> variables) {
                throw new IllegalStateException("boom");
            }
        });
        try {
            assertEquals("abc", eval("{{x|explodes}}", Map.of("x", "abc")));
        } finally {
            TemplateFilters.unregister("explodes");
        }
    }

    private static TemplateFilter named(String name) {
        return new TemplateFilter() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public Object apply(Object value, String argument, Map<String, Object> variables) {
                return value;
            }
        };
    }
}
