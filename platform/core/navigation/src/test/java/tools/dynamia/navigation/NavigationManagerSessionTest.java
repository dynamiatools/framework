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
package tools.dynamia.navigation;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers {@link NavigationManagerSession}'s {@link ScopedValue}-backed behavior: it replaced a
 * {@code @Scope("session")} bean specifically to isolate concurrent requests (e.g. several ZK
 * desktops/iframes bootstrapping at once in the same HTTP session) from each other, so the key
 * property under test is isolation across scopes, not just the plain getter/setter contract.
 * <p>
 * In production the scope is bound by {@code NavigationManagerSessionScopeFilter} around the whole
 * request; these tests bind it themselves via {@link #inScope} to exercise the class in isolation.
 */
public class NavigationManagerSessionTest {

    private void inScope(Runnable body) {
        ScopedValue.where(NavigationManagerSession.SCOPE, new NavigationManagerSession()).run(body);
    }

    @Test
    public void getInstanceOutsideAScopeShouldThrow() {
        assertThrows(NoSuchElementException.class, NavigationManagerSession::getInstance);
    }

    @Test
    public void setPageAndGetPageShouldRoundtripWithinAScope() {
        Page page = new Page("page", "Page", "the/page");
        Map<String, java.io.Serializable> params = new HashMap<>();
        params.put("k", "v");

        inScope(() -> {
            NavigationManagerSession.getInstance().setPage(page, params);

            assertSame(page, NavigationManagerSession.getInstance().getPage());
            assertEquals(params, NavigationManagerSession.getInstance().getPageParams());
        });
    }

    @Test
    public void currentShouldBeNullOutsideAScopeAndTheBoundInstanceInside() {
        assertNull(NavigationManagerSession.current());

        inScope(() -> assertSame(NavigationManagerSession.getInstance(), NavigationManagerSession.current()));
    }

    @Test
    public void parkedStateShouldBePulledLazilyAndOnlyOnceWhenADesktopConsumes() {
        Page page = new Page("page", "Page", "the/page");
        List<Integer> executed = new ArrayList<>();
        int[] pulls = {0};

        var parked = new NavigationManagerSession();
        parked.setPage(page, null);
        parked.runLater(() -> executed.add(1));

        var session = new NavigationManagerSession();
        session.setParkedStateSupplier(() -> {
            pulls[0]++;
            return parked;
        });

        assertEquals(0, pulls[0], "installing the hook must not pull anything");
        assertTrue(!session.hasPendingState());

        TestNavigationManager navManager = new TestNavigationManager();
        session.updateNavManager(navManager);
        session.executeQueue();
        session.executeQueue();

        assertEquals(1, pulls[0]);
        assertSame(page, navManager.getCurrentPage());
        assertEquals(List.of(1), executed);
    }

    @Test
    public void executeQueueShouldPullParkedStateWhenNoPageWasConsumedFirst() {
        List<Integer> executed = new ArrayList<>();
        var parked = new NavigationManagerSession();
        parked.runLater(() -> executed.add(7));

        var session = new NavigationManagerSession();
        session.setParkedStateSupplier(() -> parked);
        session.executeQueue();

        assertEquals(List.of(7), executed);
    }

    @Test
    public void executeQueueShouldNotLoopForeverWhenACallbackQueuesAnother() {
        var session = new NavigationManagerSession();
        int[] runs = {0};
        session.runLater(new tools.dynamia.commons.Callback() {
            @Override
            public void doSomething() {
                runs[0]++;
                session.runLater(this);
            }
        });

        session.executeQueue();

        assertEquals(1, runs[0]);
        assertTrue(session.hasPendingState(), "the re-queued callback waits for the next drain");
    }

    @Test
    public void executeQueueShouldRunTheRestWhenACallbackFails() {
        List<Integer> executed = new ArrayList<>();
        var session = new NavigationManagerSession();
        session.runLater(() -> executed.add(1));
        session.runLater(() -> {
            throw new IllegalStateException("boom");
        });
        session.runLater(() -> executed.add(3));

        session.executeQueue();

        assertEquals(List.of(1, 3), executed);
        assertTrue(!session.hasPendingState());
    }

    @Test
    public void getInstanceShouldReturnSameInstanceWithinAScope() {
        inScope(() -> assertSame(NavigationManagerSession.getInstance(), NavigationManagerSession.getInstance()));
    }

    @Test
    public void updateNavManagerShouldConsumeAndClearPendingPage() {
        Page page = new Page("page", "Page", "the/page");

        inScope(() -> {
            NavigationManagerSession.getInstance().setPage(page, null);

            TestNavigationManager navManager = new TestNavigationManager();
            NavigationManagerSession.getInstance().updateNavManager(navManager);

            assertSame(page, navManager.getCurrentPage());
            assertNull(NavigationManagerSession.getInstance().getPage());
            assertNull(NavigationManagerSession.getInstance().getPageParams());
        });
    }

    @Test
    public void runLaterShouldQueueAndExecuteInOrder() {
        List<Integer> executed = new ArrayList<>();

        inScope(() -> {
            NavigationManagerSession.getInstance().runLater(() -> executed.add(1));
            NavigationManagerSession.getInstance().runLater(() -> executed.add(2));
            NavigationManagerSession.getInstance().runLater(() -> executed.add(3));

            NavigationManagerSession.getInstance().executeQueue();
        });

        assertEquals(List.of(1, 2, 3), executed);
    }

    @Test
    public void executeQueueShouldDrainTheQueue() {
        inScope(() -> {
            NavigationManagerSession.getInstance().runLater(() -> {
            });
            NavigationManagerSession.getInstance().executeQueue();

            // a second call must be a no-op, not re-run anything or fail
            NavigationManagerSession.getInstance().executeQueue();
        });
    }

    @Test
    public void hasPendingStateShouldReflectPageAndQueue() {
        inScope(() -> {
            var session = NavigationManagerSession.getInstance();
            assertTrue(!session.hasPendingState());

            session.runLater(() -> {
            });
            assertTrue(session.hasPendingState());
            session.executeQueue();
            assertTrue(!session.hasPendingState());

            session.setPage(new Page("page", "Page", "the/page"), null);
            assertTrue(session.hasPendingState());
        });
    }

    @Test
    public void absorbShouldMoveThePageAndAppendTheQueue() {
        List<Integer> executed = new ArrayList<>();
        Page page = new Page("page", "Page", "the/page");
        Map<String, java.io.Serializable> params = new HashMap<>();
        params.put("k", "v");

        var source = new NavigationManagerSession();
        source.setPage(page, params);
        source.runLater(() -> executed.add(2));

        var target = new NavigationManagerSession();
        target.runLater(() -> executed.add(1));
        target.absorb(source);
        target.executeQueue();

        assertSame(page, target.getPage());
        assertEquals(params, target.getPageParams());
        assertEquals(List.of(1, 2), executed);
        assertTrue(!source.hasPendingState());
    }

    @Test
    public void scopeShouldNotLeakOutsideItsDynamicExtent() {
        inScope(() -> NavigationManagerSession.getInstance().setPage(new Page("page", "Page", "the/page"), null));

        assertThrows(NoSuchElementException.class, NavigationManagerSession::getInstance);
    }

    /**
     * The whole point of moving away from session scope: a page bound in one scope must not leak
     * into a different, concurrently-running scope — e.g. two tabs/iframes bootstrapping at the same
     * time in the same HTTP session, each on its own thread, each binding its own scope.
     */
    @Test
    public void pendingPageBoundInOneScopeShouldNotBeVisibleInAnotherConcurrentScope() throws Exception {
        Page pageA = new Page("a", "A", "the/a");
        Page pageB = new Page("b", "B", "the/b");
        java.util.concurrent.CountDownLatch bothBound = new java.util.concurrent.CountDownLatch(2);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Page> observedByA = executor.submit(() -> observeOwnPageAfterBothAreBound(pageA, bothBound));
            Future<Page> observedByB = executor.submit(() -> observeOwnPageAfterBothAreBound(pageB, bothBound));

            assertSame(pageA, observedByA.get(5, TimeUnit.SECONDS));
            assertSame(pageB, observedByB.get(5, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    private Page observeOwnPageAfterBothAreBound(Page page, java.util.concurrent.CountDownLatch bothBound) {
        Page[] observed = new Page[1];
        inScope(() -> {
            NavigationManagerSession.getInstance().setPage(page, null);
            bothBound.countDown();
            try {
                bothBound.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            observed[0] = NavigationManagerSession.getInstance().getPage();
        });
        return observed[0];
    }
}
