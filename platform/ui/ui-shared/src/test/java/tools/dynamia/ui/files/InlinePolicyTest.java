package tools.dynamia.ui.files;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InlinePolicyTest {

    @Test
    void allowsPassiveTypesWithOrWithoutParameters() {
        assertTrue(InlinePolicy.allows("application/pdf"));
        assertTrue(InlinePolicy.allows("Application/PDF; charset=binary"));
        assertTrue(InlinePolicy.allows("image/png"));
        assertTrue(InlinePolicy.allows("text/plain;charset=UTF-8"));
    }

    @Test
    void refusesActiveOrUnknownTypes() {
        assertFalse(InlinePolicy.allows("text/html"));
        assertFalse(InlinePolicy.allows("image/svg+xml"));
        assertFalse(InlinePolicy.allows("application/xhtml+xml"));
        assertFalse(InlinePolicy.allows("application/octet-stream"));
        assertFalse(InlinePolicy.allows(""));
        assertFalse(InlinePolicy.allows(null));
    }
}
