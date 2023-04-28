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
package tools.dynamia.ui.icons;

import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;

import java.util.*;

/**
 * IconsProvider for Fonts Icons
 *
 * @author Mario
 *
 */
public abstract class AbstractFontIconsProvider implements IconsProvider {

    private final Map<String, Icon> icons = new HashMap<>();
    private final LoggingService logger = new SLF4JLoggingService(getClass());

    public AbstractFontIconsProvider() {
        init();
    }

    private void init() {
        Properties names = getNamesMapping();
        if (names != null) {
            Enumeration e = names.propertyNames();
            logger.info("Installing " + names.size() + " font icons from " + getClass());
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                String value = names.getProperty(key);
                if (key != null && value != null) {
                    icons.put(key, newIcon(key, value));
                }
            }
            logger.info(" Font icons installed successfuly");
        }
    }

    protected Icon newIcon(String name, String internalName) {
        return new Icon(name, internalName, IconType.FONT);
    }

    @Override
    public Icon getIcon(String name) {
        if (icons.isEmpty()) {
            init();
        }

        return icons.get(name);
    }

    public abstract Properties getNamesMapping();

    @Override
    public List<Icon> getAll() {
        return new ArrayList<>(icons.values());
    }

}
