package tools.dynamia.crud.headless;

import org.junit.jupiter.api.Test;
import tools.dynamia.actions.ActionFlowStepType;
import tools.dynamia.actions.replay.ReplaySession;
import tools.dynamia.ui.FormOptions;
import tools.dynamia.ui.UIFacades;
import tools.dynamia.ui.UIViews;
import tools.dynamia.ui.ViewsProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code UIViews.showForm} in a headless run: a {@code DIALOG} step for the client, then the submitted values on the bean.
 */
class HeadlessViewsTest {

    public static class Payment {
        String amount;
        String note;
    }

    private final List<String> log = new ArrayList<>();

    private void run(ReplaySession session, Payment payment) {
        var provider = new HeadlessViews(session, (bean, type) -> Map.of("amount", ((Payment) bean).amount == null ? "" : ((Payment) bean).amount),
                (bean, type, values) -> {
                    ((Payment) bean).amount = (String) values.get("amount");
                    ((Payment) bean).note = (String) values.get("note");
                });
        UIFacades.with(ViewsProvider.class, provider, () -> {
            UIViews.showForm(FormOptions.of("New payment", Payment.class, payment).submitLabel("Create"), (p, dialog) -> {
                log.add("submitted " + p.amount + "/" + p.note);
                dialog.close();
            });
            return null;
        });
    }

    @Test
    void theFormBecomesADialogStepOfThatClassPrefilledWithTheBean() {
        var session = new ReplaySession(new ArrayList<>());
        var payment = new Payment();
        payment.amount = "5";

        run(session, payment);

        var step = session.pending();
        assertNotNull(step);
        assertEquals(ActionFlowStepType.DIALOG, step.getType());
        assertEquals("form", step.getViewDescriptor());
        assertEquals(Payment.class.getName(), step.getViewClass());
        assertEquals("New payment", step.getTitle());
        assertEquals(Map.of("amount", "5"), step.getData());
        assertTrue(log.isEmpty());
    }

    @Test
    void theSubmittedValuesAreAppliedAndTheHandlerRuns() {
        var session = new ReplaySession(new ArrayList<>(List.of(Map.of("amount", "10", "note", "cash"))));

        run(session, new Payment());

        assertNull(session.pending());
        assertEquals(List.of("submitted 10/cash"), log);
    }

    @Test
    void cancellingRunsNothing() {
        var answers = new ArrayList<Object>();
        answers.add(null);
        var session = new ReplaySession(answers);

        run(session, new Payment());

        assertNull(session.pending());
        assertTrue(log.isEmpty());
    }
}
