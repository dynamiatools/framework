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

import tools.dynamia.ui.icons.AbstractFontIconsProvider;
import tools.dynamia.ui.icons.Icon;
import tools.dynamia.ui.icons.IconException;
import tools.dynamia.ui.icons.InstallIcons;

import java.io.IOException;
import java.util.Properties;

@InstallIcons
public class FontAwesomeIconsProvider extends AbstractFontIconsProvider {

    private static final String FA_PREFIX = "fa-";

    @Override
    public Properties getNamesMapping() {
        try {
            Properties properties = new Properties();
            properties.load(getClass().getResourceAsStream("/META-INF/dynamia/fa-icons.properties"));
            return properties;
        } catch (IOException e) {
            throw new IconException("Unable to load dynamical theme icons", e);
        }
    }

    @Override
    public Icon getIcon(String name) {
        Icon icon = super.getIcon(name);

        if (icon == null && name.startsWith(FA_PREFIX)) {
            String internalName = name.substring(FA_PREFIX.length());
            icon = newIcon(name, internalName);
        }

        return icon;
    }

    @Override
    protected Icon newIcon(String name, String internalName) {
        return new FAIcon(name, "fa " + FA_PREFIX + internalName);
    }
}
