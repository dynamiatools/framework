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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import tools.dynamia.navigation.NavigationElement;

import static tools.dynamia.navigation.NavigationElement.PATH_SEPARATOR;

/**
 * Renders a single {@link tools.dynamia.navigation.Page} on its own, with no application shell
 * (no header, sidebar or footer) around it — just the page's own content inside the workspace.
 * <p>
 * Mirrors every URL pattern {@link PageNavigationController} supports (same
 * {@code /module/group/.../page} path shapes), but resolves to the {@code "embed"} view instead
 * of {@code "index"}. For a ZK-rendered page, {@code "embed"} resolves (via the framework's
 * existing {@code classpathZulViewResolver}) to {@code classpath:/web/views/embed.zul}, a minimal
 * ZUL layout containing only the workspace div — no theme-specific chrome, regardless of which
 * {@code ApplicationTemplate} is currently active.
 * <p>
 * Intended for non-ZK frontends (e.g. a Vue theme's {@code <dynamia-embed>}) that need to iframe
 * one ZK page's content directly, instead of the full app shell {@code PageNavigationController}
 * renders at {@code /page/**}.
 *
 * @author Mario A. Serrano Leones
 */
@Controller("pageEmbedController")
@RequestMapping("/page-embed")
public class PageEmbedController {

    private static final String VIEW_NAME = "embed";

    @RequestMapping()
    public ModelAndView route(HttpServletRequest request, HttpServletResponse response) {
        var pagePath = request.getRequestURI();
        if (pagePath.startsWith("/page-embed/")) {
            pagePath = pagePath.replaceFirst("/page-embed/", "");
        }
        return PageNavigationController.navigate(pagePath, VIEW_NAME, request, response);
    }

    @RequestMapping(value = "/{module}/{group}/{page}", method = RequestMethod.GET)
    public ModelAndView defaultPages(@PathVariable String module, @PathVariable String group, @PathVariable String page,
                                      HttpServletRequest request, HttpServletResponse response) {
        String path = module + NavigationElement.PATH_SEPARATOR + group + NavigationElement.PATH_SEPARATOR + page;
        return PageNavigationController.navigate(path, VIEW_NAME, request, response);
    }

    @RequestMapping(value = "/{module}/{page}", method = RequestMethod.GET)
    public ModelAndView directPages(@PathVariable String module, @PathVariable String page,
                                     HttpServletRequest request, HttpServletResponse response) {
        String path = module + NavigationElement.PATH_SEPARATOR + page;
        return PageNavigationController.navigate(path, VIEW_NAME, request, response);
    }

    @RequestMapping(value = "/{module}/{group}/{subgroup}/{page}", method = RequestMethod.GET)
    public ModelAndView twoGroupsPages(@PathVariable String module, @PathVariable String group,
                                       @PathVariable String subgroup, @PathVariable String page,
                                       HttpServletRequest request, HttpServletResponse response) {
        String path = module + NavigationElement.PATH_SEPARATOR + group + NavigationElement.PATH_SEPARATOR + subgroup + NavigationElement.PATH_SEPARATOR + page;
        return PageNavigationController.navigate(path, VIEW_NAME, request, response);
    }

    @RequestMapping(value = "/{module}/{group}/{subgroup}/{subgroup2}/{page}", method = RequestMethod.GET)
    public ModelAndView threeGroupsPages(@PathVariable String module, @PathVariable String group,
                                          @PathVariable String subgroup, @PathVariable String subgroup2, @PathVariable String page,
                                          HttpServletRequest request, HttpServletResponse response) {
        String path = module + NavigationElement.PATH_SEPARATOR + group + PATH_SEPARATOR + subgroup + PATH_SEPARATOR + subgroup2 + PATH_SEPARATOR + page;
        return PageNavigationController.navigate(path, VIEW_NAME, request, response);
    }

    @RequestMapping(value = "/{module}/{group}/{subgroup}/{subgroup2}/{subgroup3}/{page}", method = RequestMethod.GET)
    public ModelAndView fourGroupsPages(@PathVariable String module, @PathVariable String group,
                                         @PathVariable String subgroup, @PathVariable String subgroup2, @PathVariable String subgroup3,
                                         @PathVariable String page, HttpServletRequest request, HttpServletResponse response) {
        String path = module + NavigationElement.PATH_SEPARATOR + group + PATH_SEPARATOR + subgroup + PATH_SEPARATOR + subgroup2 + PATH_SEPARATOR + subgroup3 + PATH_SEPARATOR + page;
        return PageNavigationController.navigate(path, VIEW_NAME, request, response);
    }
}
