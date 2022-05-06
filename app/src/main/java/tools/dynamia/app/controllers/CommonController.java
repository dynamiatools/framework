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

import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import tools.dynamia.app.CurrentTemplate;
import tools.dynamia.integration.sterotypes.Controller;

import javax.servlet.http.HttpServletRequest;

@Controller
@Order(1)
public class CommonController {

	@RequestMapping("/")
	public ModelAndView index(HttpServletRequest request) {
		ModelAndView mv = new ModelAndView("index");

		if (request.getParameter("zoom") != null) {
			mv.addObject("zoom", "zoom: " + request.getParameter("zoom") + ";");
		}

		if (request.getParameter("skin") != null) {
			CurrentTemplate.get().setSkin(request.getParameter("skin"));
		}

		return mv;
	}

}
