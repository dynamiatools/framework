package tools.dynamia.modules.email.migration;

import org.junit.jupiter.api.Test;
import tools.dynamia.templates.DynamiaTemplateEngine;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VelocityConverterTest {

    private static String ok(String velocity) {
        var result = VelocityConverter.convert(velocity);
        assertTrue(result.portable(), () -> velocity + " -> " + result.issues());
        return result.text();
    }

    private static List<String> kinds(String velocity) {
        return VelocityConverter.convert(velocity).issues().stream().map(VelocityConverter.Issue::kind).toList();
    }

    @Test
    void referencesBecomeBracedVariables() {
        assertEquals("Hi ${name}!", ok("Hi $name!"));
        assertEquals("Hi ${user.name}.", ok("Hi $user.name."));
        assertEquals("${a}${b}", ok("${a}$b"));
        assertEquals("${a.b}", ok("$!a.b"));
        assertEquals("${a.b}x", ok("$!{a.b}x"));
        assertEquals("${name}s", ok("${name}s"));
    }

    @Test
    void dollarSignsThatAreNotReferencesStayText() {
        assertEquals("Cost: $5 and $ 10", ok("Cost: $5 and $ 10"));
        assertEquals("a $ b", ok("a $ b"));
        assertEquals("color: #333; href=\"#top\" #fff", ok("color: #333; href=\"#top\" #fff"));
        assertEquals("${a}(x)", ok("$a(x)"), "a plain reference followed by a parenthesis is not a method call");
    }

    @Test
    void ifElseifElseAndEnd() {
        assertEquals("{{#if vip}}V{{/if}}", ok("#if($vip)V#end"));
        assertEquals("{{#if a}}A{{else}}B{{/if}}", ok("#if( $a )A#{else}B#end"));
        assertEquals("{{#if a}}A{{else}}{{#if b}}B{{else}}C{{/if}}{{/if}}", ok("#if($a)A#elseif($b)B#{else}C#end"));
        assertEquals("{{#if a}}A{{else}}{{#if b}}B{{else}}{{#if c}}C{{/if}}{{/if}}{{/if}}", ok("#if($a)A#elseif($b)B#elseif($c)C#end"));
    }

    @Test
    void conditionsKeepOperators() {
        assertEquals("{{#if !paid}}x{{/if}}", ok("#if(!$paid)x#end"));
        assertEquals("{{#if a && b || c}}x{{/if}}", ok("#if($a && $b || $c)x#end"));
        assertEquals("{{#if status == 'paid'}}x{{/if}}", ok("#if($status == 'paid')x#end"));
        assertEquals("{{#if total >= 100}}x{{/if}}", ok("#if($total >= 100)x#end"));
        assertEquals("{{#if order.vip != true}}x{{/if}}", ok("#if(${order.vip} != true)x#end"));
        assertEquals("{{#if x == null}}x{{/if}}", ok("#if($x == null)x#end"));
    }

    @Test
    void foreachBecomesEachWithLoopVariables() {
        assertEquals("{{#each order.lines as line}}${line.name}{{/each}}", ok("#foreach($line in $order.lines)$line.name#end"));
        assertEquals("{{#each items as i}}${loop.number}.${i}{{#if !loop.last}},{{/if}}{{/each}}",
                ok("#foreach($i in $items)$velocityCount.$i#if($foreach.hasNext),#end#end"));
        assertEquals("{{#each a as x}}${loop.index}${loop.number}{{/each}}", ok("#foreach($x in $a)$foreach.index$foreach.count#end"));
        assertEquals("{{#each a as x}}{{#if loop.first}}F{{/if}}{{#if loop.last}}L{{/if}}{{/each}}",
                ok("#foreach($x in $a)#if($foreach.first)F#end#if($foreach.last)L#end#end"));
    }

    @Test
    void theParentTemplateContentIsRaw() {
        assertEquals("<div>{{ TEMPLATE_CONTENT | raw }}</div>", ok("<div>$TEMPLATE_CONTENT</div>"));
        assertEquals("{{ TEMPLATE_CONTENT | raw }}", ok("${TEMPLATE_CONTENT}"));
        assertEquals("{{ TEMPLATE_CONTENT | raw }}", ok("$!TEMPLATE_CONTENT"));
    }

    @Test
    void commentsAreDropped() {
        assertEquals("a\nb", ok("a## note $x\nb"));
        assertEquals("ab", ok("a#* block\n $x *#b"));
    }

    @Test
    void whatCannotBeConvertedIsReportedAndCopied() {
        assertEquals(List.of("SET"), kinds("#set($x = 1)$x"));
        assertEquals(List.of("MACRO"), kinds("#mymacro($a)"));
        assertEquals(List.of("MACRO"), kinds("#macro(m $a)x#end"));
        assertEquals(List.of("PARSE"), kinds("#parse('footer.vm')"));
        assertEquals(List.of("INCLUDE"), kinds("#include('a.txt')"));
        assertEquals(List.of("EVALUATE"), kinds("#evaluate($x)"));
        assertEquals(List.of("METHOD_CALL"), kinds("$list.size()"));
        assertEquals(List.of("METHOD_CALL"), kinds("$a.b.get('k')"));
        assertEquals(List.of("INDEX"), kinds("$list[0]"));
        assertEquals(List.of("EXPRESSION"), kinds("${a.size()}"));
        assertEquals(List.of("ESCAPE"), kinds("\\$name"));
        assertEquals(List.of("BREAK"), kinds("#foreach($i in $l)#break#end"));
        assertEquals(List.of("FOREACH"), kinds("#foreach($i in [1..3])x#end"));
        assertEquals(List.of("CONDITION"), kinds("#if($a.isEmpty())x#end"));
        assertEquals(List.of("CONDITION"), kinds("#if(($a || $b) && $c)x#end"));
        assertEquals(List.of("CONDITION"), kinds("#if($a eq 'x')y#end"));
        assertEquals(List.of("CONDITION"), kinds("#if($a == \"x $b\")y#end"));
        assertEquals(List.of("FOREACH"), kinds("$foreach.parent"));
        assertEquals(List.of("UNBALANCED"), kinds("#if($a)x"));
        assertEquals(List.of("UNBALANCED"), kinds("x#end"));
        assertEquals(List.of("UNBALANCED"), kinds("#else"));
    }

    @Test
    void unconvertibleTextIsCopiedAsItWas() {
        var result = VelocityConverter.convert("a #set($x = 1) b $y");
        assertFalse(result.portable());
        assertEquals("a #set($x = 1) b ${y}", result.text());
        assertEquals(1, result.issues().get(0).line());
    }

    @Test
    void issuesCarryTheLine() {
        var result = VelocityConverter.convert("ok\n$a\n#set($x = 1)");
        assertEquals(3, result.issues().get(0).line());
        assertTrue(result.issues().get(0).snippet().startsWith("#set"));
    }

    @Test
    void nullAndBlankAreReturnedAsTheyAre() {
        assertEquals(null, VelocityConverter.convert(null).text());
        assertEquals("  ", VelocityConverter.convert("  ").text());
    }

    @Test
    void theConvertedTemplateRendersWhatVelocityWould() {
        var engine = new DynamiaTemplateEngine();
        var velocity = "Hi $user.name, #if($vip)VIP#{else}standard#end customer.\n"
                + "#foreach($l in $lines)$velocityCount) $l.name x$l.qty#if($foreach.hasNext), #end#end\n"
                + "Total: $total";
        var converted = ok(velocity);
        var data = Map.<String, Object>of("user", Map.of("name", "Ada"), "vip", false, "total", 30,
                "lines", List.of(Map.of("name", "A", "qty", 1), Map.of("name", "B", "qty", 2)));
        assertEquals("Hi Ada, standard customer.\n1) A x1, 2) B x2\nTotal: 30", engine.evaluate(converted, data));
    }
}
