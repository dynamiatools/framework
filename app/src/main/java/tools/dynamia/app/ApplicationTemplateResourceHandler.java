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
package tools.dynamia.app;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import tools.dynamia.templates.ApplicationTemplate;
import tools.dynamia.templates.ApplicationTemplates;
import tools.dynamia.web.ETagVersionedResource;

import java.io.IOException;
import java.util.List;

/**
 * Template resource handler find resources in current {@link ApplicationTemplate} directory
 *
 * @author Mario A. Serrano Leones
 */

public class ApplicationTemplateResourceHandler extends ResourceHttpRequestHandler {

    public static final List<String> STATIC_PATHS = List.of(
            "/*.jpg", "/*.jpeg", "/*.png", "*.gif", "*.mp4", "/*.html", "/*.css", "/*.js", "/*.webp",
            "/*.ico", "/*.bmp", "/manifest.json", "/*.webmanifest", "/static/**", "/*.map", "/*.svg"
    );


    private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();
    public static final String STATIC = "/static/";
    public static final String WEB_TEMPLATES = "/web/templates/";

    private final ApplicationInfo appInfo;
    private String relativeContext;

    public ApplicationTemplateResourceHandler(ApplicationInfo appInfo) {
        this.appInfo = appInfo;
    }

    @Override
    protected Resource getResource(HttpServletRequest request) {

        ApplicationTemplate theme = ApplicationTemplates.findTemplate(appInfo.getTemplate());
        String path = request.getRequestURI().substring(request.getContextPath().length());

        if (relativeContext != null && path.startsWith(relativeContext)) {
            path = path.replace(relativeContext, "");
        }

        Resource resource = new ServletContextResource(request.getServletContext(), path);

        if (resource.exists()) {
            return resource;
        } else {
            try {
                String templateResourcePath = WEB_TEMPLATES + theme.getName().toLowerCase() + path;
                resource = new ClassPathResource(templateResourcePath, theme.getClass());
                resource.getURL(); //test URL
            } catch (IOException e) {
                if (isStaticResource(path)) {
                    if (!path.startsWith(STATIC)) {
                        path = STATIC + path;
                    }
                    try {
                        resource = new ClassPathResource(path);
                        resource.getURL(); //test if exit
                        resource = new ETagVersionedResource(resource, appInfo.getVersion());
                    } catch (IOException ex) {
                        resource = null;
                    }
                }
            }
            return resource;
        }
    }

    public static boolean isStaticResource(String path) {
        try {
            return STATIC_PATHS.stream().anyMatch(pattern -> ANT_PATH_MATCHER.match(pattern, path));
        } catch (Exception e) {
            return false;
        }
    }

    public String getRelativeContext() {
        return relativeContext;
    }

    public void setRelativeContext(String relativeContext) {
        this.relativeContext = relativeContext;
    }
}
