package tools.dynamia.zk.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.ui.ChoicesProvider;
import tools.dynamia.ui.FileTransfer;
import tools.dynamia.ui.MessageDisplayer;
import tools.dynamia.ui.NavigationProvider;
import tools.dynamia.ui.ProgressRunner;
import tools.dynamia.ui.UIFacades;
import tools.dynamia.ui.UIUnavailableException;
import tools.dynamia.ui.ViewsProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The ZK environment serves every port with ZK widgets, lets a container bean replace one, and is not active
 * outside a ZK execution.
 */
class ZKUIEnvironmentTest {

    private final SimpleObjectContainer container = new SimpleObjectContainer("zk-ui-environment-test");

    @BeforeEach
    void install() {
        Containers.get().removeAllContainers();
        Containers.get().installObjectContainer(container);
    }

    @AfterEach
    void uninstall() {
        Containers.get().removeAllContainers();
    }

    @Test
    void itIsNotActiveWithoutAZkExecution() {
        container.addObject("zkProvider", new ZKUIEnvironmentProvider());

        assertFalse(new ZKUIEnvironmentProvider().isActive());
        assertEquals("none", UIFacades.current().name());
        assertThrows(UIUnavailableException.class, () -> UIFacades.port(FileTransfer.class));
    }

    @Test
    void itServesEveryPortWithItsZkImplementation() {
        var environment = new ZKUIEnvironment();

        assertEquals("zk", environment.name());
        assertTrue(environment.port(MessageDisplayer.class).orElseThrow() instanceof MessageNotification);
        assertTrue(environment.port(FileTransfer.class).orElseThrow() instanceof ZKFileTransfer);
        assertTrue(environment.port(ProgressRunner.class).orElseThrow() instanceof ZKProgressRunner);
        assertTrue(environment.port(ViewsProvider.class).orElseThrow() instanceof ZKViewsProvider);
        assertTrue(environment.port(ChoicesProvider.class).orElseThrow() instanceof ZKChoicesProvider);
        assertTrue(environment.port(NavigationProvider.class).orElseThrow() instanceof ZKNavigationProvider);
        assertFalse(environment.supports(Runnable.class));
    }

    @Test
    void aContainerBeanReplacesTheZkImplementationOfThatPort() {
        MessageDisplayer custom = new MessageNotification();
        container.addObject("customDisplayer", custom);

        var environment = new ZKUIEnvironment();

        assertSame(custom, environment.port(MessageDisplayer.class).orElseThrow());
        assertTrue(environment.port(FileTransfer.class).orElseThrow() instanceof ZKFileTransfer);
    }
}
