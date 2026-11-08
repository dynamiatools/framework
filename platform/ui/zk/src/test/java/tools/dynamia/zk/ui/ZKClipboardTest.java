package tools.dynamia.zk.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZKClipboardTest {

    @Test
    void theTextCannotBreakOutOfTheScriptLiteral() {
        String script = ZKClipboard.script("a\"b\\c\nd</script><script>alert(1)");

        assertTrue(script.contains("\\\"b"), "quote escaped");
        assertTrue(script.contains("\\n"), "newline escaped");
        assertFalse(script.contains("</script>"), "no closing tag in the output");
        assertTrue(script.contains("\\u003c/script>"));
    }
}
