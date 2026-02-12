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

import org.springframework.web.servlet.view.InternalResourceViewResolver;
import tools.dynamia.templates.ApplicationTemplate;
import tools.dynamia.templates.ApplicationTemplates;

/**
 * @author Mario A. Serrano Leones
 */
public class ApplicationTemplateViewResolver extends InternalResourceViewResolver {

    private final ApplicationInfo applicationInfo;

    public ApplicationTemplateViewResolver(ApplicationInfo applicationInfo) {
        this.applicationInfo = applicationInfo;
        setViewClass(ApplicationTemplateResourceView.class);
        setCache(false);

    }

    @Override
    protected String getPrefix() {
        ApplicationTemplate template = ApplicationTemplates.findTemplate(applicationInfo.getTemplate());

        try {
            template = SessionApplicationTemplate.get().getTemplate();
        } catch (Exception e) {

        }

        return "/zkau/web/templates/" + template.getName().toLowerCase() + "/views/";
    }

    @Override
    protected String getSuffix() {
        return ".zul";
    }

}
