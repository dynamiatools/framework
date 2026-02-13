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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Abstract base class for IconsProvider implementations that serve font-based icons.
 * This class provides common functionality for loading and managing icon sets from font libraries
 * such as FontAwesome, Material Icons, or other web font icon systems.
 *
 * <p>Font icons are lightweight, scalable, and customizable through CSS. This provider manages
 * the mapping between logical icon names and their internal font class names or Unicode values.</p>
 *
 * <p>Subclasses must implement {@link #getNamesMapping()} to provide a Properties object
 * that maps logical icon names to their internal font-specific identifiers.</p>
 *
 * <p>The initialization process:</p>
 * <ul>
 *   <li>Loads icon name mappings from the Properties returned by getNamesMapping()</li>
 *   <li>Creates Icon objects with IconType.FONT for each mapping</li>
 *   <li>Stores icons in an internal cache for quick retrieval</li>
 *   <li>Logs the installation progress for debugging</li>
 * </ul>
 *
 * <p>Example implementation:</p>
 * <pre>{@code
 * @Component
 * public class FontAwesomeProvider extends AbstractFontIconsProvider {
 *
 *     @Override
 *     public Properties getNamesMapping() {
 *         Properties props = new Properties();
 *         props.setProperty("save", "fa-save");
 *         props.setProperty("edit", "fa-edit");
 *         props.setProperty("delete", "fa-trash");
 *         return props;
 *     }
 * }
 * }</pre>
 *
 * @author Mario A. Serrano Leones
 * @see IconsProvider
 * @see Icon
 * @see IconType
 */
public abstract class AbstractFontIconsProvider implements IconsProvider {

    /**
     * Internal cache storing all loaded icons by their logical name.
     */
    private final Map<String, Icon> icons = new HashMap<>();

    /**
     * Logging service for reporting icon loading progress and issues.
     */
    private final LoggingService logger = new SLF4JLoggingService(getClass());

    /**
     * Constructs a new AbstractFontIconsProvider and automatically initializes
     * the icon mappings by calling {@link #init()}.
     */
    public AbstractFontIconsProvider() {
        init();
    }

    /**
     * Initializes the icon provider by loading all icon mappings.
     * This method retrieves the Properties from {@link #getNamesMapping()},
     * iterates through all entries, and creates Icon objects for each mapping.
     *
     * <p>The initialization process:</p>
     * <ul>
     *   <li>Obtains the Properties object from the subclass</li>
     *   <li>Logs the number of icons being installed</li>
     *   <li>Iterates through each property entry</li>
     *   <li>Creates a new Icon with IconType.FONT for each mapping</li>
     *   <li>Stores the icon in the internal cache</li>
     *   <li>Logs successful completion</li>
     * </ul>
     */
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

    /**
     * Factory method for creating Icon instances.
     * Subclasses can override this method to customize how Icon objects are created,
     * for example to add additional metadata or use custom Icon subclasses.
     *
     * @param name the logical name of the icon (e.g., "save", "edit")
     * @param internalName the internal font-specific identifier (e.g., "fa-save", "md-edit")
     * @return a new Icon instance configured for font-based rendering
     */
    protected Icon newIcon(String name, String internalName) {
        return new Icon(name, internalName, IconType.FONT);
    }

    /**
     * Retrieves a font icon by its logical name.
     * If the icons cache is empty, triggers initialization automatically.
     *
     * @param name the logical name of the icon to retrieve
     * @return the Icon object if found, or null if not available in this provider
     */
    @Override
    public Icon getIcon(String name) {
        if (icons.isEmpty()) {
            init();
        }

        return icons.get(name);
    }

    /**
     * Provides the mapping between logical icon names and font-specific identifiers.
     * Subclasses must implement this method to supply their icon set configuration.
     *
     * <p>The Properties object should contain entries where:</p>
     * <ul>
     *   <li>Key: logical icon name used throughout the application (e.g., "save", "edit")</li>
     *   <li>Value: font-specific CSS class or identifier (e.g., "fa-save", "md-edit")</li>
     * </ul>
     *
     * <p>Example:</p>
     * <pre>{@code
     * Properties props = new Properties();
     * props.setProperty("save", "fa-floppy-disk");
     * props.setProperty("edit", "fa-pen-to-square");
     * props.setProperty("delete", "fa-trash-can");
     * return props;
     * }</pre>
     *
     * @return a Properties object containing icon name mappings, or null if no icons are available
     */
    public abstract Properties getNamesMapping();

    /**
     * Returns all icons provided by this font icon provider.
     * The returned list is a copy of the internal cache values.
     *
     * @return a list containing all font icons managed by this provider
     */
    @Override
    public List<Icon> getAll() {
        return new ArrayList<>(icons.values());
    }

    /**
     * Manually adds an icon to this provider's cache.
     * This method allows runtime addition of icons without requiring them to be
     * defined in the Properties returned by {@link #getNamesMapping()}.
     *
     * <p>Useful for dynamically registering icons or overriding existing ones.</p>
     *
     * @param name the logical name for the icon
     * @param icon the Icon object to register
     */
    public void addIcon(String name, Icon icon) {
        icons.put(name, icon);
    }

}
