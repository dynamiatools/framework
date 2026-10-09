package tools.dynamia.actions.replay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.dynamia.actions.ActionExecutionRequest;
import tools.dynamia.actions.ActionExecutionResponse;
import tools.dynamia.actions.ActionFlowStepType;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An action is written once against {@link UIMessages}; the executor runs it again from the start every time the
 * user answers, answering its questions in order.
 */
class ReplayExecutorTest {

    /** What the "action" did to the outside world, to show which pass finally acted. */
    private final List<String> effects = new ArrayList<>();
    private final List<String> passes = new ArrayList<>();

    @BeforeEach
    void reset() {
        effects.clear();
        passes.clear();
    }

    /** "Delete?" -> yes: acts and says so. Same shape as DeleteAction -> controller.delete(entity). */
    private Object deleteLike(ActionExecutionRequest request) {
        passes.add("pass");
        UIMessages.showQuestion("Delete " + request.getDataId() + "?", "Confirm", () -> {
            effects.add("deleted " + request.getDataId());
            UIMessages.showMessage("Deleted", MessageType.NORMAL);
        });
        return Map.of("done", true);
    }

    private ActionExecutionRequest start(String id) {
        var request = new ActionExecutionRequest();
        request.setDataId(id);
        request.setDataType("Book");
        return request;
    }

    private ActionExecutionRequest answer(ActionExecutionResponse pending, Object answer) {
        var request = new ActionExecutionRequest();
        request.setResumeToken(pending.getFlow().getResumeToken());
        request.setData(answer);
        return request;
    }

    @Test
    void aQuestionStopsThePassAndNothingIsExecutedYet() {
        var response = ReplayExecutor.execute("delete", start("7"), this::deleteLike);

        assertEquals("PENDING", response.getStatus());
        assertEquals(ActionFlowStepType.CONFIRM, response.getFlow().getType());
        assertEquals("Delete 7?", response.getFlow().getMessage());
        assertEquals("Confirm", response.getFlow().getTitle());
        assertNotNull(response.getFlow().getResumeToken());
        assertTrue(effects.isEmpty(), "the callback of an unanswered question never runs");
    }

    @Test
    void yesRunsTheActionAgainWithTheAnswerAndTheCallbackActs() {
        var pending = ReplayExecutor.execute("delete", start("7"), this::deleteLike);
        var done = ReplayExecutor.execute("delete", answer(pending, true), this::deleteLike);

        assertEquals("SUCCESS", done.getStatus());
        assertEquals(ActionFlowStepType.DONE, done.getFlow().getType());
        assertEquals(List.of("deleted 7"), effects, "the original request travels in the token");
        assertEquals(2, passes.size(), "the action ran from the start twice");
        assertEquals("Deleted", done.getFlow().getMessage());
        assertNull(done.getFlow().getResumeToken());
    }

    @Test
    void noRunsItAgainAndNothingHappens() {
        var pending = ReplayExecutor.execute("delete", start("7"), this::deleteLike);
        var done = ReplayExecutor.execute("delete", answer(pending, false), this::deleteLike);

        assertEquals("SUCCESS", done.getStatus());
        assertTrue(effects.isEmpty());
    }

    @Test
    void questionsInsideAnsweredCallbacksAreAskedOneByOneInOrder() {
        Function<ActionExecutionRequest, Object> twoSteps = request -> {
            UIMessages.showQuestion("First?", () ->
                    UIMessages.<String>showInput("Reason", String.class, reason ->
                            UIMessages.showQuestion("Really, because " + reason + "?", () -> effects.add("acted: " + reason))));
            return null;
        };

        var q1 = ReplayExecutor.execute("two", start("1"), twoSteps);
        assertEquals(ActionFlowStepType.CONFIRM, q1.getFlow().getType());
        assertEquals("First?", q1.getFlow().getMessage());

        var q2 = ReplayExecutor.execute("two", answer(q1, true), twoSteps);
        assertEquals(ActionFlowStepType.INPUT, q2.getFlow().getType());
        assertEquals("Reason", q2.getFlow().getMessage());

        var q3 = ReplayExecutor.execute("two", answer(q2, "typo"), twoSteps);
        assertEquals(ActionFlowStepType.CONFIRM, q3.getFlow().getType());
        assertEquals("Really, because typo?", q3.getFlow().getMessage());
        assertTrue(effects.isEmpty());

        var done = ReplayExecutor.execute("two", answer(q3, true), twoSteps);
        assertEquals(ActionFlowStepType.DONE, done.getFlow().getType());
        assertEquals(List.of("acted: typo"), effects);
    }

    @Test
    void messagesOfAnAnsweredPassAreCollected() {
        var pending = ReplayExecutor.execute("delete", start("7"), request -> {
            UIMessages.showMessage("Heads up", MessageType.WARNING);
            UIMessages.showQuestion("Sure?", () -> UIMessages.showMessage("Done", MessageType.NORMAL));
            return null;
        });
        assertNull(pending.getParams(), "a pass that stops at a question shows nothing else");
    }

    @Test
    void aTokenOfAnotherActionOrTamperedIsRejected() {
        var pending = ReplayExecutor.execute("delete", start("7"), this::deleteLike);

        var otherAction = ReplayExecutor.execute("other", answer(pending, true), this::deleteLike);
        assertEquals(401, otherAction.getStatusCode());

        var tampered = answer(pending, true);
        tampered.setResumeToken("x" + tampered.getResumeToken());
        assertEquals(401, ReplayExecutor.execute("delete", tampered, this::deleteLike).getStatusCode());
        assertTrue(effects.isEmpty());
    }

    @Test
    void anExceptionOfTheActionPropagatesToTheCaller() {
        var e = assertThrows(IllegalStateException.class, () ->
                ReplayExecutor.execute("boom", start("1"), request -> {
                    throw new IllegalStateException("validation");
                }));
        assertEquals("validation", e.getMessage());
    }
}
