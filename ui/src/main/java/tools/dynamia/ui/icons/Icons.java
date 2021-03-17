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

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mario A. Serrano Leones
 */
@Component("icons")
public class Icons extends HashMap<String, String> {

    @Override
    public String put(String key, String value) {
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
    }

    @Override
    public String get(Object key) {
        var icon = getIcon((String) key);
        return icon.getRealPath(getSize((String) key));
    }

    public String get(Object key, String defaultIcon) {
        var icon = getIcon((String) key);
        if (icon == null || icon.equals(Icon.NONE)) {
            key = defaultIcon;
            icon = getIcon((String) key);
        }
        return icon.getRealPath(getSize((String) key));
    }

    private Icon getIcon(String key) {
        if (key == null) {
            return null;
        }
        String name = key;
        return IconsTheme.get().getIcon(name);
    }


    private IconSize getSize(String name) {
        IconSize iconSize = IconSize.SMALL;
        try {
            if (name.contains(":")) {
                String iconsizeName = name.substring(name.indexOf(":") + 1);
                iconSize = IconSize.valueOf(iconsizeName.toUpperCase());
            }
        } catch (Exception e) {
            // ignore
        }
        return iconSize;
    }


}
