package tools.dynamia.actions.replay;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.dynamia.actions.ActionExecutionRequest;
import tools.dynamia.actions.ActionExecutionResponse;
import tools.dynamia.actions.ActionFlowStepType;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.ui.EntityLookup;
import tools.dynamia.ui.UIChoices;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code UIChoices.pickEntity} is a CHOICE step that names a lookup; the picked key is checked by the lookup on the pass
 * that receives it, never trusted.
 */
class ReplayPickEntityTest {

    private final List<String> picked = new ArrayList<>();

    /** Only customers whose name starts with "A" can be picked, as a tenant or a filter would restrict them. */
    private static final class Customers implements EntityLookup<String> {
        private static final List<String> ALL = List.of("Ana", "Andres", "Luis");

        @Override
        public String name() {
            return "customers";
        }

        @Override
        public List<String> search(String text, int limit) {
            return ALL.stream().filter(n -> n.startsWith("A") && n.toLowerCase().contains(text.toLowerCase())).limit(limit).toList();
        }

        @Override
        public Optional<String> find(String key) {
            return search("", MAX_LIMIT).contains(key) ? Optional.of(key) : Optional.empty();
        }

        @Override
        public String key(String value) {
            return value;
        }

        @Override
        public String label(String value) {
            return value;
        }
    }

    @BeforeEach
    void install() {
        var beans = new SimpleObjectContainer("pick-entity-test");
        beans.addObject(new Customers());
        Containers.get().installObjectContainer(beans);
    }

    @AfterEach
    void uninstall() {
        Containers.get().removeAllContainers();
    }

    private Object body(ActionExecutionRequest request) {
        UIChoices.<String>pickEntity("customers", "Customer", picked::add);
        return null;
    }

    private ActionExecutionRequest answer(ActionExecutionResponse pending, Object answer) {
        var request = new ActionExecutionRequest();
        request.setResumeToken(pending.getFlow().getResumeToken());
        request.setData(answer);
        return request;
    }

    @Test
    void theStepNamesTheLookupAndCarriesNoOptions() {
        var pending = ReplayExecutor.execute("pick", new ActionExecutionRequest(), this::body);

        assertEquals(ActionFlowStepType.CHOICE, pending.getFlow().getType());
        var data = (Map<?, ?>) pending.getFlow().getData();
        assertEquals("customers", data.get("lookup"));
        assertTrue(((List<?>) data.get("options")).isEmpty(), "the catalog is searched, not sent");
    }

    @Test
    void aKeyOfTheSearchableSetIsPicked() {
        var pending = ReplayExecutor.execute("pick", new ActionExecutionRequest(), this::body);

        var done = ReplayExecutor.execute("pick", answer(pending, List.of("Ana")), this::body);

        assertEquals("SUCCESS", done.getStatus());
        assertEquals(List.of("Ana"), picked);
    }

    @Test
    void aKeyOutsideTheSearchableSetIsRefusedAndTheQuestionIsAskedAgain() {
        var pending = ReplayExecutor.execute("pick", new ActionExecutionRequest(), this::body);

        var again = ReplayExecutor.execute("pick", answer(pending, List.of("Luis")), this::body);

        assertEquals("PENDING", again.getStatus());
        assertEquals(ActionFlowStepType.CHOICE, again.getFlow().getType());
        assertTrue(picked.isEmpty());
    }

    @Test
    void cancellingPicksNothing() {
        var pending = ReplayExecutor.execute("pick", new ActionExecutionRequest(), this::body);

        var done = ReplayExecutor.execute("pick", answer(pending, List.of()), this::body);

        assertEquals("SUCCESS", done.getStatus());
        assertTrue(picked.isEmpty());
    }

    @Test
    void anUnknownLookupIsAProgrammingError() {
        assertThrows(IllegalArgumentException.class, () ->
                ReplayExecutor.execute("pick", new ActionExecutionRequest(), request -> {
                    UIChoices.<String>pickEntity("nope", null, picked::add);
                    return null;
                }));
    }
}
