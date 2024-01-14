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
package tools.dynamia.templates;

import tools.dynamia.integration.Containers;

import java.util.*;

/**
 * @author Mario A. Serrano Leones
 */
public class ApplicationTemplates {

    public static ApplicationTemplate findTemplate(final String theme) {
        Collection<ApplicationTemplate> themes = Containers.get().findObjects(ApplicationTemplate.class,
                (ApplicationTemplate object) -> object.getName().equalsIgnoreCase(theme));

        Iterator<ApplicationTemplate> iterator = themes.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            ApplicationTemplate defaultTemplate = getDefaultTemplate(
                    Containers.get().findObjects(ApplicationTemplate.class));
            if (defaultTemplate != null) {
                return defaultTemplate;
            }

            throw new ApplicationTemplateNotFoundException("Application template [" + theme + "] not found");
        }

    }

    public static ApplicationTemplate findTemplate(final String name, List<ApplicationTemplate> templates) {
        if (templates != null) {
            for (ApplicationTemplate applicationTemplate : templates) {
                if (applicationTemplate.getName().equalsIgnoreCase(name)) {
                    return applicationTemplate;
                }
            }
        }
        ApplicationTemplate defaultTemplate = getDefaultTemplate(templates);
        if (defaultTemplate != null) {
            return defaultTemplate;
        }

        throw new ApplicationTemplateNotFoundException("Application template [" + name + "] not found");
    }

    /**
     * Find the template skin using id, if not found null is returned
     */
    public static ApplicationTemplateSkin findSkin(ApplicationTemplate template, String skinId) {

        if (skinId != null) {
            Optional<ApplicationTemplateSkin> skin = getAllSkins(template).stream().filter(s -> s.getId().equals(skinId)).findFirst();
            if (skin.isPresent()) {
                return skin.get();
            }
        }
        return null;
    }

    /**
     * Get all skin related to template. Include skin providers
     */
    public static List<ApplicationTemplateSkin> getAllSkins(ApplicationTemplate template) {
        if (template != null) {
            List<ApplicationTemplateSkin> allApplicationTemplateSkins = new ArrayList<>(template.getSkins());

            Containers.get().findObjects(ApplicationTemplateSkinProvider.class, o -> template.getName().equals(o.getTemplateName()))
                    .forEach(s -> allApplicationTemplateSkins.add(s.getSkin()));

            return allApplicationTemplateSkins;
        } else {
            return Collections.emptyList();
        }
    }

    public static ApplicationTemplate getDefaultTemplate(Collection<ApplicationTemplate> templates) {
        for (ApplicationTemplate t : templates) {
            if (t.getName().equalsIgnoreCase("default")) {
                return t;
            }
        }
        return null;
    }

    private ApplicationTemplates() {
    }
}
