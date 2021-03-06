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
package tools.dynamia.app.template;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import tools.dynamia.app.ApplicationInfo;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Mario A. Serrano Leones
 */
@Component
public class TemplateResourceHandler extends ResourceHttpRequestHandler {

    @Autowired
    private ApplicationInfo app;
    private String relativeContext;

    @Override
    protected Resource getResource(HttpServletRequest request) {

        ApplicationTemplate theme = ApplicationTemplates.findTemplate(app.getTemplate());
        String path = request.getRequestURI().substring(request.getContextPath().length());

        if (relativeContext != null && path.startsWith(relativeContext)) {
            path = path.replace(relativeContext, "");
        }
        Resource resource = new ServletContextResource(request.getSession(false).getServletContext(), path);

        if (resource.exists()) {
            return resource;
        } else {

            String templateResourcePath = "/web/templates/" + theme.getName().toLowerCase() + path;

            return new ClassPathResource(templateResourcePath, theme.getClass());
        }
    }

    public String getRelativeContext() {
        return relativeContext;
    }

    public void setRelativeContext(String relativeContext) {
        this.relativeContext = relativeContext;
    }
}
