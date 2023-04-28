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
package tools.dynamia.app;

import tools.dynamia.actions.AbstractAction;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.ActionLoader;
import tools.dynamia.app.template.ApplicationGlobalAction;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.PropertiesContainer;
import tools.dynamia.commons.reflect.PropertyInfo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author Mario A. Serrano Leones
 */
public class ApplicationInfo implements Serializable, PropertiesContainer {


    private static final long serialVersionUID = 5172228141974498693L;
    public static final String TEMPLATE = "template";
    public static final String DEFAULT_SKIN = "defaultSkin";
    public static final String BASE_PACKAGE = "basePackage";
    public static final String NAME = "name";
    public static final String SHORT_NAME = "shortName";
    public static final String LICENSE = "license";
    public static final String JNDI_NAME = "jndiName";
    public static final String DESCRIPTION = "description";
    public static final String AUTHOR = "author";
    public static final String COMPANY = "company";
    public static final String URL = "url";
    public static final String VERSION = "version";
    public static final String BUILD = "build";
    public static final String DEFAULT_LOGO = "defaultLogo";
    public static final String DEFAULT_ICON = "defaultIcon";
    public static final String JPA_DIALECT = "jpaDialect";
    public static final String WEB_CACHE_ENABLED = "webCacheEnabled";

    private final Map<String, String> properties;
    private final Map<String, String> systemProperties;
    private final Map<String, String> systemEnvironment;

    private ApplicationInfo() {
        systemProperties = new HashMap(System.getProperties());
        systemEnvironment = System.getenv();
        properties = new HashMap<>();
        properties.put(TEMPLATE, "Default");
    }

    public String getDefaultSkin() {
        return properties.get(DEFAULT_SKIN);
    }

    @Override
    public void addProperty(String name, String value) {
        properties.put(name, value);
    }

    @Override
    public String getProperty(String name) {
        return properties.get(name);
    }

    public String getBasePackage() {
        return properties.get(BASE_PACKAGE);
    }

    public String getName() {
        return properties.get(NAME);
    }

    public String getShortName() {
        return properties.get(SHORT_NAME);
    }

    public void setName(String name) {
        properties.put(NAME, name);
    }

    public void setShortName(String shortName) {
        addProperty(SHORT_NAME, shortName);
    }

    public void setTemplate(String template) {
        properties.put(TEMPLATE, template);
    }

    public void setDefaultSkin(String defaultSkin) {
        properties.put(DEFAULT_SKIN, defaultSkin);
    }

    public Map<String, String> getSystemEnvironment() {
        return systemEnvironment;
    }

    public Map<String, String> getSystemProperties() {
        return systemProperties;
    }

    public String getLicense() {
        return properties.get(LICENSE);
    }

    public String getJndiName() {
        return properties.get(JNDI_NAME);
    }

    public String getDescription() {
        return properties.get(DESCRIPTION);
    }

    public String getAuthor() {
        return properties.get(AUTHOR);
    }

    public String getCompany() {
        return properties.get(COMPANY);
    }

    public String getUrl() {
        return properties.get(URL);
    }

    public String getVersion() {
        return properties.get(VERSION);
    }

    public String getBuild() {
        return properties.get(BUILD);
    }

    public String getTemplate() {
        return properties.get(TEMPLATE);
    }

    public String getDefaultLogo() {
        return properties.get(DEFAULT_LOGO);
    }

    public String getDefaultIcon() {
        return properties.get(DEFAULT_ICON);
    }

    public String getJpaDialect() {
        return properties.get(JPA_DIALECT);
    }

    public boolean isWebCacheEnabled() {
        if (systemProperties.containsKey(WEB_CACHE_ENABLED)) {
            properties.put(WEB_CACHE_ENABLED, systemProperties.get(WEB_CACHE_ENABLED));
        }

        if (!properties.containsKey(WEB_CACHE_ENABLED)) {
            properties.put(WEB_CACHE_ENABLED, "true");
        }

        return "true".equalsIgnoreCase(properties.get(WEB_CACHE_ENABLED));
    }

    public static ApplicationInfo load(Properties prop) {
        List<PropertyInfo> propsInfo = BeanUtils.getPropertiesInfo(ApplicationInfo.class);
        ApplicationInfo app = new ApplicationInfo();
        for (PropertyInfo propertyInfo : propsInfo) {
            String value = prop.getProperty(propertyInfo.getName());
            if (value != null) {
                app.addProperty(propertyInfo.getName(), value);
            }
        }

        prop.stringPropertyNames().forEach(key -> {
            if (key.startsWith("prop.")) {
                try {
                    String value = prop.getProperty(key);
                    key = key.substring("prop.".length());
                    app.addProperty(key, value);
                } catch (Exception e) {
                }
            }
        });

        app.customize();

        return app;
    }

    public static ApplicationInfo dummy() {
        ApplicationInfo app = new ApplicationInfo();
        app.addProperty(NAME, "Unknow");
        app.addProperty("version", "0.0.0");
        app.addProperty(JNDI_NAME, "jdbc/datasource");
        return app;
    }

    private void customize() {
        String customizerClass = getProperty("customizerClass");
        if (customizerClass != null && !customizerClass.isEmpty()) {
            try {
                ApplicationCustomizer customizer = BeanUtils.newInstance(customizerClass);
                customizer.customize(this);
            } catch (Exception e) {
                throw new ApplicationException("Error customizing app", e);
            }
        }
    }

    public List<ApplicationGlobalAction> getGlobalActions() {
        ActionLoader<ApplicationGlobalAction> loader = new ActionLoader<>(ApplicationGlobalAction.class);
        loader.setIgnoreRestrictions(true);
        return loader.load().stream().filter(AbstractAction::isEnabled).collect(Collectors.toList());
    }

    public void execute(ApplicationGlobalAction action) {
        if (action != null) {
            action.actionPerformed(new ActionEvent(this, this));
        }
    }

}
