package tools.dynamia.actions.replay;

import org.junit.jupiter.api.Test;
import tools.dynamia.actions.ActionFlowStep;
import tools.dynamia.actions.ActionFlowStepType;
import tools.dynamia.ui.UIMessages;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The headless implementation of any UI facade takes the session of the execution, so its interactions are numbered
 * together with the ones of {@code UIMessages}.
 */
class ReplaySessionTest {

    /** A toy headless facade implementation, the shape every new facade will have. */
    private static void choose(String title, java.util.function.Consumer<String> onChoice) {
        var session = ReplaySession.current();
        session.interact(ActionFlowStep.input(title, title), answer -> onChoice.accept(String.valueOf(answer)));
    }

    private final List<String> log = new ArrayList<>();

    private void action() {
        UIMessages.showQuestion("Sure?", "Confirm", () -> choose("Which format?", format -> log.add("export " + format)));
    }

    private Object pass(ReplaySession session) {
        var interactions = new ReplayInteractions(session);
        return ReplaySession.run(session, () -> UIMessages.withDisplayer(interactions, () -> {
            action();
            return null;
        }));
    }

    @Test
    void currentIsOnlyBoundInsideRun() {
        assertNull(ReplaySession.current());
        var session = new ReplaySession(List.of());
        ReplaySession.run(session, () -> {
            assertEquals(session, ReplaySession.current());
            return null;
        });
        assertNull(ReplaySession.current());
    }

    @Test
    void interactionsOfDifferentFacadesShareOneNumbering() {
        var first = new ReplaySession(new ArrayList<>());
        pass(first);
        assertEquals(ActionFlowStepType.CONFIRM, first.pending().getType());

        var second = new ReplaySession(new ArrayList<>(List.of(true)));
        pass(second);
        assertEquals(ActionFlowStepType.INPUT, second.pending().getType());
        assertTrue(log.isEmpty());

        var third = new ReplaySession(new ArrayList<>(List.of(true, "PDF")));
        pass(third);
        assertNull(third.pending());
        assertEquals(List.of("export PDF"), log);
    }
}
