package tools.dynamia.actions.replay;

import org.junit.jupiter.api.Test;
import tools.dynamia.ui.ChoicesProvider;
import tools.dynamia.ui.FileTransfer;
import tools.dynamia.ui.MessageDisplayer;
import tools.dynamia.ui.NavigationProvider;
import tools.dynamia.ui.ProgressRunner;
import tools.dynamia.ui.UIFacades;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.ui.UIPlatform;
import tools.dynamia.ui.ViewsProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * One environment per pass: every headless port, plus what contributors add, behind a single binding.
 */
class ReplayUIEnvironmentTest {

    @Test
    void aSingleBindingServesEveryBuiltInPort() {
        var session = new ReplaySession(new ArrayList<>());
        var environment = new ReplayUIEnvironment(session, List.of());

        UIFacades.with(environment, () -> {
            assertEquals("replay", UIPlatform.current().name());
            assertSame(environment.interactions(), UIFacades.port(MessageDisplayer.class));
            assertTrue(UIFacades.port(FileTransfer.class) instanceof ReplayFileTransfer);
            assertTrue(UIFacades.port(ProgressRunner.class) instanceof ReplayProgressRunner);
            assertTrue(UIFacades.port(ChoicesProvider.class) instanceof ReplayChoicesProvider);
            assertTrue(UIFacades.port(NavigationProvider.class) instanceof ReplayNavigationProvider);
            assertSame(session, ReplaySession.current());
            return null;
        });
        assertNull(ReplaySession.current());
    }

    @Test
    void portsNobodyContributedAreNotSupported() {
        var environment = new ReplayUIEnvironment(new ReplaySession(new ArrayList<>()), List.of());

        assertFalse(environment.supports(ViewsProvider.class));
    }

    @Test
    void contributorsAddPortsForTheSessionOfThePass() {
        var views = new Object[1];
        ReplayPortContributor contributor = session -> {
            var provider = new ViewsProvider() {
                @Override
                public <T> void showForm(tools.dynamia.ui.FormOptions<T> options, java.util.function.BiConsumer<T, tools.dynamia.ui.ViewDialog> onSubmit) {
                }

                @Override
                public <T> void showView(tools.dynamia.ui.ViewOptions<T> options) {
                }
            };
            views[0] = provider;
            return Map.of(ViewsProvider.class, provider);
        };
        var environment = new ReplayUIEnvironment(new ReplaySession(new ArrayList<>()), List.of(contributor));

        UIFacades.with(environment, () -> {
            assertSame(views[0], UIFacades.port(ViewsProvider.class));
            return null;
        });
    }

    @Test
    void questionsStopTheActionAndTheEnvironmentKnowsIt() {
        var environment = new ReplayUIEnvironment(new ReplaySession(new ArrayList<>()), List.of());
        var log = new ArrayList<String>();

        UIFacades.with(environment, () -> {
            UIMessages.showQuestion("Sure?", () -> log.add("yes"));
            return null;
        });

        assertTrue(environment.interactions().isPending());
        assertTrue(log.isEmpty());
    }
}
