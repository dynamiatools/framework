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
package tools.dynamia.web.navigation;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.dynamia.navigation.NavigationManagerSession;
import tools.dynamia.navigation.Page;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers {@link NavigationManagerSessionScopeFilter}: it must bind a fresh
 * {@link NavigationManagerSession} for the dynamic extent of the request (so a controller can stash
 * a pending page and a later forward can consume it), and that binding must be torn down once the
 * request finishes — including when the chain throws — with no leakage into an unrelated request
 * that happens to reuse the same thread. The concurrency test is the actual property motivating the
 * whole redesign: two "requests" (e.g. two iframes bootstrapping at once) must not see each other's
 * pending page.
 */
public class NavigationManagerSessionScopeFilterTest {

    private final NavigationManagerSessionScopeFilter filter = new NavigationManagerSessionScopeFilter();

    @Test
    public void chainShouldSeeItsOwnPendingPage() throws ServletException, IOException {
        Page page = new Page("page", "Page", "the/page");

        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response) {
                NavigationManagerSession.getInstance().setPage(page, null);
                assertSame(page, NavigationManagerSession.getInstance().getPage());
            }
        };

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);
    }

    @Test
    public void noScopeShouldBeBoundBeforeTheFilterRuns() {
        assertThrows(NoSuchElementException.class, NavigationManagerSession::getInstance);
    }

    @Test
    public void scopeShouldNotBeVisibleAfterFilterReturns() throws ServletException, IOException {
        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), new MockFilterChain());

        assertThrows(NoSuchElementException.class, NavigationManagerSession::getInstance);
    }

    @Test
    public void scopeShouldBeTornDownEvenWhenChainThrows() {
        MockFilterChain failingChain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response) throws ServletException {
                NavigationManagerSession.getInstance().setPage(new Page("page", "Page", "the/page"), null);
                throw new ServletException("boom");
            }
        };

        assertThrows(ServletException.class, () ->
                filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), failingChain));

        assertThrows(NoSuchElementException.class, NavigationManagerSession::getInstance);
    }

    /**
     * Simulates two ZK desktops (e.g. two {@code <iframe>}s) bootstrapping concurrently through the
     * same filter instance, each on its own thread: each must only ever see its own pending page,
     * never the other's.
     */
    @Test
    public void concurrentRequestsShouldGetIsolatedSessions() throws Exception {
        Page pageA = new Page("a", "A", "the/a");
        Page pageB = new Page("b", "B", "the/b");
        CountDownLatch bothSetTheirPage = new CountDownLatch(2);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Page> futureA = executor.submit(() -> runThroughFilter(pageA, bothSetTheirPage));
            Future<Page> futureB = executor.submit(() -> runThroughFilter(pageB, bothSetTheirPage));

            assertSame(pageA, futureA.get(5, TimeUnit.SECONDS));
            assertSame(pageB, futureB.get(5, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    private Page runThroughFilter(Page page, CountDownLatch bothSetTheirPage) throws ServletException, IOException {
        AtomicReference<Page> observed = new AtomicReference<>();
        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response) throws ServletException {
                NavigationManagerSession.getInstance().setPage(page, null);
                bothSetTheirPage.countDown();
                try {
                    // wait until both threads have set their own page before reading back, so a
                    // shared/leaky binding would actually have a chance to show the wrong page
                    if (!bothSetTheirPage.await(5, TimeUnit.SECONDS)) {
                        throw new ServletException("timed out waiting for the other request");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new ServletException(e);
                }
                observed.set(NavigationManagerSession.getInstance().getPage());
            }
        };

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);
        return observed.get();
    }
}
