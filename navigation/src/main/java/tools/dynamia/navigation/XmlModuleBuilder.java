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
package tools.dynamia.navigation;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import tools.dynamia.commons.SimpleTemplateEngine;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.integration.Containers;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Read and parse XML Navigation files to Modules
 *
 * @author Mario A. Serrano Leones
 */
public class XmlModuleBuilder implements ModuleBuilder {

    private final Document xml;
    private final SAXReader saxReader = new SAXReader();
    private final LoggingService logger = Containers.get().findObject(LoggingService.class);

    public XmlModuleBuilder(File file) {
        try {
            this.xml = saxReader.read(file);
        } catch (DocumentException ex) {
            throw new ModuleBuilderException(ex);
        }
    }

    public XmlModuleBuilder(InputStream inputStream) {
        try {
            this.xml = saxReader.read(inputStream);
        } catch (DocumentException ex) {
            throw new ModuleBuilderException(ex);
        }
    }

    public XmlModuleBuilder(Reader reader) {
        try {
            this.xml = saxReader.read(reader);
        } catch (DocumentException ex) {
            throw new ModuleBuilderException(ex);
        }
    }

    public XmlModuleBuilder(URL url) {
        try {
            this.xml = saxReader.read(url);
        } catch (DocumentException ex) {
            throw new ModuleBuilderException(ex);
        }
    }

    @Override
    public Module build() {
        Module module = new Module();
        Element root = xml.getRootElement();

        // GET AND PARSE Properties
        module.getProperties().clear();
        Element properties = root.element("properties");
        if (properties != null && properties.elements().size() > 0) {
            for (Object object : properties.elements()) {
                Element prop = (Element) object;
                module.addProperty(prop.getName(), prop.getTextTrim());
            }
        }

        loadDefaultProperties(module, module, root);
        if (module.getId() == null) {
            module.setId(module.getName());
        }
        String bundleFile = root.attributeValue("bundle");
        ResourceBundle bundle = null;
        if (bundleFile != null && bundleFile.length() > 0) {
            try {
                bundle = ResourceBundle.getBundle(bundleFile, Locale.ROOT);
            } catch (Exception ex) {
                logger.error("Error loading resource bundle", ex);
            }
        }

        if (root.attributeValue("page") != null) {
            Page moduleMainPage = new Page();
            moduleMainPage.setPath(root.attributeValue("page"));
            moduleMainPage.setName(SimpleTemplateEngine.parse(module.getName(), module.getProperties()));
            moduleMainPage.setId(module.getName());
            module.setMainPage(moduleMainPage);
        }

        // GET AND PARSE PageGroups and pages
        List groups = root.elements("page-group");
        for (Object object : groups) {
            Element xmlSet = (Element) object;
            PageGroup group = new PageGroup();
            loadDefaultProperties(module, group, xmlSet);
            group.setListeners(xmlSet.attributeValue("listeners"));
            module.addPageGroup(group);
            logger.debug(group.getName());
            List pages = xmlSet.elements();

            for (Object elem : pages) { // Pages

                Element xmlPage = (Element) elem;
                Page page = new Page();
                loadDefaultProperties(module, page, xmlPage);
                page.setPath(SimpleTemplateEngine.parse(xmlPage.attributeValue("path"), module.getProperties()));
                if (xmlPage.attributeValue("showAsPopup") != null && xmlPage.attributeValue("showAsPopup").equals("true")) {
                    page.setShowAsPopup(true);
                }
                group.addPage(page);
                logger.debug("   -" + page.getPageGroup().getName() + "/" + page.getName());
            }
        }

        return module;
    }

    private void loadDefaultProperties(Module module, NavigationElement e, Element xml) {
        Map<String, Object> vars = module.getProperties();

        e.setId(xml.attributeValue("id"));
        e.setPosition(Double.parseDouble(xml.attributeValue("position", "0")));
        e.setEnable(Boolean.parseBoolean(xml.attributeValue("enable", "true")));
        e.setVisible(Boolean.parseBoolean(xml.attributeValue("visible", "true")));
        e.setName(SimpleTemplateEngine.parse(xml.attributeValue("name"), vars));
        e.setDescription(SimpleTemplateEngine.parse(xml.attributeValue("description"), vars));
        e.setIcon(SimpleTemplateEngine.parse(xml.attributeValue("icon"), vars));
        e.setRenderOnUserRoles(SimpleTemplateEngine.parse(xml.attributeValue("renderOnUserRoles", "ROLE_ADMIN"), vars));
    }
}
