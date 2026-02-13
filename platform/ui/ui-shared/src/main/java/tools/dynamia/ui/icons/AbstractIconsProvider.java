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

import tools.dynamia.commons.StringUtils;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.io.IOUtils;
import tools.dynamia.io.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for IconsProvider implementations that serve image-based icons.
 * This class provides common functionality for loading and managing icon sets from file resources
 * such as PNG, SVG, GIF, or other image formats stored in the classpath.
 *
 * <p>Image-based icons are loaded from a conventional directory structure under the classpath:
 * {@code classpath:web/{prefix}/16/*.{extension}}. The provider scans this directory at initialization
 * and automatically registers all icons found.</p>
 *
 * <p>Subclasses must implement two methods to configure the icon location:</p>
 * <ul>
 *   <li>{@link #getPrefix()}: Defines the subdirectory name under web/ where icons are located</li>
 *   <li>{@link #getExtension()}: Specifies the file extension of the icon images</li>
 * </ul>
 *
 * <p>The initialization process:</p>
 * <ul>
 *   <li>Constructs the classpath pattern using prefix and extension</li>
 *   <li>Scans the directory for matching resources</li>
 *   <li>Creates Icon objects for each found resource</li>
 *   <li>Caches icons in an internal map for quick retrieval</li>
 *   <li>Logs the installation progress and any errors</li>
 * </ul>
 *
 * <p>Example implementation:</p>
 * <pre>{@code
 * @Component
 * public class SilkIconsProvider extends AbstractIconsProvider {
 *
 *     @Override
 *     public String getPrefix() {
 *         return "silk"; // Icons in classpath:web/silk/16/*.png
 *     }
 *
 *     @Override
 *     public String getExtension() {
 *         return "png";
 *     }
 * }
 * }</pre>
 *
 * <p>Expected directory structure:</p>
 * <pre>
 * src/main/resources/
 *   web/
 *     silk/           (prefix)
 *       16/           (fixed size directory)
 *         save.png    (icon files with extension)
 *         edit.png
 *         delete.png
 * </pre>
 *
 * @author Mario A. Serrano Leones
 * @see IconsProvider
 * @see Icon
 * @see IconType
 */
public abstract class AbstractIconsProvider implements IconsProvider {

	/**
	 * Logging service for reporting icon loading progress and errors.
	 */
	private final LoggingService logger = new SLF4JLoggingService(getClass());

	/**
	 * Internal cache storing all loaded icons by their name (without extension).
	 */
	private final Map<String, Icon> icons = new HashMap<>();

	/**
	 * List containing all loaded icons for quick retrieval.
	 */
	private final List<Icon> all = new ArrayList<>();

	/**
	 * Constructs a new AbstractIconsProvider and automatically initializes
	 * the icon set by scanning the classpath directory.
	 */
	public AbstractIconsProvider() {
		init();
	}

	/**
	 * Initializes the icon provider by scanning and loading icon resources from the classpath.
	 * This method builds the resource path using the prefix and extension provided by subclasses,
	 * scans for matching resources, and creates Icon objects for each found file.
	 *
	 * <p>The scanning process:</p>
	 * <ul>
	 *   <li>Constructs the classpath pattern: {@code classpath:web/{prefix}/16/*.{extension}}</li>
	 *   <li>Logs the installation path for debugging</li>
	 *   <li>Uses IOUtils to scan for matching resources</li>
	 *   <li>Warns if no icons are found in the directory</li>
	 *   <li>Iterates through found resources and extracts icon names from filenames</li>
	 *   <li>Creates Icon objects with IconType.IMAGE for each resource</li>
	 *   <li>Stores icons in both the cache map and the complete list</li>
	 *   <li>Logs successful completion or errors</li>
	 * </ul>
	 *
	 * <p>The icon name is derived from the filename without its extension.
	 * For example, {@code save.png} becomes an icon with name {@code "save"}.</p>
	 */
	private void init() {
		String prefix = getPrefix();
		String extension = getExtension();
		try {
			String path = "classpath:web/" + prefix + "/16/*." + extension;

			logger.info("Installing Icons in " + path);

			Resource[] resources = IOUtils.getResources(path);
			if (resources.length == 0) {
				logger.warn("No icons to install in " + path);
			} else {
				logger.info("Installing " + resources.length + " icons from " + getClass());
			}

			for (Resource r : resources) {
				String name = StringUtils.removeFilenameExtension(r.getFilename());
				Icon icon = new Icon(name, "~./" + prefix, extension);
				icons.put(name, icon);
				all.add(icon);
			}
			logger.info(" Icons installed successfuly");
		} catch (IOException ex) {
			logger.error("Error installing icons in " + prefix + ": " + ex.getMessage());
		}
	}

	/**
	 * Returns the prefix (subdirectory name) where icons are located under the web/ directory.
	 * This prefix is used to construct the classpath resource pattern for icon scanning.
	 *
	 * <p>The prefix defines the icon set name and should match the directory structure:
	 * {@code src/main/resources/web/{prefix}/16/}</p>
	 *
	 * <p>Examples:</p>
	 * <ul>
	 *   <li>"silk" for Silk icon set → {@code web/silk/16/*.png}</li>
	 *   <li>"fugue" for Fugue icon set → {@code web/fugue/16/*.png}</li>
	 *   <li>"custom" for custom icons → {@code web/custom/16/*.svg}</li>
	 * </ul>
	 *
	 * @return the prefix (subdirectory name) for the icon set
	 */
	public abstract String getPrefix();

	/**
	 * Returns the file extension for icon image files (without the leading dot).
	 * This extension is used to filter icon resources during directory scanning.
	 *
	 * <p>Common extensions include:</p>
	 * <ul>
	 *   <li>"png" for PNG images (most common)</li>
	 *   <li>"svg" for SVG vector graphics</li>
	 *   <li>"gif" for GIF images</li>
	 *   <li>"jpg" or "jpeg" for JPEG images</li>
	 * </ul>
	 *
	 * @return the file extension without the leading dot (e.g., "png", "svg", "gif")
	 */
	public abstract String getExtension();

	/**
	 * Retrieves an image icon by its logical name.
	 * If the icons cache is empty, triggers initialization automatically.
	 *
	 * <p>The name should match the filename (without extension) of an icon in the
	 * configured directory. For example, if {@code save.png} exists in the icon directory,
	 * use "save" as the name parameter.</p>
	 *
	 * @param name the logical name of the icon (filename without extension)
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
	 * Returns all image icons provided by this icon provider.
	 * The returned list contains all icons discovered during initialization.
	 *
	 * @return a list containing all image icons managed by this provider
	 */
	@Override
	public List<Icon> getAll() {
		return all;

	}
}
