package tools.dynamia.actions.replay;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import tools.dynamia.actions.ActionExecutionRequest;
import tools.dynamia.actions.ActionExecutionResponse;
import tools.dynamia.actions.ActionFlowStepType;
import tools.dynamia.actions.flow.FlowTokenSigner;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.ui.UIProgress;
import tools.dynamia.ui.files.FlowPrincipal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What makes replay safe: answers only apply to the question they answered, a pass that failed is not committed, tokens
 * belong to a user, and a missing secret or a huge state is an error, not a surprise.
 */
class ReplayRobustnessTest {

    private final List<String> log = new ArrayList<>();
    private final AtomicReference<String> user = new AtomicReference<>("ana");

    @AfterEach
    void uninstall() {
        Containers.get().removeAllContainers();
    }

    /** An environment with the given properties; no profile is active unless one is set afterwards. */
    private static StandardEnvironment env(String... keysAndValues) {
        var environment = new StandardEnvironment();
        var map = new java.util.HashMap<String, Object>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            map.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        environment.getPropertySources().addFirst(new MapPropertySource("test", map));
        return environment;
    }

    private void installUser() {
        var beans = new SimpleObjectContainer("robustness");
        beans.addObject((FlowPrincipal) new FlowPrincipal() {
            @Override
            public String subject() {
                return user.get();
            }

            @Override
            public String tenant() {
                return null;
            }
        });
        Containers.get().installObjectContainer(beans);
    }

    private ActionExecutionRequest answer(ActionExecutionResponse pending, Object answer) {
        var request = new ActionExecutionRequest();
        request.setResumeToken(pending.getFlow().getResumeToken());
        request.setData(answer);
        return request;
    }

    @Test
    void anAnswerIsNotAppliedWhenTheActionNowAsksAnotherQuestion() {
        var amount = new int[]{5};
        Function<ActionExecutionRequest, Object> body = request -> {
            UIMessages.showQuestion("Delete " + amount[0] + " items?", () -> log.add("deleted " + amount[0]));
            return null;
        };
        var pending = ReplayExecutor.execute("delete", new ActionExecutionRequest(), body);
        assertEquals("Delete 5 items?", pending.getFlow().getMessage());

        amount[0] = 9; // the data changed between the question and the answer
        var reasked = ReplayExecutor.execute("delete", answer(pending, true), body);

        assertEquals(ActionFlowStepType.CONFIRM, reasked.getFlow().getType());
        assertEquals(MessageType.WARNING, reasked.getFlow().getMessageType());
        assertTrue(reasked.getFlow().getMessage().endsWith("Delete 9 items?"), "the new question is shown");
        assertTrue(log.isEmpty(), "the yes of the old question did not delete anything");

        var done = ReplayExecutor.execute("delete", answer(reasked, true), body);
        assertEquals("SUCCESS", done.getStatus());
        assertEquals(List.of("deleted 9"), log);
    }

    @Test
    void aValidationRetryAsksTheSameStepAgainWithTheErrorAndNoLaterAnswerSurvives() {
        Function<ActionExecutionRequest, Object> body = request -> {
            UIMessages.<Integer>showInput("How many?", Integer.class, n -> {
                if (n < 1) {
                    throw new ReplayRetry("Must be at least 1");
                }
                UIMessages.showQuestion("Use " + n + "?", () -> log.add("used " + n));
            });
            return null;
        };
        var pending = ReplayExecutor.execute("ask", new ActionExecutionRequest(), body);

        var reasked = ReplayExecutor.execute("ask", answer(pending, 0), body);

        assertEquals(ActionFlowStepType.INPUT, reasked.getFlow().getType());
        assertEquals(MessageType.ERROR, reasked.getFlow().getMessageType());
        assertTrue(reasked.getFlow().getMessage().startsWith("Must be at least 1"));

        var confirm = ReplayExecutor.execute("ask", answer(reasked, 3), body);
        assertEquals("Use 3?", confirm.getFlow().getMessage());
        ReplayExecutor.execute("ask", answer(confirm, true), body);
        assertEquals(List.of("used 3"), log);
    }

    /** Transactions that remember how every pass ended and where tasks ran. */
    private static final class RecordingTransactions implements ReplayTransactions {
        final List<String> outcomes = new ArrayList<>();
        boolean inside;
        boolean taskInsideTransaction;

        @Override
        public <T> T run(Supplier<T> work, BooleanSupplier commit) {
            inside = true;
            try {
                T result = work.get();
                outcomes.add(commit.getAsBoolean() ? "commit" : "rollback");
                return result;
            } catch (RuntimeException e) {
                outcomes.add("rollback");
                throw e;
            } finally {
                inside = false;
            }
        }

        @Override
        public <T> T runOutside(Supplier<T> work) {
            boolean was = inside;
            inside = false;
            try {
                return work.get();
            } finally {
                inside = was;
            }
        }
    }

    private RecordingTransactions installTransactions() {
        var transactions = new RecordingTransactions();
        var beans = new SimpleObjectContainer("transactions");
        beans.addObject((ReplayTransactions) transactions);
        Containers.get().installObjectContainer(beans);
        return transactions;
    }

    @Test
    void aProgressTaskRunsOutsideTheTransactionOfThePass() {
        var transactions = installTransactions();
        ReplayExecutor.execute("task", new ActionExecutionRequest(), request -> {
            UIProgress.run("Working", monitor -> transactions.taskInsideTransaction = transactions.inside, null);
            return null;
        });

        assertFalse(transactions.taskInsideTransaction);
        assertEquals(List.of("commit"), transactions.outcomes);
    }

    @Test
    void aPassWhoseProgressTaskFailedIsRolledBackEvenIfTheActionHandledTheError() {
        var transactions = installTransactions();
        var done = ReplayExecutor.execute("task", new ActionExecutionRequest(), request -> {
            UIProgress.run("Working", null, monitor -> {
                throw new IllegalStateException("boom");
            }, null, e -> UIMessages.showMessage("Error: " + e.getMessage()));
            return null;
        });

        assertEquals(List.of("rollback"), transactions.outcomes);
        assertEquals("Error: boom", done.getFlow().getMessage());
    }

    @Test
    void passesThatStopAtAQuestionAreRolledBackAndTheLastOneIsCommitted() {
        var transactions = installTransactions();
        Function<ActionExecutionRequest, Object> body = request -> {
            UIMessages.showQuestion("Sure?", () -> log.add("yes"));
            return null;
        };
        var pending = ReplayExecutor.execute("ask", new ActionExecutionRequest(), body);
        ReplayExecutor.execute("ask", answer(pending, true), body);

        assertEquals(List.of("rollback", "commit"), transactions.outcomes);
    }

    @Test
    void aTokenOfAnotherUserIsRejectedWith401() {
        installUser();
        Function<ActionExecutionRequest, Object> body = request -> {
            UIMessages.showQuestion("Sure?", () -> log.add("yes"));
            return null;
        };
        var pending = ReplayExecutor.execute("ask", new ActionExecutionRequest(), body);

        user.set("luis");
        var rejected = ReplayExecutor.execute("ask", answer(pending, true), body);

        assertEquals(401, rejected.getStatusCode());
        assertTrue(log.isEmpty());

        user.set("ana");
        assertEquals("SUCCESS", ReplayExecutor.execute("ask", answer(pending, true), body).getStatus());
    }

    @Test
    void theSecretIsMandatoryInProductionAndWhenRequired() {
        var longSecret = "0123456789012345678901234567890123456789";
        new FlowTokenSigner(env(FlowTokenSigner.SECRET_PROPERTY, longSecret));
        new FlowTokenSigner(env()); // development: a random secret with a warning

        var prod = env();
        prod.setActiveProfiles("prod");
        assertThrows(IllegalStateException.class, () -> new FlowTokenSigner(prod));
        assertThrows(IllegalStateException.class, () -> new FlowTokenSigner(env(FlowTokenSigner.REQUIRE_SECRET_PROPERTY, "true")));

        var prodWithSecret = env(FlowTokenSigner.SECRET_PROPERTY, longSecret);
        prodWithSecret.setActiveProfiles("prod");
        new FlowTokenSigner(prodWithSecret);
    }

    @Test
    void aStateTooBigForATokenIsAClearError() {
        var signer = new FlowTokenSigner(env(FlowTokenSigner.MAX_TOKEN_BYTES_PROPERTY, "200"));
        var big = new tools.dynamia.actions.flow.FlowTokenPayload("f", "a", "CONFIRM",
                java.util.Map.of("blob", "x".repeat(1000)), Long.MAX_VALUE);

        var failure = assertThrows(IllegalStateException.class, () -> signer.sign(big));

        assertTrue(failure.getMessage().contains(FlowTokenSigner.MAX_TOKEN_BYTES_PROPERTY));
    }
}
