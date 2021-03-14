/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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
package tools.dynamia.app.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import tools.dynamia.navigation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@Controller
@RequestMapping("/page")
public class PageNavigationController {

    @RequestMapping(value = "/{module}/{group}/{page}", method = RequestMethod.GET)
    public ModelAndView defaultPages(@PathVariable String module, @PathVariable String group, @PathVariable String page,
                                     HttpServletRequest request) {

        String path = module + "/" + group + "/" + page;

        return navigate(path, request);

    }

    @RequestMapping(value = "/{module}/{page}", method = RequestMethod.GET)
    public ModelAndView directPages(@PathVariable String module, @PathVariable String page,
                                    HttpServletRequest request) {

        String path = module + "/" + page;
        return navigate(path, request);

    }

    @RequestMapping(value = "/{module}/{group}/{subgroup}/{page}", method = RequestMethod.GET)
    public ModelAndView twoGroupsPages(@PathVariable String module, @PathVariable String group,
                                       @PathVariable String subgroup, @PathVariable String page, HttpServletRequest request) {

        String path = module + "/" + group + "/" + subgroup + "/" + page;
        return navigate(path, request);

    }

    private ModelAndView navigate(String path, HttpServletRequest request) {
        if (new File(request.getRequestURI()).isFile()) {
            return null;
        }

        Map<String, Object> pageParams = new HashMap<>();
        if (request != null && request.getParameterMap() != null) {
            for (Object object : request.getParameterMap().entrySet()) {
                Entry httpParam = (Entry) object;
                pageParams.put(httpParam.getKey().toString(), httpParam.getValue());
            }
        }
        ModelAndView mv = new ModelAndView("index");
        mv.addObject("contextPath", request.getContextPath());
        if (request.getParameter("zoom") != null) {
            mv.addObject("zoom", "zoom: " + request.getParameter("zoom") + ";");
        }

        try {
            Page page = NavigationManager.getCurrent().findPageByPrettyVirtualPath(path);
            NavigationRestrictions.verifyAccess(page);
            NavigationManager.getCurrent().reload();
            NavigationManager.getCurrent().setCurrentPage(page, pageParams);


            mv.addObject("navPage", page);
            mv.addObject("pageName", page.getName());


        } catch (PageNotFoundException | NavigationNotAllowedException e) {
            mv.setViewName("error/404");
            mv.addObject("message", e.getMessage());
        }

        return mv;

    }

}
