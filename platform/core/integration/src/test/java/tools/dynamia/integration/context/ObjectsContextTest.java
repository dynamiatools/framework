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
package tools.dynamia.integration.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.integration.ThreadLocalObjectContainer;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObjectsContextTest {

    record Branch(String name) {
    }

    record Cashbox(String name) {
    }

    @AfterEach
    void cleanUp() {
        Containers.get().removeAllContainers();
        ThreadLocalObjectContainer.clear();
        ObjectsContext.legacyThreadLocalBridge(true);
    }

    @Test
    void getReturnsNullAndRequireFailsWhenThereIsNothing() {
        assertNull(ObjectsContext.get(Branch.class));
        assertTrue(ObjectsContext.find(Branch.class).isEmpty());
        assertThrows(ContextException.class, () -> ObjectsContext.require(Branch.class));
    }

    @Test
    void withBindsObjectsOnlyWhileTheCodeRuns() {
        var branch = new Branch("north");

        ObjectsContext.with(branch).run(() -> {
            assertSame(branch, ObjectsContext.get(Branch.class));
            assertSame(branch, ObjectsContext.require(Branch.class));
            assertEquals(List.of(branch), List.copyOf(ObjectsContext.getAll(Branch.class)));
        });

        assertNull(ObjectsContext.get(Branch.class));
    }

    @Test
    void nestedScopesAddUpAndTheInnerOneWinsForTheSameType() {
        var north = new Branch("north");
        var south = new Branch("south");
        var cashbox = new Cashbox("c1");

        ObjectsContext.with(north).run(() -> {
            ObjectsContext.with(cashbox).run(() -> {
                assertSame(north, ObjectsContext.get(Branch.class)); // outer object still visible
                assertSame(cashbox, ObjectsContext.get(Cashbox.class));
            });
            ObjectsContext.with(south).run(() -> assertSame(south, ObjectsContext.get(Branch.class)));
            assertSame(north, ObjectsContext.get(Branch.class));
            assertNull(ObjectsContext.get(Cashbox.class));
        });
    }

    @Test
    void namedObjectsAreFoundByNameAndType() {
        var branch = new Branch("named");

        ObjectsContext.with().add("mainBranch", branch).run(() ->
                assertSame(branch, ObjectsContext.get("mainBranch", Branch.class)));
    }

    @Test
    void theBindingIsUndoneWhenTheCodeFails() {
        assertThrows(IllegalStateException.class, () -> ObjectsContext.with(new Branch("x")).run(() -> {
            throw new IllegalStateException("boom");
        }));

        assertNull(ObjectsContext.get(Branch.class));
    }

    @Test
    void getAndCallReturnTheResultAndCallMayThrowCheckedExceptions() throws Exception {
        var branch = new Branch("b");

        assertEquals("b", ObjectsContext.with(branch).get(() -> ObjectsContext.get(Branch.class).name()));
        assertEquals("ok", ObjectsContext.with(branch).call(() -> "ok"));
        assertThrows(java.io.IOException.class, () -> ObjectsContext.with(branch).call(() -> {
            throw new java.io.IOException("io");
        }));
    }

    @Test
    void lookupOrderIsBoundThenLegacyThreadLocalThenSpring() {
        var spring = new SimpleObjectContainer();
        spring.addObject(new Branch("spring"));
        Containers.get().installObjectContainer(spring);
        assertEquals("spring", ObjectsContext.get(Branch.class).name());

        var legacy = new SimpleObjectContainer();
        legacy.addObject(new Branch("legacy"));
        ThreadLocalObjectContainer.set(legacy);
        assertEquals("legacy", ObjectsContext.get(Branch.class).name());

        ObjectsContext.with(new Branch("bound")).run(() -> assertEquals("bound", ObjectsContext.get(Branch.class).name()));
        ObjectsContext.with(new Cashbox("only")).run(() -> assertEquals("legacy", ObjectsContext.get(Branch.class).name()));
    }

    @Test
    void anotherThreadDoesNotInheritTheBindingButASnapshotCarriesIt() throws Exception {
        var branch = new Branch("carried");
        var plain = new AtomicReference<Branch>();
        var wrapped = new AtomicReference<Branch>();

        ObjectsContext.with(branch).run(() -> {
            var snapshot = ObjectsContext.capture();
            try {
                Thread.ofVirtual().start(() -> plain.set(ObjectsContext.get(Branch.class))).join();
                Thread.ofVirtual().start(snapshot.wrap(() -> wrapped.set(ObjectsContext.get(Branch.class)))).join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        assertNull(plain.get());
        assertSame(branch, wrapped.get());
    }

    @Test
    void aSnapshotKeepsEveryLayerOfNestedScopes() throws Exception {
        var seen = new AtomicReference<String>();

        ObjectsContext.with(new Branch("north")).run(() -> ObjectsContext.with(new Cashbox("c9")).run(() -> {
            var snapshot = ObjectsContext.capture();
            try {
                Thread.ofVirtual().start(snapshot.wrap(() ->
                        seen.set(ObjectsContext.get(Branch.class).name() + "/" + ObjectsContext.get(Cashbox.class).name()))).join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));

        assertEquals("north/c9", seen.get());
    }

    @Test
    void snapshotWrapsCallablesAndSuppliers() throws Exception {
        var snapshot = ObjectsContext.with(new Branch("w")).get(ObjectsContext::capture);

        Callable<String> callable = snapshot.wrapCallable(() -> ObjectsContext.get(Branch.class).name());
        var thread = Executors.newVirtualThreadPerTaskExecutor();
        try {
            assertEquals("w", thread.submit(callable).get(5, TimeUnit.SECONDS));
            assertEquals("w", thread.submit(() -> snapshot.wrapSupplier(() -> ObjectsContext.get(Branch.class).name()).get()).get(5, TimeUnit.SECONDS));
        } finally {
            thread.shutdown();
        }
    }

    @Test
    void wrapExecutorGivesEachTaskTheContextOfItsSubmitter() throws Exception {
        var pool = Executors.newFixedThreadPool(1);
        var wrapping = ObjectsContext.wrap(pool);
        var seen = new AtomicReference<String>();
        try {
            ObjectsContext.with(new Branch("submitter")).run(() -> wrapping.execute(() -> seen.set(ObjectsContext.get(Branch.class).name())));
            pool.shutdown();
            assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
        } finally {
            pool.shutdownNow();
        }

        assertEquals("submitter", seen.get());
    }

    @Test
    void capturersContributeStateThatIsReappliedInsideTheTask() throws Exception {
        ScopedValue<String> tenant = ScopedValue.newInstance();
        ContextCapturer capturer = () -> {
            String value = tenant.isBound() ? tenant.get() : null;
            return value == null ? null : new ContextCapturer.Binding() {
                @Override
                public <T, X extends Throwable> T call(ScopedValue.CallableOp<? extends T, X> op) throws X {
                    return ScopedValue.where(tenant, value).call(op);
                }
            };
        };
        var beans = new SimpleObjectContainer();
        beans.addObject(capturer);
        Containers.get().installObjectContainer(beans);
        var seen = new AtomicReference<String>();

        ScopedValue.where(tenant, "acme").run(() -> {
            var snapshot = ObjectsContext.capture();
            try {
                Thread.ofVirtual().start(snapshot.wrap(() -> seen.set(tenant.isBound() ? tenant.get() : "none"))).join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        assertEquals("acme", seen.get());
    }

    @Test
    void theLegacyBridgeFillsTheThreadLocalInsideTheTaskAndRestoresItAfterwards() {
        var outer = new SimpleObjectContainer();
        outer.addObject("outer");
        ThreadLocalObjectContainer.set(outer);
        var inside = new AtomicReference<Object>();

        var snapshot = ObjectsContext.with(new Branch("legacy-visible")).get(ObjectsContext::capture);
        snapshot.run(() -> inside.set(ThreadLocalObjectContainer.getObject(Branch.class)));

        assertEquals(new Branch("legacy-visible"), inside.get());
        assertSame(outer, ThreadLocalObjectContainer.get());
    }

    @Test
    void theLegacyBridgeCanBeSwitchedOff() {
        ObjectsContext.legacyThreadLocalBridge(false);
        var inside = new AtomicReference<Boolean>();

        ObjectsContext.with(new Branch("x")).get(ObjectsContext::capture)
                .run(() -> inside.set(ThreadLocalObjectContainer.isInitialized()));

        assertFalse(ObjectsContext.isLegacyThreadLocalBridge());
        assertEquals(Boolean.FALSE, inside.get());
    }
}
