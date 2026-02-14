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

/**
 * IconsProvider implementation for Font Awesome icon library.
 * This provider loads and manages Font Awesome icons, supporting both predefined mappings
 * from a properties file and dynamic icon name resolution.
 *
 * <p>The provider supports multiple icon resolution strategies:</p>
 * <ul>
 *   <li>Loading predefined icon mappings from {@code /META-INF/dynamia/fa-icons.properties}</li>
 *   <li>Dynamically resolving icons with "fa-" prefix (e.g., "fa-save" → "fa fa-save")</li>
 *   <li>Direct Font Awesome class names (e.g., "fa fa-user", "fab fa-github")</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Icon saveIcon = provider.getIcon("save");           // From properties file
 * Icon customIcon = provider.getIcon("fa-user");      // Dynamic resolution
 * Icon brandIcon = provider.getIcon("fab fa-github"); // Direct class name
 * }</pre>
 *
 * @see AbstractFontIconsProvider
 * @see FAIcon
 */
public class FontAwesomeIconsProvider extends AbstractFontIconsProvider {

    /**
     * Logger for reporting icon loading errors and warnings.
     */
    private static final LoggingService logger = new SLF4JLoggingService(FontAwesomeIconsProvider.class);

    /**
     * Provides the mapping between logical icon names and Font Awesome class names.
     * Loads the icon mappings from a properties file located at {@code /META-INF/dynamia/fa-icons.properties}.
     *
     * <p>The properties file format:</p>
     * <pre>
     * save=floppy-disk
     * edit=pen-to-square
     * delete=trash-can
     * </pre>
     *
     * @return Properties object containing icon name mappings, or empty Properties if loading fails
     */
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

    /**
     * Returns the classpath location of the Font Awesome icons properties file.
     * Subclasses can override this method to provide a different properties file location.
     *
     * @return the classpath path to the icons properties file
     */
    protected String getIconsPath() {
        return "/META-INF/dynamia/fa-icons.properties";
    }

    /**
     * Retrieves a Font Awesome icon by its name with support for multiple resolution strategies.
     *
     * <p>Resolution order:</p>
     * <ol>
     *   <li>Checks the predefined mappings from properties file</li>
     *   <li>If not found and name starts with "fa-", creates icon dynamically</li>
     *   <li>If name starts with "fa " or "fab ", uses it as direct Font Awesome class</li>
     * </ol>
     *
     * <p>Examples:</p>
     * <pre>{@code
     * getIcon("save");           // From properties: fa fa-floppy-disk
     * getIcon("fa-user");        // Dynamic: fa fa-user
     * getIcon("fab fa-github");  // Direct: fab fa-github
     * }</pre>
     *
     * @param name the icon name or Font Awesome class
     * @return the Icon object, or null if not resolvable
     */
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

    /**
     * Returns the prefix used for Font Awesome icon names.
     * This prefix is used in dynamic icon resolution and icon class generation.
     *
     * @return the icon prefix, default is "fa-"
     */
    protected String getIconsPrefix() {
        return "fa-";
    }

    /**
     * Creates a new FAIcon instance with proper Font Awesome class formatting.
     * Constructs the full Font Awesome CSS class by combining "fa " prefix with the icon name.
     *
     * @param name the logical name of the icon
     * @param internalName the Font Awesome specific icon name (without "fa-" prefix)
     * @return a new FAIcon instance configured with Font Awesome classes
     */
    @Override
    protected Icon newIcon(String name, String internalName) {
        return new FAIcon(name, "fa " + getIconsPrefix() + internalName);
    }
}
