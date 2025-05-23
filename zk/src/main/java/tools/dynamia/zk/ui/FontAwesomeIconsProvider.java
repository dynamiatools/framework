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

package tools.dynamia.zk.ui;

import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.ui.icons.AbstractFontIconsProvider;
import tools.dynamia.ui.icons.Icon;
import tools.dynamia.ui.icons.IconException;
import tools.dynamia.ui.icons.InstallIcons;

import java.io.IOException;
import java.util.Properties;

public class FontAwesomeIconsProvider extends AbstractFontIconsProvider {

    private static final LoggingService logger = new SLF4JLoggingService(FontAwesomeIconsProvider.class);


    @Override
    public Properties getNamesMapping() {
        Properties properties = new Properties();
        try {
            properties.load(FontAwesomeIconsProvider.class.getResourceAsStream(getIconsPath()));
        } catch (IOException | NullPointerException e) {
            logger.error("Unable to load icons from file " + getIconsPath(), e);
        }

        return properties;
    }

    protected String getIconsPath() {
        return "/META-INF/dynamia/fa-icons.properties";
    }

    @Override
    public Icon getIcon(String name) {
        Icon icon = super.getIcon(name);

        if (icon == null) {
            if (name.startsWith(getIconsPrefix())) {
                String internalName = name.substring(getIconsPrefix().length());
                icon = newIcon(name, internalName);
                addIcon(name, icon);
            } else if (name.startsWith("fa ") || name.startsWith("fab ")) {
                icon = new FAIcon(name, name);
                addIcon(name, icon);
            }
        }

        return icon;
    }

    protected String getIconsPrefix() {
        return "fa-";
    }

    @Override
    protected Icon newIcon(String name, String internalName) {
        return new FAIcon(name, "fa " + getIconsPrefix() + internalName);
    }
}
