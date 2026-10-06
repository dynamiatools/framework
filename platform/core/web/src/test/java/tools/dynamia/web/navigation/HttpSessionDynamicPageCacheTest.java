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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.dynamia.navigation.Page;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Verifies that {@link HttpSessionDynamicPageCache} scopes pages to the HTTP session.
 */
class HttpSessionDynamicPageCacheTest {

    private final HttpSessionDynamicPageCache cache = new HttpSessionDynamicPageCache();

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    private static void bind(MockHttpServletRequest request) {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void putAndGetWithinSameSession() {
        var session = new MockHttpSession();
        var request = new MockHttpServletRequest();
        request.setSession(session);
        bind(request);

        var page = new Page("p1", "Page 1", "path1");
        cache.put("path:a/b", page);

        assertSame(page, cache.get("path:a/b"));
        assertNull(cache.get("path:other"));
    }

    @Test
    void pagesAreNotSharedBetweenSessions() {
        var first = new MockHttpServletRequest();
        first.setSession(new MockHttpSession());
        bind(first);
        cache.put("path:a/b", new Page("p1", "Page 1", "path1"));

        var second = new MockHttpServletRequest();
        second.setSession(new MockHttpSession());
        bind(second);

        assertNull(cache.get("path:a/b"));
    }

    @Test
    void withoutRequestItDoesNothing() {
        cache.put("path:a/b", new Page("p1", "Page 1", "path1"));
        assertNull(cache.get("path:a/b"));
    }

}
