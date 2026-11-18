package tools.dynamia.templates;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DynamiaTemplateEngineTest {

    private final DynamiaTemplateEngine engine = new DynamiaTemplateEngine();

    @Test
    void replacesDollarAndMustacheStyles() {
        var out = engine.evaluate("A ${name} B {{name}}", Map.of("name", "X"));
        assertEquals("A X B X", out);
    }

    @Test
    void nullValueBecomesEmptyString() {
        var vars = new HashMap<String, Object>();
        vars.put("name", null);
        assertEquals("[]", engine.evaluate("[${name}]", vars));
    }

    @Test
    void unknownKeysAreLeftUntouched() {
        assertEquals("${other}", engine.evaluate("${other}", Map.of("name", "X")));
    }

    @Test
    void nullTemplateGivesEmptyString() {
        assertEquals("", engine.evaluate(null, Map.of()));
    }

    @Test
    void valuesUseStringValueOf() {
        assertEquals("1500", engine.evaluate("${n}", Map.of("n", 1500)));
    }

    @Test
    void bareDollarWithoutBracesIsNotReplaced() {
        // characterization: "$total" (no braces) is not substituted by the engine
        assertEquals("$total", engine.evaluate("$total", Map.of("total", 10)));
    }

    // ---- property paths (issue: loop variable properties) ----

    public record Product(String name, int qty) {
    }

    public static class Bean {
        public String getTitle() {
            return "Mr";
        }

        public boolean isVip() {
            return true;
        }
    }

    @Test
    void resolvesPathsOnBeansMapsAndRecords() {
        var vars = new HashMap<String, Object>();
        vars.put("b", new Bean());
        vars.put("m", Map.of("city", "Cali"));
        vars.put("p", new Product("Cafe", 2));

        assertEquals("Mr", engine.evaluate("${b.title}", vars));
        assertEquals("true", engine.evaluate("${b.vip}", vars));
        assertEquals("Cali", engine.evaluate("${m.city}", vars));
        assertEquals("Cafe x2", engine.evaluate("${p.name} x${p.qty}", vars));
    }

    @Test
    void shortFormNeedsADefinedVariable() {
        var vars = Map.<String, Object>of("d", new Product("Pan", 1));
        assertEquals("Pan", engine.evaluate("$d.name", vars));
        assertEquals("$x.name", engine.evaluate("$x.name", vars));
        assertEquals("Price: $5.00", engine.evaluate("Price: $5.00", vars));
    }

    @Test
    void missingPropertyOfAKnownVariableIsEmptyAndClassIsNotReachable() {
        var vars = Map.<String, Object>of("d", new Product("Pan", 1));
        assertEquals("[]", engine.evaluate("[${d.nope}]", vars));
        assertEquals("[]", engine.evaluate("[${d.class}]", vars));
        assertEquals("[]", engine.evaluate("[${d.class.name}]", vars));
    }

    // ---- filters ----

    private static Map<String, Object> es() {
        var vars = new HashMap<String, Object>();
        vars.put("locale", "es-CO");
        vars.put("total", 1234567.891);
        vars.put("name", "  Maria  ");
        vars.put("empty", "");
        vars.put("day", java.time.LocalDate.of(2026, 10, 31));
        vars.put("moment", java.time.LocalDateTime.of(2026, 10, 31, 14, 5));
        return vars;
    }

    @Test
    void numberAndCurrencyFiltersUseTheTemplateLocale() {
        assertEquals("1.234.567,89", engine.evaluate("${total|number:#,##0.00}", es()));
        assertEquals("1.234.567,89", engine.evaluate("${total|currency}", es()));
        assertEquals("1.234.568", engine.evaluate("${total|currency:0}", es()));

        var us = es();
        us.put("locale", "en-US");
        assertEquals("1,234,567.89", engine.evaluate("${total|currency}", us));
    }

    @Test
    void textFilters() {
        assertEquals("MARIA", engine.evaluate("${name|trim|upper}", es()));
        assertEquals("maria", engine.evaluate("${name|trim|lower}", es()));
        assertEquals("-", engine.evaluate("${empty|default:-}", es()));
        assertEquals("  Maria  ", engine.evaluate("${name|default:-}", es()));
    }

    @Test
    void dateFilter() {
        assertEquals("31/10/2026", engine.evaluate("${day|date}", es()));
        assertEquals("2026-10-31 14:05", engine.evaluate("${moment|date:yyyy-MM-dd HH:mm}", es()));
    }

    @Test
    void badFilterInputIsShownAsIs() {
        var vars = new HashMap<String, Object>();
        vars.put("x", "abc");
        assertEquals("abc", engine.evaluate("${x|number:#.00}", vars));
        assertEquals("abc", engine.evaluate("${x|nofilter}", vars));
        assertEquals("abc", engine.evaluate("${x|date}", vars));
    }

    @Test
    void filterOnNullGivesEmpty() {
        var vars = new HashMap<String, Object>();
        vars.put("x", null);
        assertEquals("[]", engine.evaluate("[${x|currency}]", vars));
        assertEquals("[-]", engine.evaluate("[${x|default:-}]", vars));
    }

    @Test
    void valuesAreNeverReinterpreted() {
        // a value that looks like a template expression must not be expanded (no injection through data)
        var vars = new HashMap<String, Object>();
        vars.put("note", "${secret}");
        vars.put("secret", "LEAK");
        assertEquals("${secret}", engine.evaluate("${note}", vars));
        vars.put("note", "$other.name");
        vars.put("other", Map.of("name", "LEAK"));
        assertEquals("$other.name", engine.evaluate("${note}", vars));
    }

    // ---- flat keys and reflection limits (review findings) ----

    @Test
    void variableNamesWithADotStillResolveAsFlatKeys() {
        var vars = new HashMap<String, Object>();
        vars.put("empresa.nombre", "FLAT");
        assertEquals("FLAT", engine.evaluate("${empresa.nombre}", vars));
        assertEquals("FLAT", engine.evaluate("$empresa.nombre", vars));
        // the flat key wins over a nested one, as it always did
        vars.put("empresa", Map.of("nombre", "NESTED"));
        assertEquals("FLAT", engine.evaluate("${empresa.nombre}", vars));
    }

    public enum Color {
        RED
    }

    public static class Holder {
        public ClassLoader getLoader() {
            return Holder.class.getClassLoader();
        }

        public java.io.File getFile() {
            return new java.io.File("/etc/passwd");
        }

        public Thread getThread() {
            return Thread.currentThread();
        }

        public Object getSneaky() {
            return Holder.class; // declared as Object, but the value is a Class
        }

        public String getName() {
            return "ok";
        }
    }

    @Test
    void templatesCannotWalkIntoReflectionOrTheFileSystem() {
        var vars = new HashMap<String, Object>();
        vars.put("c", Color.RED);
        vars.put("h", new Holder());
        vars.put("t", Thread.currentThread());
        vars.put("m", Map.of("cls", Holder.class));

        assertEquals("[]", engine.evaluate("[${c.declaringClass}]", vars));
        assertEquals("[]", engine.evaluate("[${c.declaringClass.classLoader}]", vars));
        assertEquals("[]", engine.evaluate("[${h.loader}]", vars));
        assertEquals("[]", engine.evaluate("[${h.loader.parent}]", vars));
        assertEquals("[]", engine.evaluate("[${h.file}]", vars));
        assertEquals("[]", engine.evaluate("[${h.file.absolutePath}]", vars));
        assertEquals("[]", engine.evaluate("[${h.thread}]", vars));
        assertEquals("[]", engine.evaluate("[${h.sneaky}]", vars));
        assertEquals("[]", engine.evaluate("[${t.name}]", vars), "a Thread is never walked");
        assertEquals("[]", engine.evaluate("[${m.cls}]", vars));
        assertEquals("[]", engine.evaluate("[${m.cls.name}]", vars));
        assertEquals("ok", engine.evaluate("${h.name}", vars), "ordinary getters still work");
    }

    @Test
    void mustacheAndDollarStylesAreInterchangeable() {
        var data = Map.<String, Object>of("qty", 3, "price", 2, "paid", true, "total", 1234.5,
                "items", java.util.List.of(Map.of("n", 1), Map.of("n", 2)));
        for (var open : new String[][]{{"${", "}"}, {"{{", "}}"}}) {
            String o = open[0], c = open[1];
            assertEquals("6", engine.evaluate(o + "qty * price" + c, data));
            assertEquals("6", engine.evaluate(o + " qty * price " + c, data), "spaces inside are ignored");
            assertEquals("PAID", engine.evaluate(o + "paid ? 'PAID' : 'DUE'" + c, data));
            assertEquals("3", engine.evaluate(o + "items|sum:n" + c, data));
            assertEquals("2", engine.evaluate(o + " items | count " + c, data));
        }
        assertEquals("1.234,50", engine.evaluate("{{total|number:#,##0.00}}", Map.<String, Object>of("total", 1234.5, "locale", "es")));
    }

    @Test
    void unwrapTakesTheNameOutOfEitherStyle() {
        assertEquals("items", TemplateExpressions.unwrap("${items}"));
        assertEquals("items", TemplateExpressions.unwrap("{{ items }}"));
        assertEquals("items", TemplateExpressions.unwrap("${ items }"));
        assertEquals("plain", TemplateExpressions.unwrap("plain"));
        assertEquals("{{x", TemplateExpressions.unwrap("{{x"));
    }

    @Test
    void aDollarSignRightBeforeAMustacheVariableIsACurrencySymbol() {
        assertEquals("$1,234.5", engine.evaluate("${{total|number:#,##0.##}}", Map.of("total", 1234.5, "locale", "en")));
        assertEquals("$ 5", engine.evaluate("$ {{n}}", Map.of("n", 5)));
        assertEquals("5", engine.evaluate("${n}", Map.of("n", 5)));
    }

    @Test
    void theDateFilterFormatsIsoStringsToo() {
        var engine = new DynamiaTemplateEngine();
        var vars = new java.util.HashMap<String, Object>();
        vars.put("day", "2026-11-01");
        vars.put("moment", "2026-11-01T14:30:00");
        vars.put("instant", "2026-11-01T14:30:00Z");
        vars.put("text", "not a date");

        assertEquals("01/11/2026", engine.evaluate("{{day|date}}", vars));
        assertEquals("01-11-2026 14:30", engine.evaluate("{{moment|date:dd-MM-yyyy HH:mm}}", vars));
        assertEquals("2026", engine.evaluate("{{instant|date:yyyy}}", vars));
        // anything that is not an ISO date stays as it came
        assertEquals("not a date", engine.evaluate("{{text|date}}", vars));
    }

    @Test
    void defaultLocaleIsConfigurableAndLocaleVariableWins() {
        var es = new DynamiaTemplateEngine(java.util.Locale.forLanguageTag("es-CO"));
        var en = new DynamiaTemplateEngine(java.util.Locale.US);
        assertEquals("1.234,5", es.evaluate("${n|number:#,##0.0}", Map.of("n", 1234.5)));
        assertEquals("1,234.5", en.evaluate("${n|number:#,##0.0}", Map.of("n", 1234.5)));
        assertEquals("1,234.5", es.evaluate("${n|number:#,##0.0}", Map.of("n", 1234.5, "locale", "en-US")));
    }
}
