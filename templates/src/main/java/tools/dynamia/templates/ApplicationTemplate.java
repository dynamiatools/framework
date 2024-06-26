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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Implement this interface if you need a new application template.
 * Each application template can provide multiple skins or colors variations
 *
 * @author Mario A. Serrano Leones
 */
public interface ApplicationTemplate extends Serializable {

    String AUTHOR = "author";
    String DATE = "date";
    String VERSION = "version";
    String COPYRIGHT = "copyright";
    String ORIGINAL_AUTHOR = "original_author";

    String getName();

    Map<String, Object> getProperties();

    List<ApplicationTemplateSkin> getSkins();

    ApplicationTemplateSkin getDefaultSkin();


    default void installSkin(ApplicationTemplateSkin applicationTemplateSkin) {
        if (getSkins().stream().noneMatch(s -> s.getId().equals(applicationTemplateSkin.getId()))) {
            getSkins().add(applicationTemplateSkin);
        } else {
            System.err.println("Already skin installed with id " + applicationTemplateSkin.getId());
        }
    }

    void init();

    default ApplicationTemplateSkin getSkin(String name) {
        ApplicationTemplateSkin result = null;
        if (name != null && !name.isEmpty()) {
            var skin = getSkins().stream().filter(s -> s.getName().equalsIgnoreCase(name)).findFirst();
            result = skin.orElse(null);
        }

        return result;
    }

}
