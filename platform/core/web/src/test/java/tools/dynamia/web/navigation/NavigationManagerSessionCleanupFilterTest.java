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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.dynamia.navigation.NavigationManagerSession;
import tools.dynamia.navigation.Page;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Covers {@link NavigationManagerSessionCleanupFilter}: it must clear the thread-local
 * {@link NavigationManagerSession} after the request completes, including when the rest of the
 * chain throws, so pooled request-handling threads don't retain a stale navigation intent across
 * unrelated later requests.
 */
public class NavigationManagerSessionCleanupFilterTest {

    private final NavigationManagerSessionCleanupFilter filter = new NavigationManagerSessionCleanupFilter();

    @AfterEach
    public void cleanup() {
        NavigationManagerSession.clear();
    }

    @Test
    public void shouldClearSessionAfterSuccessfulChain() throws ServletException, IOException {
        Page page = new Page("page", "Page", "the/page");
        NavigationManagerSession.getInstance().setPage(page, null);

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), new MockFilterChain());

        assertNull(NavigationManagerSession.getInstance().getPage());
    }

    @Test
    public void pendingPageShouldStillBeVisibleToTheChain() throws ServletException, IOException {
        Page page = new Page("page", "Page", "the/page");
        NavigationManagerSession.getInstance().setPage(page, null);

        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response) {
                assertSame(page, NavigationManagerSession.getInstance().getPage());
            }
        };

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);
    }

    @Test
    public void shouldClearSessionEvenWhenChainThrows() {
        NavigationManagerSession.getInstance().setPage(new Page("page", "Page", "the/page"), null);

        MockFilterChain failingChain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response) throws ServletException {
                throw new ServletException("boom");
            }
        };

        assertThrows(ServletException.class, () ->
                filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), failingChain));

        assertNull(NavigationManagerSession.getInstance().getPage());
    }
}
