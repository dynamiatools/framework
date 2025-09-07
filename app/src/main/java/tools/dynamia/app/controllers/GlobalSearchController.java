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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import tools.dynamia.integration.search.SearchService;

/**
 * Controller for global search functionality in the application.
 * <p>
 * Provides an endpoint to perform search queries using the {@link SearchService} and returns results in a view.
 * <p>
 * Endpoint:
 * <ul>
 *   <li>GET /search?q=... - Search query</li>
 * </ul>
 *
 * @author Mario A. Serrano Leones
 * @since 2023
 */
@Component("/search")
public class GlobalSearchController {

    /**
     * Service for performing search queries.
     */
    @Autowired
    private SearchService service;

    /**
     * Handles search queries and returns results in the "searchResult" view.
     * @param q the search query string
     * @return the {@link ModelAndView} containing search results
     */
    @RequestMapping("/")
    public ModelAndView query(@RequestParam("q") String q) {
        ModelAndView mv = new ModelAndView("searchResult");
        mv.addObject("result", service.search(q));
        return mv;
    }

}
