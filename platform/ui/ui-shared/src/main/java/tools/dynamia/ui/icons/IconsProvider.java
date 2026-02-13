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

import java.util.List;

/**
 * Service provider interface for supplying icon resources to the Dynamia Tools framework.
 * Implementations of this interface are responsible for providing Icon objects from various sources
 * such as file systems, resources, databases, or external icon libraries.
 *
 * <p>IconsProvider implementations are automatically discovered and registered by the Spring container
 * through component scanning. The {@link IconsTheme} class queries all registered providers to resolve
 * icon requests, searching through them sequentially until a match is found.</p>
 *
 * <p>Typical use cases include:</p>
 * <ul>
 *   <li>Providing icons from theme resources</li>
 *   <li>Loading icons from external icon libraries (FontAwesome, Material Icons, etc.)</li>
 *   <li>Serving custom application-specific icons</li>
 *   <li>Implementing icon sets with different visual styles</li>
 * </ul>
 *
 * <p>Example implementation:</p>
 * <pre>{@code
 * @Component
 * public class MyIconsProvider implements IconsProvider {
 *
 *     @Override
 *     public Icon getIcon(String name) {
 *         // Load icon from resources
 *         if ("custom-icon".equals(name)) {
 *             return new Icon(name, "/icons/custom.png");
 *         }
 *         return null;
 *     }
 *
 *     @Override
 *     public List<Icon> getAll() {
 *         // Return all available icons
 *         return Arrays.asList(
 *             new Icon("custom-icon", "/icons/custom.png"),
 *             new Icon("another-icon", "/icons/another.png")
 *         );
 *     }
 * }
 * }</pre>
 *
 * @author Mario A. Serrano Leones
 * @see Icon
 * @see IconsTheme
 */
public interface IconsProvider {

    /**
     * Retrieves an icon by its logical name.
     * Implementations should search their icon repository for an icon matching
     * the provided name and return it if found.
     *
     * <p>If the icon is not available in this provider, this method should return null
     * to allow the IconsTheme to continue searching in other providers.</p>
     *
     * @param name the logical name of the icon to retrieve (e.g., "save", "edit", "delete")
     * @return the Icon object if found, or null if this provider doesn't have the requested icon
     */
    Icon getIcon(String name);

    /**
     * Retrieves all icons available from this provider.
     * This method is typically used for building icon selectors, catalogs, or documentation.
     *
     * <p>Implementations should return a complete list of all icons they can provide.
     * The returned list should not be null but may be empty if no icons are available.</p>
     *
     * @return a list containing all available icons from this provider, never null
     */
    List<Icon> getAll();
}
