package tools.dynamia.actions.replay;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.dynamia.actions.ActionExecutionRequest;
import tools.dynamia.actions.ActionExecutionResponse;
import tools.dynamia.actions.ActionFlowStepType;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.ui.UIFacades;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.ui.UIProgress;
import tools.dynamia.ui.files.FlowPrincipal;
import tools.dynamia.ui.jobs.InMemoryJobRegistry;
import tools.dynamia.ui.jobs.JobStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code UIProgress} in a remote run: the task does not run in the request. The pass stops at a {@code PROGRESS} step, the
 * client follows the job and, when it is done, the action goes on without running the task again.
 */
class ReplayProgressRunnerTest {

    private final List<String> log = java.util.Collections.synchronizedList(new ArrayList<>());
    private final InMemoryJobRegistry jobs = new InMemoryJobRegistry();

    @BeforeEach
    void install() {
        var beans = new SimpleObjectContainer("progress-test");
        beans.addObject(jobs);
        Containers.get().installObjectContainer(beans);
    }

    @AfterEach
    void uninstall() {
        Containers.get().removeAllContainers();
    }

    private static String jobId(ActionExecutionResponse pending) {
        return String.valueOf(((Map<?, ?>) pending.getFlow().getData()).get("jobId"));
    }

    private JobStatus waitFor(String jobId) throws InterruptedException {
        for (int i = 0; i < 200; i++) {
            var status = jobs.status(jobId, FlowPrincipal.ANONYMOUS).orElseThrow();
            if (status.state().isFinished()) {
                return status;
            }
            Thread.sleep(25);
        }
        throw new AssertionError("The job did not finish");
    }

    private ActionExecutionRequest answer(ActionExecutionResponse pending) {
        var request = new ActionExecutionRequest();
        request.setResumeToken(pending.getFlow().getResumeToken());
        request.setData(Map.of("jobId", jobId(pending), "state", "DONE"));
        return request;
    }

    private final Function<ActionExecutionRequest, Object> move = request -> {
        UIProgress.run("Moving", monitor -> {
            monitor.setMax(2);
            log.add("task");
            monitor.setCurrent(2);
        }, () -> {
            log.add("finish");
            UIMessages.showMessage("Moving files completed");
        });
        return null;
    };

    @Test
    void theRequestDoesNotWaitForTheTaskAndTheClientGetsAJobToFollow() throws Exception {
        var gate = new CountDownLatch(1);

        var pending = ReplayExecutor.execute("move", new ActionExecutionRequest(), request -> {
            UIProgress.run("Moving", monitor -> {
                gate.await(5, TimeUnit.SECONDS);
                log.add("task");
            }, () -> log.add("finish"));
            return null;
        });

        assertEquals("PENDING", pending.getStatus());
        assertEquals(ActionFlowStepType.PROGRESS, pending.getFlow().getType());
        assertEquals("Moving", pending.getFlow().getTitle());
        assertTrue(log.isEmpty(), "the request returned while the task is still waiting");
        assertEquals(JobStatus.State.RUNNING, jobs.status(jobId(pending), FlowPrincipal.ANONYMOUS).orElseThrow().state());

        gate.countDown();
        waitFor(jobId(pending));
        assertEquals(List.of("task"), log);
    }

    @Test
    void whenTheJobIsDoneOnFinishRunsOnceAndTheTaskIsNotRunAgain() throws Exception {
        var pending = ReplayExecutor.execute("move", new ActionExecutionRequest(), move);
        waitFor(jobId(pending));

        var done = ReplayExecutor.execute("move", answer(pending), move);

        assertEquals("SUCCESS", done.getStatus());
        assertEquals(List.of("task", "finish"), log, "the task ran once and onFinish once");
        assertEquals("Moving files completed", done.getFlow().getMessage());
        assertTrue(jobs.status(jobId(pending), FlowPrincipal.ANONYMOUS).isEmpty(), "the consumed job is forgotten");
    }

    @Test
    void ifTheJobStillRunsTheSameStepIsAskedAgainWithTheSameJob() throws Exception {
        var gate = new CountDownLatch(1);
        Function<ActionExecutionRequest, Object> slow = request -> {
            UIProgress.run("Moving", monitor -> gate.await(5, TimeUnit.SECONDS), () -> log.add("finish"));
            return null;
        };
        var pending = ReplayExecutor.execute("move", new ActionExecutionRequest(), slow);

        var again = ReplayExecutor.execute("move", answer(pending), slow);

        assertEquals(ActionFlowStepType.PROGRESS, again.getFlow().getType());
        assertEquals(jobId(pending), jobId(again), "it waits for the same job, it does not start another");
        assertTrue(log.isEmpty());

        gate.countDown();
        waitFor(jobId(pending));
        ReplayExecutor.execute("move", answer(again), slow);
        assertEquals(List.of("finish"), log);
    }

    @Test
    void theClientCannotClaimThatAJobIsDone() throws Exception {
        var gate = new CountDownLatch(1);
        Function<ActionExecutionRequest, Object> slow = request -> {
            UIProgress.run("Moving", monitor -> gate.await(5, TimeUnit.SECONDS), () -> log.add("finish"));
            return null;
        };
        var pending = ReplayExecutor.execute("move", new ActionExecutionRequest(), slow);

        ReplayExecutor.execute("move", answer(pending), slow); // the answer says DONE, the job says otherwise

        assertTrue(log.isEmpty());
        gate.countDown();
    }

    @Test
    void aFailingTaskGoesToOnErrorAndNotToFinish() throws Exception {
        Function<ActionExecutionRequest, Object> failing = request -> {
            UIProgress.run("Moving", null, monitor -> {
                throw new IllegalStateException("disk full");
            }, () -> log.add("finish"), e -> log.add("error: " + e.getMessage()));
            return null;
        };
        var pending = ReplayExecutor.execute("move", new ActionExecutionRequest(), failing);
        assertEquals(JobStatus.State.FAILED, waitFor(jobId(pending)).state());

        ReplayExecutor.execute("move", answer(pending), failing);

        assertEquals(List.of("error: disk full"), log);
    }

    @Test
    void askingAfterProgressIsFineBecauseTheWorkNeverRepeats() throws Exception {
        Function<ActionExecutionRequest, Object> body = request -> {
            UIProgress.run("Moving", monitor -> log.add("task"), () ->
                    UIMessages.showQuestion("Open the result?", () -> log.add("opened")));
            return null;
        };
        var pending = ReplayExecutor.execute("move", new ActionExecutionRequest(), body);
        waitFor(jobId(pending));

        var question = ReplayExecutor.execute("move", answer(pending), body);
        assertEquals(ActionFlowStepType.CONFIRM, question.getFlow().getType());

        var resume = new ActionExecutionRequest();
        resume.setResumeToken(question.getFlow().getResumeToken());
        resume.setData(true);
        ReplayExecutor.execute("move", resume, body);

        assertEquals(List.of("task", "opened"), log, "the task ran once across three passes");
    }

    @Test
    void progressAfterAnAnsweredQuestionStartsOnlyInThePassThatReachesIt() throws Exception {
        Function<ActionExecutionRequest, Object> body = request -> {
            UIMessages.showQuestion("Sure?", () -> UIProgress.run("Moving", monitor -> log.add("task"), null));
            return null;
        };
        var question = ReplayExecutor.execute("move", new ActionExecutionRequest(), body);
        Thread.sleep(100);
        assertTrue(log.isEmpty(), "the pass that stops at a question does not start the work");

        var resume = new ActionExecutionRequest();
        resume.setResumeToken(question.getFlow().getResumeToken());
        resume.setData(true);
        var progress = ReplayExecutor.execute("move", resume, body);

        assertEquals(ActionFlowStepType.PROGRESS, progress.getFlow().getType());
        waitFor(jobId(progress));
        assertEquals(List.of("task"), log);
    }

    @Test
    void theTaskRunsWithNoUi() throws Exception {
        var seen = new ArrayList<String>();
        var pending = ReplayExecutor.execute("move", new ActionExecutionRequest(), request -> {
            UIProgress.run("Moving", monitor -> seen.add(UIFacades.current().name()), null);
            return null;
        });
        waitFor(jobId(pending));

        assertEquals(List.of("none"), seen);
    }

    @Test
    void anotherUserCannotSeeTheJob() throws Exception {
        var pending = ReplayExecutor.execute("move", new ActionExecutionRequest(), move);
        waitFor(jobId(pending));

        var stranger = new FlowPrincipal() {
            @Override
            public String subject() {
                return "luis";
            }

            @Override
            public String tenant() {
                return null;
            }
        };
        assertTrue(jobs.status(jobId(pending), stranger).isEmpty());
        assertNull(jobs.status("unknown", FlowPrincipal.ANONYMOUS).orElse(null));
    }

    @Test
    void withoutAJobRegistryTheErrorSaysSo() {
        Containers.get().removeAllContainers();

        var failure = assertThrows(IllegalStateException.class, () -> ReplayExecutor.execute("move", new ActionExecutionRequest(), move));

        assertTrue(failure.getMessage().contains("JobRegistry"));
        assertFalse(log.contains("task"));
    }
}
