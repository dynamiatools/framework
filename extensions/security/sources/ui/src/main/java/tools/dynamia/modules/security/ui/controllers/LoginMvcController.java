
/*
 * Copyright (c) 2009 - 2021 Dynamia Soluciones IT SAS  All Rights Reserved
 *
 * Todos los Derechos Reservados  2009 - 2021
 *
 * Este archivo es propiedad de Dynamia Soluciones IT NIT 900302344-1 en Colombia / Sur America,
 * esta estrictamente prohibida su copia o distribución sin previa autorización del propietario.
 * Puede contactarnos a info@dynamiasoluciones.com o visitar nuestro sitio web
 * https://www.dynamiasoluciones.com
 *
 * Autor: Ing. Mario Serrano Leones <mario@dynamiasoluciones.com>
 */

package tools.dynamia.modules.security.ui.controllers;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.security.LoginControllerInterceptor;
import tools.dynamia.modules.security.listeners.LoginListener;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mario Serrano Leones
 */
@Controller
public class LoginMvcController {

    private LoggingService logger = new SLF4JLoggingService(LoginMvcController.class);

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView login(HttpServletRequest request) {

        logger.info("Starting login page");

        ModelAndView mv = new ModelAndView("login");


        Object csrf = request.getAttribute("_csrf");
        mv.addObject("CSRF", csrf);
        mv.addObject("username", request.getParameter("username"));


        HttpSession session = request.getSession();

        Exception ex = (Exception) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);

        if (ex != null) {
            mv.addObject("loginMessage", ex.getLocalizedMessage());
        }

        Map<String, Object> params = new HashMap<>(mv.getModel());
        params.put("exception", ex);
        params.put("request", request);
        params.put("session", session);

        Containers.get().findObjects(LoginControllerInterceptor.class).forEach(l -> l.login(request, mv.getViewName(), params));
        mv.addAllObjects(params);
        fireOnLoginPageListener(mv, params);

        return mv;
    }


    private void fireOnLoginPageListener(ModelAndView mv, Map<String, Object> params) {

        if (mv.getViewName() != null && mv.getViewName().equals("login")) {

            Containers.get().findObjects(LoginListener.class).stream()
                    .sorted(Comparator.comparingInt(LoginListener::getPriority))
                    .forEach(l -> l.onLoginPage(params));
        }
    }

}
