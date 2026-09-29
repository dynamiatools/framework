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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers {@link NavigationManagerSession}'s thread-local behavior: it replaced a
 * {@code @Scope("session")} bean specifically to isolate concurrent requests (e.g. several ZK
 * desktops/iframes bootstrapping at once in the same HTTP session) from each other, so the key
 * property under test is isolation across threads, not just the plain getter/setter contract.
 */
public class NavigationManagerSessionTest {

    @AfterEach
    public void cleanup() {
        NavigationManagerSession.clear();
    }

    @Test
    public void setPageAndGetPageShouldRoundtripOnSameThread() {
        Page page = new Page("page", "Page", "the/page");
        Map<String, java.io.Serializable> params = new HashMap<>();
        params.put("k", "v");

        NavigationManagerSession.getInstance().setPage(page, params);

        assertSame(page, NavigationManagerSession.getInstance().getPage());
        assertEquals(params, NavigationManagerSession.getInstance().getPageParams());
    }

    @Test
    public void getInstanceShouldReturnSameInstanceWithinAThread() {
        assertSame(NavigationManagerSession.getInstance(), NavigationManagerSession.getInstance());
    }

    @Test
    public void updateNavManagerShouldConsumeAndClearPendingPage() {
        Page page = new Page("page", "Page", "the/page");
        NavigationManagerSession.getInstance().setPage(page, null);

        TestNavigationManager navManager = new TestNavigationManager();
        NavigationManagerSession.getInstance().updateNavManager(navManager);

        assertSame(page, navManager.getCurrentPage());
        assertNull(NavigationManagerSession.getInstance().getPage());
        assertNull(NavigationManagerSession.getInstance().getPageParams());
    }

    @Test
    public void runLaterShouldQueueAndExecuteInOrder() {
        List<Integer> executed = new ArrayList<>();
        NavigationManagerSession.getInstance().runLater(() -> executed.add(1));
        NavigationManagerSession.getInstance().runLater(() -> executed.add(2));
        NavigationManagerSession.getInstance().runLater(() -> executed.add(3));

        NavigationManagerSession.getInstance().executeQueue();

        assertEquals(List.of(1, 2, 3), executed);
    }

    @Test
    public void executeQueueShouldDrainTheQueue() {
        NavigationManagerSession.getInstance().runLater(() -> {
        });
        NavigationManagerSession.getInstance().executeQueue();

        // a second call must be a no-op, not re-run anything or fail
        NavigationManagerSession.getInstance().executeQueue();
    }

    @Test
    public void clearShouldDropTheCurrentThreadInstance() {
        Page page = new Page("page", "Page", "the/page");
        NavigationManagerSession session = NavigationManagerSession.getInstance();
        session.setPage(page, null);

        NavigationManagerSession.clear();

        NavigationManagerSession afterClear = NavigationManagerSession.getInstance();
        assertNull(afterClear.getPage());
    }

    /**
     * The whole point of moving away from session scope: a page/params set on one thread must not
     * leak into a different thread handling a concurrent request (e.g. another tab/iframe
     * bootstrapping at the same time in the same HTTP session).
     */
    @Test
    public void pendingPageSetOnOneThreadShouldNotBeVisibleOnAnother() throws Exception {
        Page page = new Page("page", "Page", "the/page");
        NavigationManagerSession.getInstance().setPage(page, null);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Page> otherThreadPage = executor.submit(() -> NavigationManagerSession.getInstance().getPage());
            assertNull(otherThreadPage.get(5, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }

        // still present on the original thread
        assertSame(page, NavigationManagerSession.getInstance().getPage());
    }
}
