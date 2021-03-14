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
package tools.dynamia.ui.icons;

import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.integration.Containers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class IconsTheme {

    private static IconsTheme instance;
    private Map<String, Icon> iconsCache = new HashMap<>();
    private LoggingService logger = Containers.get().findObject(LoggingService.class);
    private List<Icon> all = new ArrayList<>();

    public static IconsTheme get() {
        if (instance == null) {
            instance = new IconsTheme();
        }
        return instance;
    }

    /**
     * Get the icon info using its logical name.
     *
     * @param name
     * @return
     */
    public Icon getIcon(String name) {
        if (name == null) {
            return Icon.NONE;
        }

        if (name.startsWith("icons:")) {
            name = name.substring("icons:".length());
        }

        Icon icon = iconsCache.get(name);
        if (icon == null) {
            icon = findIcon(name);
        }

        if (icon == null) {
            logger.warn("Icon not found " + name);
            icon = Icon.NONE;
        } else {
            iconsCache.put(name, icon);
        }

        return icon;
    }

    /**
     * Get the icon info using its logical name, if not found its find by
     * defaultName;
     *
     * @param name
     * @param defaultName
     * @return
     */
    public Icon getIcon(String name, String defaultName) {
        Icon icon = getIcon(name);
        if (icon.getRealPath() == null) {
            icon = getIcon(defaultName);
        }
        return icon;
    }

    private Icon findIcon(String name) {
        Icon icon = null;

        for (IconsProvider p : Containers.get().findObjects(IconsProvider.class)) {
            icon = p.getIcon(name);
            if (icon != null) {
                break;
            }
        }

        return icon;
    }

    public List<Icon> getAll() {
        if (all.isEmpty()) {
            for (IconsProvider p : Containers.get().findObjects(IconsProvider.class)) {
                all.addAll(p.getAll());
            }
        }

        return all;
    }

}
