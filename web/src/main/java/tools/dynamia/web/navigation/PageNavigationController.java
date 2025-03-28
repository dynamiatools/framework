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

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static tools.dynamia.navigation.NavigationElement.PATH_SEPARATOR;

@Controller("pageNavigationController")
@RequestMapping("/page")
public class PageNavigationController {


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
        if (new File(request.getRequestURI()).isFile()) {
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
        ModelAndView mv = new ModelAndView("index");
        if (request.getParameter("zoom") != null) {
            mv.addObject("zoom", "zoom: " + request.getParameter("zoom") + ";");
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
