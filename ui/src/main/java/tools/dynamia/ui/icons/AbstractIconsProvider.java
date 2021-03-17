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
 *
 * @author Mario A. Serrano Leones
 */
public abstract class AbstractIconsProvider implements IconsProvider {

	private final LoggingService logger = new SLF4JLoggingService(getClass());
	private final Map<String, Icon> icons = new HashMap<>();
	private final List<Icon> all = new ArrayList<>();

	public AbstractIconsProvider() {
		init();
	}

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

	public abstract String getPrefix();

	public abstract String getExtension();

	@Override
	public Icon getIcon(String name) {
		if (icons.isEmpty()) {
			init();
		}
		return icons.get(name);
	}

	@Override
	public List<Icon> getAll() {
		return all;

	}
}
