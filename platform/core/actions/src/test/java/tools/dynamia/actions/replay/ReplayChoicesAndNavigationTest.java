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
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void aChoiceIsAStepWithLabelsAndTheAnswerIsThePosition() {
        var pending = ReplayExecutor.execute("move", new ActionExecutionRequest(), moveLike);

        assertEquals(ActionFlowStepType.CHOICE, pending.getFlow().getType());
        var data = (Map<?, ?>) pending.getFlow().getData();
        assertEquals(List.of("LOCAL", "S3", "FTP"), data.get("options"));
        assertEquals(false, data.get("multiple"));

        var confirm = ReplayExecutor.execute("move", answer(pending, 1), moveLike);
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
    void manyChoicesGetTheListOfOptions() {
        Function<ActionExecutionRequest, Object> body = request -> {
            UIChoices.chooseMany("Pick", storages, String::valueOf, chosen -> log.add(String.join(",", chosen)));
            return null;
        };
        var pending = ReplayExecutor.execute("pick", new ActionExecutionRequest(), body);
        ReplayExecutor.execute("pick", answer(pending, List.of(0, 2)), body);

        assertEquals(List.of("local,ftp"), log);
    }

    @Test
    void aPositionOutsideTheOptionsIsRejected() {
        var pending = ReplayExecutor.execute("move", new ActionExecutionRequest(), moveLike);
        assertThrows(IllegalArgumentException.class, () -> ReplayExecutor.execute("move", answer(pending, 7), moveLike));
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
