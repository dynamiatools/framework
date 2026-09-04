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
package tools.dynamia.web;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for {@link MicroFrontendView}, covering both server-side resolution strategies:
 * a {@code view:}-prefixed Spring {@link ViewResolver} lookup and a {@code classpath:}-prefixed
 * direct resource read.
 */
public class MicroFrontendViewTest {

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @Before
    public void setUp() {
        Containers.get().removeAllContainers();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @After
    public void tearDown() {
        Containers.get().removeAllContainers();
    }

    @Test
    public void shouldRecognizeServerResolvedPrefixes() {
        assertTrue(MicroFrontendView.isServerResolved("view:someapp/index"));
        assertTrue(MicroFrontendView.isServerResolved("classpath:microfrontends/someapp/index.html"));
        assertTrue(!MicroFrontendView.isServerResolved("/static/next/subscription"));
        assertTrue(!MicroFrontendView.isServerResolved(null));
    }

    @Test
    public void shouldResolveClasspathApp() {
        MicroFrontendView.Result result = MicroFrontendView.resolve(
                "classpath:microfrontends/testapp/index.html", request, response);

        assertEquals("/static/testapp/bundle.js", result.src());
        assertEquals("/static/testapp/style.css", result.css());
        assertTrue(result.bodyHtml().contains("mount point"));
    }

    @Test
    public void shouldFailWhenClasspathResourceMissing() {
        try {
            MicroFrontendView.resolve("classpath:microfrontends/testapp/missing.html", request, response);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("not found"));
        }
    }

    @Test
    public void shouldRejectRelativeScriptSrc() {
        try {
            MicroFrontendView.resolve("classpath:microfrontends/testapp/relative-src.html", request, response);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("absolute"));
        }
    }

    @Test
    public void shouldFailWhenNoModuleScriptFound() {
        try {
            MicroFrontendView.resolve("classpath:microfrontends/testapp/no-script.html", request, response);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("script"));
        }
    }

    @Test
    public void shouldRejectUnsupportedPrefix() {
        try {
            MicroFrontendView.resolve("/static/next/subscription", request, response);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("/static/next/subscription"));
        }
    }

    @Test
    public void shouldResolveViewThroughRegisteredViewResolver() {
        View view = (model, req, res) -> res.getWriter().write(
                "<html><body><div id=\"app\"></div>"
                        + "<link rel=\"stylesheet\" href=\"/static/someapp/style.css\">"
                        + "<script type=\"module\" src=\"/static/someapp/index.js\"></script>"
                        + "</body></html>");

        ViewResolver resolver = (viewName, locale) -> "someapp/index".equals(viewName) ? view : null;

        SimpleObjectContainer container = new SimpleObjectContainer();
        container.addObject("microfrontendViewResolver", resolver);
        Containers.get().installObjectContainer(container);

        MicroFrontendView.Result result = MicroFrontendView.resolve("view:someapp/index", request, response);

        assertEquals("/static/someapp/index.js", result.src());
        assertEquals("/static/someapp/style.css", result.css());
    }

    @Test
    public void shouldFailWhenNoViewResolverMatches() {
        SimpleObjectContainer container = new SimpleObjectContainer();
        container.addObject("noopViewResolver", (ViewResolver) (viewName, locale) -> null);
        Containers.get().installObjectContainer(container);

        try {
            MicroFrontendView.resolve("view:unknown/index", request, response);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("No ViewResolver"));
        }
    }
}
