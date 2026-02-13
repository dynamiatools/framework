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
import tools.dynamia.integration.Containers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central manager for icon resources in the Dynamia Tools framework.
 * This singleton class provides a unified interface for retrieving icons from multiple
 * {@link IconsProvider} implementations, with built-in caching for improved performance.
 *
 * <p>The IconsTheme acts as a facade over all registered IconsProvider beans,
 * searching through them sequentially until an icon with the requested name is found.
 * Once found, icons are cached to avoid repeated lookups.</p>
 *
 * <p>Icons can be retrieved by their logical name, with optional support for
 * prefixed names (e.g., "icons:save") and fallback icons when the primary icon is not found.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Icon saveIcon = IconsTheme.get().getIcon("save");
 * Icon customIcon = IconsTheme.get().getIcon("custom-icon", "default-icon");
 * List<Icon> allIcons = IconsTheme.get().getAll();
 * }</pre>
 *
 * @author Mario A. Serrano Leones
 * @see Icon
 * @see IconsProvider
 */
public class IconsTheme {

    /**
     * Singleton instance of IconsTheme.
     */
    private static IconsTheme instance;

    /**
     * Cache map storing already resolved icons by their name for quick lookup.
     */
    private final Map<String, Icon> iconsCache = new HashMap<>();

    /**
     * Logging service for reporting icon resolution issues.
     */
    private final LoggingService logger = Containers.get().findObject(LoggingService.class);

    /**
     * List containing all available icons from all registered providers.
     * Populated lazily on first call to {@link #getAll()}.
     */
    private final List<Icon> all = new ArrayList<>();

    /**
     * Returns the singleton instance of IconsTheme.
     * Creates the instance on first call (lazy initialization).
     *
     * @return the singleton IconsTheme instance
     */
    public static IconsTheme get() {
        if (instance == null) {
            instance = new IconsTheme();
        }
        return instance;
    }

    /**
     * Retrieves an icon by its logical name.
     * This method searches through all registered IconsProvider implementations
     * to find the requested icon. Results are cached for subsequent requests.
     *
     * <p>The method handles special cases:</p>
     * <ul>
     *   <li>Returns Icon.NONE if the name is null or blank</li>
     *   <li>Strips "icons:" prefix if present (e.g., "icons:save" becomes "save")</li>
     *   <li>Uses cached icon if available</li>
     *   <li>Logs a warning and returns Icon.NONE if icon is not found</li>
     * </ul>
     *
     * @param name the logical name of the icon (e.g., "save", "edit", "delete")
     * @return the Icon object, or Icon.NONE if not found
     *
     * <p>Example:</p>
     * <pre>{@code
     * Icon icon = IconsTheme.get().getIcon("save");
     * Icon prefixedIcon = IconsTheme.get().getIcon("icons:edit");
     * }</pre>
     */
    public Icon getIcon(String name) {
        if (name == null || name.isBlank()) {
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
     * Retrieves an icon by its logical name, with a fallback to a default icon.
     * If the primary icon is not found or has no real path, the method attempts
     * to retrieve the icon specified by the default name.
     *
     * <p>This method is useful when you want to ensure an icon is always returned,
     * falling back to a known icon if the requested one doesn't exist.</p>
     *
     * @param name the logical name of the primary icon to retrieve
     * @param defaultName the logical name of the fallback icon if primary is not found
     * @return the Icon object (primary or fallback), or Icon.NONE if neither is found
     *
     * <p>Example:</p>
     * <pre>{@code
     * // Returns custom-icon if exists, otherwise returns the save icon
     * Icon icon = IconsTheme.get().getIcon("custom-icon", "save");
     * }</pre>
     */
    public Icon getIcon(String name, String defaultName) {
        Icon icon = getIcon(name);
        if (icon.getRealPath() == null) {
            icon = getIcon(defaultName);
        }
        return icon;
    }

    /**
     * Searches for an icon by name across all registered IconsProvider implementations.
     * This method iterates through providers in the order they are discovered by the
     * container and returns the first matching icon found.
     *
     * @param name the logical name of the icon to find
     * @return the Icon object if found in any provider, or null if not found
     */
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

    /**
     * Retrieves all available icons from all registered IconsProvider implementations.
     * The list is populated lazily on the first call and cached for subsequent requests.
     *
     * <p>This method aggregates icons from all providers found in the container,
     * which is useful for displaying icon selectors or generating icon catalogs.</p>
     *
     * @return an unmodifiable list containing all available icons
     *
     * <p>Example:</p>
     * <pre>{@code
     * List<Icon> allIcons = IconsTheme.get().getAll();
     * for (Icon icon : allIcons) {
     *     System.out.println(icon.getName());
     * }
     * }</pre>
     */
    public List<Icon> getAll() {
        if (all.isEmpty()) {
            for (IconsProvider p : Containers.get().findObjects(IconsProvider.class)) {
                all.addAll(p.getAll());
            }
        }

        return all;
    }

}
