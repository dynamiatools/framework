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

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.navigation.NavigationNotAllowedException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception controller for handling application errors and exceptions.
 * <p>
 * Provides handlers for navigation errors, general exceptions, and error endpoints.
 * Returns appropriate views and error information for UI clients.
 *
 * @author Mario A. Serrano Leones
 * @since 2023
 */
@Controller
public class GlobalExceptionController {

    /**
     * Logger for error and exception handling.
     */
    private final LoggingService logger = new SLF4JLoggingService(GlobalExceptionController.class);

    /**
     * Handles navigation not allowed exceptions, returning a specific error view.
     *
     * @param req the HTTP servlet request
     * @param e   the exception
     * @return the {@link ModelAndView} for the error
     */
    @ExceptionHandler(NavigationNotAllowedException.class)
    public ModelAndView handleNavigationNotAllowed(HttpServletRequest req, Exception e) {
        ModelAndView mv = new ModelAndView("errors/notallow");
        mv.addObject("exception", e);
        return mv;
    }

    /**
     * Handles all other exceptions, returning the login view with error information.
     *
     * @param req the HTTP servlet request
     * @param e   the exception
     * @return the {@link ModelAndView} for the error
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleOthersException(HttpServletRequest req, Exception e) {
        ModelAndView mv = new ModelAndView("login");
        mv.addObject("exception", e);
        return mv;
    }

    /**
     * Handles error endpoints, returning detailed error information and appropriate views.
     *
     * @param request the HTTP servlet request
     * @return the {@link ModelAndView} for the error
     */
    @RequestMapping(value = {"/errors", "/error"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
    public ModelAndView htmlError(HttpServletRequest request) {
        var statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        var messageObj = request.getAttribute("jakarta.servlet.error.message");
        var throwable = (Throwable) request.getAttribute("jakarta.servlet.error.exception");
        var requestUri = (String) request.getAttribute("jakarta.servlet.error.request_uri");


        if (statusCode == null && throwable == null) {
            logger.warn("Unknow request error");

        }

        if (messageObj == null) {
            messageObj = "";
        }

        if (requestUri == null) {
            requestUri = "Unknown";
        }

        if (!requestUri.endsWith(".map")) {
            logger.error("ERROR " + statusCode + ": " + requestUri + " on  " + request.getServerName() + ". " + messageObj, throwable);
        }

        ModelAndView mv = new ModelAndView("error/error");

        mv.addObject("title", "Error");
        mv.addObject("statusCode", statusCode);
        mv.addObject("uri", requestUri);
        mv.addObject("message", messageObj + " " + (throwable != null ? throwable.getMessage() : ""));
        mv.addObject("exception", throwable);

        if (statusCode != null && statusCode == 404) {
            mv.setViewName("error/404");
            mv.addObject("pageAlias", requestUri);
        }

        return mv;

    }

    /**
     * Handles error endpoints for JSON requests, returning error details in JSON format.
     *
     * @param request the HTTP servlet request
     * @return a {@link ResponseEntity} containing error details in JSON format
     */
    @RequestMapping(value = {"/errors", "/error"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT},
            consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, String>> jsonError(HttpServletRequest request) {

        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        Object messageObj = request.getAttribute("jakarta.servlet.error.message");
        Throwable throwable = (Throwable) request.getAttribute("jakarta.servlet.error.exception");
        String requestUri = (String) request.getAttribute("jakarta.servlet.error.request_uri");


        if (messageObj == null) {
            messageObj = "";
        }

        if (requestUri == null) {
            requestUri = "Unknown";
        }

        Map<String, String> body = new HashMap<>();
        body.put("status", statusCode != null ? statusCode.toString() : "500");
        body.put("message", messageObj.toString());
        body.put("path", requestUri);
        if (throwable != null) {
            body.put("error", throwable.getClass().getName());
            body.put("exception", throwable.getMessage());
        }


        return ResponseEntity.status(statusCode != null ? statusCode : 500).body(body);

    }
}
