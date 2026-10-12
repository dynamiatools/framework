package tools.dynamia.actions.replay;

import org.junit.jupiter.api.Test;
import tools.dynamia.actions.ActionExecutionRequest;
import tools.dynamia.actions.ActionExecutionResponse;
import tools.dynamia.actions.ActionFlowStepType;
import tools.dynamia.ui.UIChoices;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.ui.UINavigation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReplayChoicesAndNavigationTest {

    private final List<String> log = new ArrayList<>();
    private final List<String> storages = List.of("local", "s3", "ftp");

    private ActionExecutionRequest answer(ActionExecutionResponse pending, Object answer) {
        var request = new ActionExecutionRequest();
        request.setResumeToken(pending.getFlow().getResumeToken());
        request.setData(answer);
        return request;
    }

    private final Function<ActionExecutionRequest, Object> moveLike = request -> {
        UIChoices.chooseOne("Select storage", storages, String::toUpperCase, storage ->
                UIMessages.showQuestion("Move to " + storage + "?", () -> log.add("moved to " + storage)));
        return null;
    };

    @Test
    void aChoiceIsAStepWithKeyedOptionsAndTheAnswerIsTheKey() {
        var pending = ReplayExecutor.execute("move", new ActionExecutionRequest(), moveLike);

        assertEquals(ActionFlowStepType.CHOICE, pending.getFlow().getType());
        var data = (Map<?, ?>) pending.getFlow().getData();
        assertEquals(List.of(Map.of("key", "LOCAL", "label", "LOCAL"), Map.of("key", "S3", "label", "S3"),
                Map.of("key", "FTP", "label", "FTP")), data.get("options"));
        assertEquals(false, data.get("multiple"));

        var confirm = ReplayExecutor.execute("move", answer(pending, List.of("S3")), moveLike);
        assertEquals(ActionFlowStepType.CONFIRM, confirm.getFlow().getType());
        assertEquals("Move to s3?", confirm.getFlow().getMessage());

        ReplayExecutor.execute("move", answer(confirm, true), moveLike);
        assertEquals(List.of("moved to s3"), log);
    }

    @Test
    void cancellingTheChoiceRunsNothing() {
        var pending = ReplayExecutor.execute("move", new ActionExecutionRequest(), moveLike);
        var done = ReplayExecutor.execute("move", answer(pending, List.of()), moveLike);

        assertEquals("SUCCESS", done.getStatus());
        assertTrue(log.isEmpty());
    }

    @Test
    void manyChoicesAreAnsweredWithKeysAndNoneIsCancelling() {
        Function<ActionExecutionRequest, Object> body = request -> {
            UIChoices.chooseMany("Pick", storages, String::valueOf, chosen -> log.add(String.join(",", chosen)));
            return null;
        };
        var pending = ReplayExecutor.execute("pick", new ActionExecutionRequest(), body);
        ReplayExecutor.execute("pick", answer(pending, List.of("local", "ftp")), body);
        assertEquals(List.of("local,ftp"), log);

        var again = ReplayExecutor.execute("pick", new ActionExecutionRequest(), body);
        ReplayExecutor.execute("pick", answer(again, List.of()), body);
        assertEquals(List.of("local,ftp"), log, "an empty selection does not call the callback");
    }

    @Test
    void anUnknownKeyAsksTheQuestionAgainWithAWarning() {
        var pending = ReplayExecutor.execute("move", new ActionExecutionRequest(), moveLike);

        var again = ReplayExecutor.execute("move", answer(pending, List.of("NOPE")), moveLike);

        assertEquals(ActionFlowStepType.CHOICE, again.getFlow().getType());
        assertEquals(tools.dynamia.ui.MessageType.ERROR, again.getFlow().getMessageType());
        assertTrue(log.isEmpty());
    }

    @Test
    void whenTheOptionsChangeBetweenPassesTheAnswerIsNotAppliedToAnotherOption() {
        var options = new ArrayList<>(List.of("a", "b"));
        Function<ActionExecutionRequest, Object> body = request -> {
            UIChoices.chooseOne("Pick", options, String::valueOf, picked ->
                    UIMessages.showQuestion("Use " + picked + "?", () -> log.add("used " + picked)));
            return null;
        };
        var pending = ReplayExecutor.execute("pick", new ActionExecutionRequest(), body);
        var confirm = ReplayExecutor.execute("pick", answer(pending, List.of("b")), body);
        assertEquals("Use b?", confirm.getFlow().getMessage());

        options.remove("a"); // between the passes the list changed: the same key now points elsewhere in the list
        options.add("c");
        var reasked = ReplayExecutor.execute("pick", answer(confirm, true), body);

        assertEquals(ActionFlowStepType.CHOICE, reasked.getFlow().getType(), "the choice is asked again, not confirmed");
        assertEquals(tools.dynamia.ui.MessageType.WARNING, reasked.getFlow().getMessageType());
        assertTrue(log.isEmpty(), "nothing was done with the old answers");
    }

    @Test
    void navigationEndsTheActionWithARedirectStep() {
        var done = ReplayExecutor.execute("open", new ActionExecutionRequest(), request -> {
            log.add("did work");
            UINavigation.openInNewWindow("/files/1/download");
            return null;
        });

        assertEquals(ActionFlowStepType.REDIRECT, done.getFlow().getType());
        var data = (Map<?, ?>) done.getFlow().getData();
        assertEquals("/files/1/download", data.get("url"));
        assertEquals(true, data.get("newWindow"));
        assertEquals(List.of("did work"), log);
    }
}
