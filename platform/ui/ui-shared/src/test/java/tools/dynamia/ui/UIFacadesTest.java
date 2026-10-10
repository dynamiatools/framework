package tools.dynamia.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Environment resolution: bound environment, then the active provider of the container, then no UI at all.
 */
class UIFacadesTest {

    interface Spi {
        String name();
    }

    private final SimpleObjectContainer container = new SimpleObjectContainer("ui-facades-test");

    @BeforeEach
    void install() {
        Containers.get().removeAllContainers();
        Containers.get().installObjectContainer(container);
    }

    @AfterEach
    void uninstall() {
        Containers.get().removeAllContainers();
    }

    private static Spi spi(String name) {
        return () -> name;
    }

    private static UIEnvironment environment(String name, Class<?> spi, Object implementation) {
        return new UIEnvironment() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public <S> Optional<S> port(Class<S> requested) {
                return requested == spi ? Optional.of(requested.cast(implementation)) : Optional.empty();
            }
        };
    }

    private static UIEnvironmentProvider provider(boolean active, UIEnvironment environment) {
        return new UIEnvironmentProvider() {
            @Override
            public boolean isActive() {
                return active;
            }

            @Override
            public UIEnvironment environment() {
                return environment;
            }
        };
    }

    @Test
    void withoutBindingNorProviderTheEnvironmentIsNone() {
        assertSame(NoUIEnvironment.INSTANCE, UIFacades.current());
        assertEquals("none", UIPlatform.current().name());
    }

    @Test
    void anActiveProviderBeatsNone() {
        container.addObject("zkLike", provider(true, environment("zk", Spi.class, spi("zk"))));

        assertEquals("zk", UIFacades.current().name());
        assertEquals("zk", UIFacades.port(Spi.class).name());
    }

    @Test
    void anInactiveProviderIsIgnored() {
        container.addObject("zkLike", provider(false, environment("zk", Spi.class, spi("zk"))));

        assertEquals("none", UIFacades.current().name());
    }

    @Test
    void theBoundEnvironmentBeatsAnActiveProvider() {
        container.addObject("zkLike", provider(true, environment("zk", Spi.class, spi("zk"))));

        var seen = UIFacades.with(environment("test", Spi.class, spi("bound")), () -> UIFacades.port(Spi.class).name());

        assertEquals("bound", seen);
        assertEquals("zk", UIFacades.port(Spi.class).name());
    }

    @Test
    void theBindingEndsWithTheWork() {
        UIFacades.with(environment("test", Spi.class, spi("bound")), () -> {
            assertEquals("test", UIFacades.current().name());
            return null;
        });

        assertEquals("none", UIFacades.current().name());
    }

    @Test
    void aPortTheEnvironmentDoesNotSupportFailsWithPortAndEnvironment() {
        var e = assertThrows(UIUnavailableException.class, () -> UIFacades.port(FileTransfer.class));

        assertEquals("files", e.getPort());
        assertEquals("none", e.getEnvironment());
        assertTrue(e.getMessage().contains("'files'"));
        assertTrue(e.getMessage().contains("'none'"));
    }

    @Test
    void facadesFailClearlyWhereThereIsNoUi() {
        var e = assertThrows(UIUnavailableException.class, () -> UIFiles.download("a.txt", "text/plain", new byte[0]));
        assertEquals("files", e.getPort());

        assertThrows(UIUnavailableException.class, () -> UINavigation.open("/somewhere"));
        assertThrows(UIUnavailableException.class, () -> UIMessages.showQuestion("Sure?", () -> {
        }));
    }

    @Test
    void messagesWithoutUiAreLoggedNotFailed() {
        UIMessages.showMessage("from a job", MessageType.WARNING);
    }

    @Test
    void withAPortOverlaysOneSpiAndKeepsTheRest() {
        var outer = environment("test", Spi.class, spi("outer"));

        UIFacades.with(outer, () -> UIFacades.with(Runnable.class, (Runnable) () -> {
        }, () -> {
            assertEquals("outer", UIFacades.port(Spi.class).name());
            assertEquals("test", UIFacades.current().name());
            assertTrue(UIPlatform.supports(Runnable.class));
            assertFalse(UIPlatform.supports(FileTransfer.class));
            return null;
        }));
    }

    @Test
    void uiMessagesFollowsTheEnvironmentOnEveryCall() {
        var first = new ArrayList<String>();
        var second = new ArrayList<String>();

        UIFacades.with(environment("a", MessageDisplayer.class, new RecordingDisplayer(first)), () -> {
            UIMessages.showMessage("one");
            return null;
        });
        UIFacades.with(environment("b", MessageDisplayer.class, new RecordingDisplayer(second)), () -> {
            UIMessages.showMessage("two");
            return null;
        });

        assertEquals(List.of("one"), first);
        assertEquals(List.of("two"), second);
    }

    @Test
    void uiMessagesUsesTheBoundDisplayer() {
        var shown = new ArrayList<String>();

        UIMessages.withDisplayer(new RecordingDisplayer(shown), () -> {
            UIMessages.showMessage("hello");
            return null;
        });

        assertEquals(List.of("hello"), shown);
    }
}
