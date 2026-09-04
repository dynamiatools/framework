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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import tools.dynamia.integration.Containers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves a {@code tools.dynamia.zk.ui.MicroFrontend}'s {@code app} entry HTML server-side, in
 * process, when it uses one of two prefixes, instead of requiring the bundle to be copied into
 * {@code src/main/resources/static/}:
 * <ul>
 *     <li>{@value #VIEW_PREFIX} — the rest of the string is a view name resolved and rendered
 *     through the application's registered {@link ViewResolver} chain (e.g.
 *     {@link ClassPathViewResolver}, which resolves a classpath {@code views/*.html} resource, or
 *     any other resolver a module registers), exactly like any regular Spring MVC
 *     {@code ModelAndView}.</li>
 *     <li>{@value #CLASSPATH_PREFIX} — the rest of the string is read directly as a
 *     {@link ClassPathResource} via {@link ClassPathView}, bypassing the {@link ViewResolver} chain
 *     entirely for the common case of "just serve this HTML file from my module's jar".</li>
 * </ul>
 * Both resolve entirely within the current request (no dedicated HTTP endpoint is exposed for
 * this), so the resolved view is subject to the exact same security context as the ZK page
 * embedding it — there is no separate "render any registered view by name" surface to secure.
 * <p>
 * The resulting HTML is parsed the same way the client parses a static {@code app} folder's
 * {@code index.html}: a {@code <script type="module" src="...">} tag (required), any
 * {@code <link rel="stylesheet" href="...">} tags, and the {@code <body>} markup stripped of
 * {@code <script>} tags (used only for {@code MicroFrontend.MODE_AUTO}). Since there is no real
 * folder backing a {@code view:}/{@code classpath:} app for relative URLs to resolve against, every
 * discovered {@code src}/{@code href} must be absolute or root-relative.
 *
 * @author Mario A. Serrano Leones
 */
public final class MicroFrontendView {

    /** Prefix resolving the rest of the {@code app} string as a Spring MVC view name. */
    public static final String VIEW_PREFIX = "view:";
    /** Prefix resolving the rest of the {@code app} string as a classpath resource path. */
    public static final String CLASSPATH_PREFIX = "classpath:";

    private static final Pattern SCRIPT_MODULE = Pattern.compile("<script[^>]*type=[\"']module[\"'][^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern LINK_STYLESHEET = Pattern.compile("<link[^>]*rel=[\"']stylesheet[\"'][^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern SRC_ATTR = Pattern.compile("\\ssrc=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
    private static final Pattern HREF_ATTR = Pattern.compile("\\shref=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
    private static final Pattern BODY = Pattern.compile("<body[^>]*>([\\s\\S]*)</body>", Pattern.CASE_INSENSITIVE);
    private static final Pattern SCRIPT_TAG = Pattern.compile("<script[\\s\\S]*?</script>", Pattern.CASE_INSENSITIVE);
    private static final Pattern ABSOLUTE_URL = Pattern.compile("^([a-zA-Z][\\w+.-]*:)?/.*");

    private MicroFrontendView() {
    }

    /**
     * Whether {@code app} uses {@link #VIEW_PREFIX} or {@link #CLASSPATH_PREFIX} and should be
     * resolved server-side through {@link #resolve(String, HttpServletRequest, HttpServletResponse)}
     * instead of being sent to the client as-is.
     *
     * @param app the {@code MicroFrontend#app} value, possibly null
     */
    public static boolean isServerResolved(String app) {
        return app != null && (app.startsWith(VIEW_PREFIX) || app.startsWith(CLASSPATH_PREFIX));
    }

    /**
     * Resolves and renders {@code app} into its bundle {@code src}, stylesheet {@code css} and
     * (for {@code MicroFrontend.MODE_AUTO}) mount-target {@code bodyHtml}, ready to send to the
     * client as an already-resolved config, skipping the client's own {@code fetch}-based discovery.
     *
     * @param app      the prefixed {@code app} value, see {@link #isServerResolved(String)}
     * @param request  the current request, used to resolve/render the Spring view (locale, etc.)
     * @param response the current response, only used as a template for the wrapper that captures
     *                 the rendered output; nothing is written to the real response
     * @return the resolved bundle {@code src}/{@code css}/{@code bodyHtml}
     * @throws IllegalArgumentException if {@code app} doesn't use a recognized prefix
     * @throws IllegalStateException    if the view/resource can't be resolved or rendered, or the
     *                                  resolved HTML doesn't follow the expected shape (see class
     *                                  Javadoc)
     */
    public static Result resolve(String app, HttpServletRequest request, HttpServletResponse response) {
        String html = renderHtml(app, request, response);
        return parse(html);
    }

    private static String renderHtml(String app, HttpServletRequest request, HttpServletResponse response) {
        View view;
        if (app.startsWith(VIEW_PREFIX)) {
            view = resolveRegisteredView(app.substring(VIEW_PREFIX.length()), request);
        } else if (app.startsWith(CLASSPATH_PREFIX)) {
            String path = app.substring(CLASSPATH_PREFIX.length());
            ClassPathResource resource = new ClassPathResource(path);
            if (!resource.exists()) {
                throw new IllegalStateException("Microfrontend classpath resource not found: " + path);
            }
            view = new ClassPathView(resource, "text/html;charset=UTF-8");
        } else {
            throw new IllegalArgumentException("Unsupported microfrontend app prefix (expected \"" + VIEW_PREFIX +
                    "\" or \"" + CLASSPATH_PREFIX + "\"): " + app);
        }
        return render(view, request, response);
    }

    /**
     * Resolves {@code viewName} through every registered {@link ViewResolver} bean, in order,
     * mirroring how Spring's {@code DispatcherServlet} itself chains them: the first resolver to
     * return a non-null {@link View} wins.
     */
    private static View resolveRegisteredView(String viewName, HttpServletRequest request) {
        List<ViewResolver> resolvers = new ArrayList<>(Containers.get().findObjects(ViewResolver.class));
        AnnotationAwareOrderComparator.sort(resolvers);
        Locale locale = request != null ? request.getLocale() : Locale.getDefault();
        for (ViewResolver resolver : resolvers) {
            try {
                View view = resolver.resolveViewName(viewName, locale);
                if (view != null) {
                    return view;
                }
            } catch (Exception e) {
                throw new IllegalStateException("Error resolving microfrontend view: " + viewName, e);
            }
        }
        throw new IllegalStateException("No ViewResolver could resolve microfrontend view: " + viewName);
    }

    /**
     * Renders {@code view} into a string instead of the real response, using a
     * {@link BufferedHttpServletResponse} wrapper so nothing reaches the client directly.
     */
    private static String render(View view, HttpServletRequest request, HttpServletResponse response) {
        BufferedHttpServletResponse buffer = new BufferedHttpServletResponse(response);
        try {
            view.render(Map.of(), request, buffer);
        } catch (Exception e) {
            throw new IllegalStateException("Error rendering microfrontend view", e);
        }
        return buffer.getContent();
    }

    /**
     * Extracts the bundle {@code src}, stylesheet {@code css} and mount-target {@code bodyHtml} from
     * rendered HTML, the server-side equivalent of the client's {@code dynamiaResolveApp} discovery.
     */
    private static Result parse(String html) {
        Matcher scriptMatcher = SCRIPT_MODULE.matcher(html);
        String src = null;
        if (scriptMatcher.find()) {
            Matcher srcMatcher = SRC_ATTR.matcher(scriptMatcher.group());
            if (srcMatcher.find()) {
                src = srcMatcher.group(1);
            }
        }
        if (src == null) {
            throw new IllegalStateException("No <script type=\"module\" src=\"...\"> found in resolved microfrontend HTML");
        }
        requireAbsolute("script src", src);

        List<String> cssHrefs = new ArrayList<>();
        Matcher linkMatcher = LINK_STYLESHEET.matcher(html);
        while (linkMatcher.find()) {
            Matcher hrefMatcher = HREF_ATTR.matcher(linkMatcher.group());
            if (hrefMatcher.find()) {
                String href = hrefMatcher.group(1);
                requireAbsolute("stylesheet href", href);
                cssHrefs.add(href);
            }
        }

        String bodyHtml = "";
        Matcher bodyMatcher = BODY.matcher(html);
        if (bodyMatcher.find()) {
            bodyHtml = SCRIPT_TAG.matcher(bodyMatcher.group(1)).replaceAll("");
        }

        return new Result(src, cssHrefs.isEmpty() ? null : String.join(",", cssHrefs), bodyHtml);
    }

    private static void requireAbsolute(String what, String url) {
        if (!ABSOLUTE_URL.matcher(url).matches()) {
            throw new IllegalStateException("Microfrontend " + what + " must be absolute or root-relative (no folder backs a "
                    + VIEW_PREFIX + "/" + CLASSPATH_PREFIX + " app to resolve a relative URL against): " + url);
        }
    }

    /** The resolved bundle {@code src}, stylesheet {@code css} (comma-separated, may be null) and {@code bodyHtml}. */
    public record Result(String src, String css, String bodyHtml) {
    }

    /**
     * Captures a {@link View}'s rendered output into a buffer instead of writing it to the real
     * response.
     */
    private static class BufferedHttpServletResponse extends HttpServletResponseWrapper {

        private final StringWriter buffer = new StringWriter();
        private final PrintWriter writer = new PrintWriter(buffer);

        BufferedHttpServletResponse(HttpServletResponse response) {
            super(response);
        }

        @Override
        public PrintWriter getWriter() {
            return writer;
        }

        String getContent() {
            writer.flush();
            return buffer.toString();
        }
    }
}
