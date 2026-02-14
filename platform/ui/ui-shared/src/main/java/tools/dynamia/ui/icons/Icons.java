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

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * A Spring-managed bean that provides access to icon resources through a Map-like interface.
 * This class extends HashMap but overrides key methods to integrate with the IconsTheme system
 * for dynamic icon resolution and size specification.
 *
 * <p>The class allows icon retrieval using simple string keys, with optional size specification
 * using a colon-separated format (e.g., "icon-name:large"). Icons are resolved through the
 * IconsTheme system and return the real path to the icon resource.</p>
 *
 * <p>Example usage in expression language (EL):</p>
 * <pre>{@code
 * ${icons['save']}           // Returns small save icon path
 * ${icons['save:large']}     // Returns large save icon path
 * ${icons.get('save', 'disk')} // Returns save icon or disk icon as fallback
 * }</pre>
 *
 * <p>This component is registered with Spring using the bean name "icons" for easy access
 * in templates and views.</p>
 *
 * @author Mario A. Serrano Leones
 * @see IconsTheme
 * @see Icon
 * @see IconSize
 */
@Component("icons")
public class Icons extends HashMap<String, String> {

    /**
     * Overridden to prevent modification of the internal map structure.
     * This method intentionally does nothing and always returns null, as icons
     * are managed by the IconsTheme system and should not be added directly.
     *
     * @param key   the icon name key (ignored)
     * @param value the icon value (ignored)
     * @return always returns null
     */
    @Override
    public String put(String key, String value) {
        return null;
    }

    /**
     * Overridden to prevent modification of the internal map structure.
     * This method intentionally does nothing, as icons are managed by the
     * IconsTheme system and should not be added directly.
     *
     * @param m the map of entries to add (ignored)
     */
    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
    }

    /**
     * Retrieves the real path to an icon resource by its name.
     * The key can include an optional size specification using colon notation (e.g., "icon-name:large").
     * If no size is specified, IconSize.SMALL is used by default.
     *
     * @param key the icon name, optionally followed by ":size" (e.g., "save", "save:large")
     * @return the real path to the icon resource, or null if the icon is not found
     *
     * <p>Example:</p>
     * <pre>{@code
     * Icons icons = new Icons();
     * String path = icons.get("save");        // Returns small save icon path
     * String path2 = icons.get("save:large"); // Returns large save icon path
     * }</pre>
     */
    @Override
    public String get(Object key) {
        var icon = getIcon((String) key);
        return icon.getRealPath(getSize((String) key));
    }

    /**
     * Retrieves the real path to an icon resource by its name, with a fallback to a default icon.
     * If the requested icon is not found or equals Icon.NONE, the default icon is used instead.
     * The key can include an optional size specification using colon notation.
     *
     * @param key         the icon name, optionally followed by ":size" (e.g., "save", "save:large")
     * @param defaultIcon the fallback icon name to use if the primary icon is not found
     * @return the real path to the icon resource (primary or default), or null if neither is found
     *
     * <p>Example:</p>
     * <pre>{@code
     * Icons icons = new Icons();
     * String path = icons.get("custom-icon", "save"); // Returns custom-icon if exists, otherwise save
     * String path2 = icons.get("missing:large", "fallback:large"); // Returns fallback icon in large size
     * }</pre>
     */
    public String get(Object key, String defaultIcon) {
        var icon = getIcon((String) key);
        if (icon == null || icon.equals(Icon.NONE)) {
            key = defaultIcon;
            icon = getIcon((String) key);
        }
        return icon.getRealPath(getSize((String) key));
    }

    /**
     * Retrieves an Icon object from the IconsTheme system.
     * Extracts the icon name (without size specification) and looks it up in the theme.
     *
     * @param key the icon name, possibly including size specification (e.g., "save:large")
     * @return the Icon object, or null if the key is null or icon not found
     */
    private Icon getIcon(String key) {
        if (key == null) {
            return null;
        }
        return IconsTheme.get().getIcon(key);
    }


    /**
     * Extracts the icon size from a name string using colon notation.
     * If the name contains a colon, the text after it is parsed as an IconSize enum value.
     * If no colon is present or parsing fails, returns IconSize.SMALL as default.
     *
     * @param name the icon name with optional size (e.g., "save", "save:large", "delete:medium")
     * @return the parsed IconSize, or IconSize.SMALL if not specified or invalid
     *
     * <p>Example:</p>
     * <pre>{@code
     * getSize("save");        // Returns IconSize.SMALL
     * getSize("save:large");  // Returns IconSize.LARGE
     * getSize("save:MEDIUM"); // Returns IconSize.MEDIUM
     * getSize("save:invalid");// Returns IconSize.SMALL (fallback)
     * }</pre>
     */
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

    /**
     * Parses a comma-separated string of icon class names into a list of trimmed, non-blank strings.
     *
     * @param names the comma-separated string of icon class names (e.g., "class1, class2, class3")
     * @return a List of individual icon class names, or an empty list if the input is null or blank
     *
     * <p>Example:</p>
     * <pre>{@code
     * parseIconNames("class1, class2, class3"); // Returns ["class1", "class2", "class3"]
     * parseIconNames("  class1 , , class2  , "); // Returns ["class1", "class2"]
     * parseIconNames(null); // Returns []
     * parseIconNames(""); // Returns []
     * }</pre>
     */
    public static List<String> parseIconNames(String names) {
        if (names == null || names.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(names.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    /**
     * Parses a string into an IconName object containing the icon name and optional CSS classes.
     * The expected format is: {@code iconName|class1,class2,class3} where the icon name comes first,
     * followed by an optional pipe separator and comma-separated CSS class names.
     *
     * <p>This method is useful for specifying icons with additional styling or animation classes
     * in a compact, string-friendly format. The pipe character (|) separates the base icon name
     * from the CSS classes, and commas separate individual class names.</p>
     *
     * <p>Format rules:</p>
     * <ul>
     *   <li>Icon name is always the first part before the pipe (|) separator</li>
     *   <li>CSS classes are optional and come after the pipe separator</li>
     *   <li>Multiple CSS classes are separated by commas</li>
     *   <li>Whitespace around names and classes is automatically trimmed</li>
     *   <li>Empty or blank strings return null</li>
     * </ul>
     *
     * @param name the string to parse in format "iconName|class1,class2" or just "iconName"
     * @return an IconName object containing the icon name and CSS classes, or null if the input is null or blank
     *
     * <p>Examples:</p>
     * <pre>{@code
     * // Simple icon without classes
     * IconName icon1 = Icons.parseIconName("edit");
     * // Returns: IconName{name="edit", classes=[]}
     *
     * // Icon with single CSS class
     * IconName icon2 = Icons.parseIconName("check|text-success");
     * // Returns: IconName{name="check", classes=["text-success"]}
     *
     * // Icon with multiple CSS classes
     * IconName icon3 = Icons.parseIconName("edit|text-danger,pulse,fa-spin");
     * // Returns: IconName{name="edit", classes=["text-danger", "pulse", "fa-spin"]}
     *
     * // Icon with whitespace (automatically trimmed)
     * IconName icon4 = Icons.parseIconName("  save | text-primary , bounce  ");
     * // Returns: IconName{name="save", classes=["text-primary", "bounce"]}
     *
     * // Invalid inputs
     * Icons.parseIconName(null);   // Returns: null
     * Icons.parseIconName("");     // Returns: null
     * Icons.parseIconName("   ");  // Returns: null
     * }</pre>
     *
     * @see IconName
     * @see #parseIconNames(String)
     */
    public static IconName parseIconName(String name) {

        if (name == null || name.isBlank()) {
            return null;
        }
        String[] parts = name.split("\\|");
        if (parts.length == 0) {
            return null;
        }
        String iconName = parts[0].trim();
        List<String> classes = Collections.emptyList();
        if (parts.length > 1) {
            classes = parseIconNames(parts[1]);
        }
        return new IconName(iconName, classes);
    }


}
