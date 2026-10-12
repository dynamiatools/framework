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
import tools.dynamia.integration.Containers;
import tools.dynamia.navigation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import static tools.dynamia.navigation.NavigationElement.PATH_SEPARATOR;

/**
 * Controller responsible for handling page navigation requests in the web application. It maps incoming HTTP requests to specific page paths and manages the navigation flow based on the requested URL structure. The controller supports various URL patterns to accommodate different levels of page hierarchy, allowing for flexible navigation within the application. It also handles access restrictions and integrates with page navigation interceptors to provide additional functionality during the navigation process.
 *
 * @author Mario A. Serrano Leones
 */
@Controller("pageNavigationController")
@RequestMapping("/page")
public class PageNavigationController {

    /**
     * A last path segment with a file extension ({@code logo.png}, {@code app.min.js}) is a static resource.
     */
    private static final Pattern STATIC_RESOURCE = Pattern.compile(".*/[^/]*\\.[A-Za-z0-9]{1,10}$");

    /**
     * Accepted values of the {@code zoom} request parameter: a plain number, optionally a percentage (80, 0.8, 90%).
     */
    private static final Pattern ZOOM = Pattern.compile("\\d{1,3}(\\.\\d{1,3})?%?");

    @RequestMapping()
    public ModelAndView route(HttpServletRequest request, HttpServletResponse response) {

        var pagePath = request.getRequestURI();
        if (pagePath.startsWith("/page/")) {
            pagePath = pagePath.replaceFirst("/page/", "");
        }

        return PageNavigationController.navigate(pagePath, request, response);
    }

    @RequestMapping(value = "/{module}/{group}/{page}", method = RequestMethod.GET)
    public ModelAndView defaultPages(@PathVariable String module, @PathVariable String group, @PathVariable String page,
                                     HttpServletRequest request, HttpServletResponse response) {

        String path = module + NavigationElement.PATH_SEPARATOR + group + NavigationElement.PATH_SEPARATOR + page;

        return navigate(path, request, response);

    }

    @RequestMapping(value = "/{module}/{page}", method = RequestMethod.GET)
    public ModelAndView directPages(@PathVariable String module, @PathVariable String page,
                                    HttpServletRequest request, HttpServletResponse response) {

        String path = module + NavigationElement.PATH_SEPARATOR + page;
        return navigate(path, request, response);

    }

    @RequestMapping(value = "/{module}/{group}/{subgroup}/{page}", method = RequestMethod.GET)
    public ModelAndView twoGroupsPages(@PathVariable String module, @PathVariable String group,
                                       @PathVariable String subgroup, @PathVariable String page,
                                       HttpServletRequest request, HttpServletResponse response) {

        String path = module + NavigationElement.PATH_SEPARATOR + group + NavigationElement.PATH_SEPARATOR + subgroup + NavigationElement.PATH_SEPARATOR + page;
        return navigate(path, request, response);
    }

    @RequestMapping(value = "/{module}/{group}/{subgroup}/{subgroup2}/{page}", method = RequestMethod.GET)
    public ModelAndView threeGroupsPages(@PathVariable String module, @PathVariable String group,
                                         @PathVariable String subgroup, @PathVariable String subgroup2, @PathVariable String page, HttpServletRequest request
            , HttpServletResponse response) {

        String path = module + NavigationElement.PATH_SEPARATOR + group + PATH_SEPARATOR + subgroup + PATH_SEPARATOR + subgroup2 + PATH_SEPARATOR + page;
        return navigate(path, request, response);
    }

    @RequestMapping(value = "/{module}/{group}/{subgroup}/{subgroup2}/{subgroup3}/{page}", method = RequestMethod.GET)
    public ModelAndView fourGroupsPages(@PathVariable String module, @PathVariable String group,
                                        @PathVariable String subgroup, @PathVariable String subgroup2, @PathVariable String subgroup3,
                                        @PathVariable String page, HttpServletRequest request
            , HttpServletResponse response) {

        String path = module + NavigationElement.PATH_SEPARATOR + group + PATH_SEPARATOR + subgroup + PATH_SEPARATOR + subgroup2 + PATH_SEPARATOR + subgroup3 + PATH_SEPARATOR + page;
        return navigate(path, request, response);
    }

    public static ModelAndView navigate(String path, HttpServletRequest request, HttpServletResponse response) {
        return navigate(path, "index", request, response);
    }

    /**
     * Tells whether the request URI points to a static resource, judging only by its last path segment (the file
     * system is never touched, so the answer cannot reveal which files exist on the server).
     */
    private static boolean isStaticResource(String requestUri) {
        return requestUri != null && STATIC_RESOURCE.matcher(requestUri).matches();
    }

    /**
     * Same page-resolution logic as {@link #navigate(String, HttpServletRequest, HttpServletResponse)},
     * but rendering into an arbitrary view name instead of the hardcoded {@code "index"} app shell.
     * Used by {@link PageEmbedController} to render a page with the {@code "embed"} view (just the
     * workspace, no header/sidebar/footer) for iframe-style embedding from non-ZK frontends.
     *
     * @param viewName logical Spring view name to render the resolved page into
     */
    public static ModelAndView navigate(String path, String viewName, HttpServletRequest request, HttpServletResponse response) {
        if (isStaticResource(request.getRequestURI())) {
            return null;
        }

        Map<String, Serializable> pageParams = new HashMap<>();
        if (request.getParameterMap() != null) {
            for (Object object : request.getParameterMap().entrySet()) {
                Entry httpParam = (Entry) object;
                if (httpParam.getValue() instanceof Serializable paramValue) {
                    pageParams.put(httpParam.getKey().toString(), paramValue);
                }
            }
        }
        ModelAndView mv = new ModelAndView(viewName);
        var zoom = request.getParameter("zoom");
        if (zoom != null && ZOOM.matcher(zoom).matches()) {
            // only a number reaches the style string; anything else is ignored
            mv.addObject("zoom", "zoom: " + zoom + ";");
        }

        try {
            Page page = ModuleContainer.getInstance().findPageByPrettyVirtualPath(path);
            NavigationRestrictions.verifyAccess(page);
            NavigationManager.setPageLater(page, pageParams);

            mv.addObject("navPage", page);
            mv.addObject("pageName", page.getName());

            Containers.get().findObjects(PageNavigationInterceptor.class).forEach(pageNavigationInterceptor -> pageNavigationInterceptor.afterPage(page, mv, request, response));

        } catch (PageNotFoundException | NavigationNotAllowedException e) {
            mv.setViewName("error/404");
            mv.addObject("message", e.getMessage());
        }


        return mv;

    }

}
