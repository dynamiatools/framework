package tools.dynamia.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class UIFacadesTest {

    interface Spi {
        String name();
    }

    private static Spi spi(String name) {
        return () -> name;
    }

    @Test
    void boundImplementationIsVisibleOnlyInsideWith() {
        assertNull(UIFacades.bound(Spi.class));

        var result = UIFacades.with(Spi.class, spi("headless"), () -> UIFacades.resolve(Spi.class).name());

        assertEquals("headless", result);
        assertNull(UIFacades.bound(Spi.class));
    }

    @Test
    void nestedBindingsOfOtherSpisStayVisibleAndSameSpiIsReplaced() {
        var outer = spi("outer");
        var inner = spi("inner");
        Runnable other = () -> {
        };

        UIFacades.with(Spi.class, outer, () ->
                UIFacades.with(Runnable.class, other, () -> {
                    assertSame(outer, UIFacades.bound(Spi.class));
                    assertSame(other, UIFacades.bound(Runnable.class));
                    UIFacades.with(Spi.class, inner, () -> {
                        assertSame(inner, UIFacades.bound(Spi.class));
                        return null;
                    });
                    assertSame(outer, UIFacades.bound(Spi.class));
                    return null;
                }));
    }

    @Test
    void uiMessagesUsesTheBoundDisplayer() {
        var shown = new java.util.ArrayList<String>();
        var displayer = new RecordingDisplayer(shown);

        UIMessages.withDisplayer(displayer, () -> {
            UIMessages.showMessage("hello");
            return null;
        });

        assertEquals(java.util.List.of("hello"), shown);
    }
}
