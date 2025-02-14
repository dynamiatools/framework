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
package tools.dynamia.app.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import tools.dynamia.app.SessionApplicationTemplate;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.templates.ApplicationTemplateNotFoundException;
import tools.dynamia.templates.ApplicationTemplates;
import tools.dynamia.web.navigation.NavigationIndexInterceptor;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.sterotypes.Controller;
import tools.dynamia.navigation.Page;
import tools.dynamia.web.navigation.PageNavigationInterceptor;

import java.util.stream.Stream;

@Controller
@Order(1)
public class CommonController implements PageNavigationInterceptor {

    private final static LoggingService logger = LoggingService.get(CommonController.class);

    @RequestMapping("/")
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("index");

        if (request.getParameter("zoom") != null) {
            mv.addObject("zoom", "zoom: " + request.getParameter("zoom") + ";");
        }

        setupTemplate(request, response, mv);
        setupSkin(request, response, mv);

        Containers.get().findObjects(NavigationIndexInterceptor.class).forEach(indexInterceptor -> indexInterceptor.afterIndex(mv, request));

        return mv;
    }

    /**
     * Setup template and skin from request parameters
     *
     * @param request
     * @param response
     * @param mv
     */
    public static void setupTemplate(HttpServletRequest request, HttpServletResponse response, ModelAndView mv) {

        boolean updateCookie = true;

        var template = request.getParameter("template");

        if (template == null || template.isBlank()) {
            if (request.getCookies() != null) {
                template = Stream.of(request.getCookies()).filter(c -> c.getName().equals("template")).map(Cookie::getValue)
                        .findFirst().orElse(null);
                updateCookie = false;
            }

        }

        if (template != null) {
            try {
                var appTemplate = ApplicationTemplates.findTemplate(template);
                SessionApplicationTemplate.get().setTemplate(appTemplate);
                SessionApplicationTemplate.get().setSkin(appTemplate.getDefaultSkin().getId());
                var currentSkin = SessionApplicationTemplate.get().getSkin();
                if (currentSkin != null && currentSkin.isCustomLayout() && currentSkin.getLayoutView() != null) {
                    mv.setViewName(currentSkin.getLayoutView());
                }
                if (updateCookie) {
                    response.addCookie(new Cookie("template", template));
                }
            } catch (ApplicationTemplateNotFoundException e) {
                logger.warn("Template not found: " + e.getMessage());
            }
        }

    }

    /**
     * Setup skin from request parameters
     *
     * @param request
     * @param response
     * @param mv
     */
    public static void setupSkin(HttpServletRequest request, HttpServletResponse response, ModelAndView mv) {

        boolean updateCookie = true;

        var skin = request.getParameter("skin");

        if (skin == null || skin.isBlank()) {
            if (request.getCookies() != null) {
                skin = Stream.of(request.getCookies()).filter(c -> c.getName().equals("skin")).map(Cookie::getValue)
                        .findFirst().orElse(null);
                updateCookie = false;
            }

        }

        if (skin != null) {
            SessionApplicationTemplate.get().setSkin(skin);
            if (updateCookie) {
                response.addCookie(new Cookie("skin", skin));
            }
        }

        var currentSkin = SessionApplicationTemplate.get().getSkin();
        if (currentSkin != null && currentSkin.isCustomLayout() && currentSkin.getLayoutView() != null) {
            mv.setViewName(currentSkin.getLayoutView());
        }
    }

    @Override
    public void afterPage(Page page, ModelAndView modelAndView, HttpServletRequest request, HttpServletResponse response) {

        setupSkin(request, response, modelAndView);
    }
}
