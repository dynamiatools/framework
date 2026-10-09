package tools.dynamia.actions.replay;

import org.junit.jupiter.api.Test;
import tools.dynamia.actions.ActionExecutionRequest;
import tools.dynamia.actions.ActionExecutionResponse;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.ui.UIProgress;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReplayProgressRunnerTest {

    private final List<String> log = new ArrayList<>();

    @Test
    void theTaskRunsInsideTheRequestAndThenFinishRuns() {
        var done = ReplayExecutor.execute("move", new ActionExecutionRequest(), request -> {
            UIProgress.run("Moving", monitor -> {
                monitor.setMax(2);
                log.add("task");
                monitor.increment();
            }, () -> {
                log.add("finish");
                UIMessages.showMessage("Moving files completed");
            });
            return null;
        });

        assertEquals("SUCCESS", done.getStatus());
        assertEquals(List.of("task", "finish"), log);
        assertEquals("Moving files completed", done.getFlow().getMessage());
    }

    @Test
    void aFailingTaskGoesToOnErrorAndNotToFinish() {
        ReplayExecutor.execute("move", new ActionExecutionRequest(), request -> {
            UIProgress.run("Moving", null, monitor -> {
                throw new IllegalStateException("disk full");
            }, () -> log.add("finish"), e -> log.add("error: " + e.getMessage()));
            return null;
        });

        assertEquals(List.of("error: disk full"), log);
    }

    @Test
    void askingAfterProgressFailsBecauseTheWorkWouldRepeat() {
        assertThrows(IllegalStateException.class, () ->
                ReplayExecutor.execute("move", new ActionExecutionRequest(), request -> {
                    UIProgress.run("Moving", monitor -> log.add("task"), null);
                    UIMessages.showQuestion("Again?", () -> log.add("again"));
                    return null;
                }));
    }

    @Test
    void progressAfterAnAnsweredQuestionIsFine() {
        java.util.function.Function<ActionExecutionRequest, Object> body = request -> {
            UIMessages.showQuestion("Sure?", () -> UIProgress.run("Moving", monitor -> log.add("task"), null));
            return null;
        };
        ActionExecutionResponse pending = ReplayExecutor.execute("move", new ActionExecutionRequest(), body);
        assertTrue(log.isEmpty(), "the pass that stops at a question does not do the work");

        var resume = new ActionExecutionRequest();
        resume.setResumeToken(pending.getFlow().getResumeToken());
        resume.setData(true);
        ReplayExecutor.execute("move", resume, body);

        assertEquals(List.of("task"), log);
    }
}
