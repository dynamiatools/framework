/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.domain.jpa;

import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.util.AopTestUtils;
import tools.dynamia.domain.LoadPlan;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;

import static org.junit.jupiter.api.Assertions.*;

/**
 * How the CrudService initializes the associations of the entities it hands out, with Open Persistence In View
 * disabled: outside a transaction the persistence context is closed when the call returns.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = JpaTestConfig.class)
public class LoadPlanTest {

    @Autowired
    private CrudService crudService;
    @Autowired
    private PlatformTransactionManager txManager;

    private TransactionTemplate tx;
    private JpaCrudService target;
    private Long orderId;
    private Long noteOrderId;

    @BeforeEach
    public void setUp() {
        target = AopTestUtils.getUltimateTargetObject(crudService);
        tx = new TransactionTemplate(txManager);
        tx.executeWithoutResult(s -> {
            var order = new LoadPlanOrder("o1");
            for (int i = 0; i < 3; i++) {
                var line = new LoadPlanLine();
                line.setName("l" + i);
                line.setOrder(order);
                var sub = new LoadPlanSub();
                sub.setName("s" + i);
                sub.setLine(line);
                line.getSubs().add(sub);
                order.getLines().add(line);
            }
            var note = new LoadPlanNote();
            note.setText("n");
            note.setOrder(order);
            order.getNotes().add(note);
            orderId = crudService.save(order).getId();
        });
    }

    @AfterEach
    public void tearDown() {
        target.setLoadCollectionsMode("annotated");
        tx.executeWithoutResult(s -> crudService.executeQuery("select o from LoadPlanOrder o").forEach(o -> crudService.delete(o)));
    }

    // ---------------------------------------------------------------- the declared plan (@InitializeOnLoad)

    @Test
    public void loadOutsideTransactionInitializesTheDeclaredPlan() {
        var order = crudService.load(LoadPlanOrder.class, orderId);

        assertEquals(3, order.getLines().size());
        assertEquals(1, order.getLines().getFirst().getSubs().size(), "nested path lines.subs");
    }

    @Test
    public void whatIsNotInThePlanStaysLazy() {
        var order = crudService.load(LoadPlanOrder.class, orderId);

        assertThrows(LazyInitializationException.class, () -> order.getNotes().size());
    }

    @Test
    public void reloadOutsideTransactionInitializesTheDeclaredPlan() {
        var detached = new LoadPlanOrder();
        detached.setId(orderId);

        var order = crudService.reload(detached);

        assertEquals(3, order.getLines().size());
        assertEquals(1, order.getLines().getFirst().getSubs().size());
    }

    @Test
    public void findByIdOutsideTransactionInitializesTheDeclaredPlan() {
        var order = crudService.find(LoadPlanOrder.class, orderId);

        assertEquals(3, order.getLines().size());
    }

    @Test
    public void findSingleOutsideTransactionInitializesTheDeclaredPlan() {
        var order = crudService.findSingle(LoadPlanOrder.class, QueryParameters.with("name", "o1"));

        assertEquals(3, order.getLines().size());
        assertEquals(1, order.getLines().getFirst().getSubs().size());
    }

    @Test
    public void callsInsideACallerTransactionDoNotInitializeAnything() {
        tx.executeWithoutResult(s -> {
            var order = crudService.load(LoadPlanOrder.class, orderId);
            var found = crudService.find(LoadPlanOrder.class, orderId);
            var single = crudService.findSingle(LoadPlanOrder.class, QueryParameters.with("name", "o1"));

            assertFalse(Hibernate.isInitialized(order.getLines()), "load joined to the caller transaction must stay lazy");
            assertFalse(Hibernate.isInitialized(found.getLines()));
            assertFalse(Hibernate.isInitialized(single.getLines()));
            // and it still loads on demand while the transaction is open
            assertEquals(3, order.getLines().size());
        });
    }

    @Test
    public void executeWithinTransactionCallbackKeepsEntitiesManaged() {
        crudService.executeWithinTransaction(() -> {
            var order = crudService.reload(new LoadPlanOrderRef(orderId).asOrder());

            assertFalse(Hibernate.isInitialized(order.getLines()));
            assertEquals(3, order.getLines().size());
        });
    }

    // ---------------------------------------------------------------- explicit paths (layer D)

    @Test
    public void loadWithExplicitPathsAddsThemToTheDeclaredPlan() {
        var order = crudService.load(LoadPlanOrder.class, orderId, "notes");

        assertEquals(1, order.getNotes().size());
        assertEquals(3, order.getLines().size(), "the declared plan still applies");
    }

    @Test
    public void reloadWithExplicitPathsAddsThemToTheDeclaredPlan() {
        var detached = crudService.load(LoadPlanOrder.class, orderId);

        var order = crudService.reload(detached, "notes");

        assertEquals(1, order.getNotes().size());
    }

    @Test
    public void loadWithPlanObject() {
        var order = crudService.load(LoadPlanOrder.class, orderId, LoadPlan.of("notes"));

        assertEquals(1, order.getNotes().size());
    }

    @Test
    public void explicitPathsAreHonoredEvenWhenTheModeIsNone() {
        target.setLoadCollectionsMode("none");

        var order = crudService.load(LoadPlanOrder.class, orderId, "notes");

        assertEquals(1, order.getNotes().size());
        assertThrows(LazyInitializationException.class, () -> order.getLines().size(), "declared plan is off in none mode");
    }

    @Test
    public void unknownPathsAreIgnored() {
        var order = crudService.load(LoadPlanOrder.class, orderId, "doesNotExist", "lines.nope");

        assertEquals(3, order.getLines().size());
    }

    // ---------------------------------------------------------------- global mode

    @Test
    public void noneModeDisablesTheDeclaredPlan() {
        target.setLoadCollectionsMode("none");

        var order = crudService.load(LoadPlanOrder.class, orderId);

        assertThrows(LazyInitializationException.class, () -> order.getLines().size());
    }

    @Test
    public void allModeInitializesEveryCollectionToTheConfiguredDepth() {
        target.setLoadCollectionsMode("all");
        target.setLoadDepth(2);

        var order = crudService.load(LoadPlanOrder.class, orderId);

        assertEquals(1, order.getNotes().size());
        assertEquals(1, order.getLines().getFirst().getSubs().size());
    }

    @Test
    public void allModeDepthOneStopsAtTheFirstLevel() {
        target.setLoadCollectionsMode("all");
        target.setLoadDepth(1);
        // lines.subs is declared in the annotation, so use an entity without declaration to see the depth
        var line = crudService.load(LoadPlanLine.class, tx.execute(s -> crudService.find(LoadPlanOrder.class, orderId).getLines().getFirst().getId()));

        assertThrows(LazyInitializationException.class, () -> line.getOrder().getLines().size());
        assertEquals(1, line.getSubs().size());
    }

    // ---------------------------------------------------------------- LoadPlan

    @Test
    public void loadPlanCombinesPaths() {
        var plan = LoadPlan.of("a", "b").and("c").and(LoadPlan.allCollections(2));

        assertTrue(plan.getPaths().containsAll(java.util.List.of("a", "b", "c")));
        assertTrue(plan.isAllCollections());
        assertEquals(2, plan.getDepth());
        assertTrue(LoadPlan.EMPTY.isEmpty());
        assertTrue(LoadPlan.of().isEmpty());
        assertTrue(LoadPlan.of((String) null, " ").isEmpty());
    }

    @Test
    public void annotatedPlanIsReadFromTheEntityClass() {
        assertEquals(java.util.Set.of("lines", "lines.subs"), LoadPlan.annotatedOf(LoadPlanOrder.class).getPaths());
        assertTrue(LoadPlan.annotatedOf(LoadPlanLine.class).isEmpty());
    }

    /** Helper: a detached reference to an order by id. */
    private static final class LoadPlanOrderRef {
        private final Long id;

        LoadPlanOrderRef(Long id) {
            this.id = id;
        }

        LoadPlanOrder asOrder() {
            var o = new LoadPlanOrder();
            o.setId(id);
            return o;
        }
    }
}
