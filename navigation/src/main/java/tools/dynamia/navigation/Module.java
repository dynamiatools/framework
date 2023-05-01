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
package tools.dynamia.navigation;

import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * @author Ing. Mario Serrano Leones
 */
public class Module extends NavigationElement<Module> implements Serializable, Cloneable {

    private static final long serialVersionUID = -817147208762427863L;
    private final Collection<PageGroup> pageGroups = new ArrayList<>();
    private Page mainPage;
    private PageGroup defaultGroup = new PageGroup();
    private final LoggingService logger = new SLF4JLoggingService();
    private final Map<String, Object> properties = new HashMap<>();
    private Class baseClass;
    private final List<Class> additionalBaseClasses = new ArrayList<>();

    public Module(String id, String name, String description) {
        super(id, name, description);
        setPosition(Double.MAX_VALUE);
        defaultGroup.setParentModule(this);
    }

    public Module(String id, String name) {
        this(id, name, null);
    }

    public Module() {
        this(null, null);
    }

    @Override
    public String getName() {
        return super.getName();
    }

    public Page getMainPage() {
        return mainPage;
    }

    public void setMainPage(Page mainAction) {
        this.mainPage = mainAction;
    }

    public Module addPageGroup(PageGroup pageGroup) {
        if (pageGroup == null) {
            logger.warn("Navigation " + this.getName() + " cannot add null PageGroup");
        }

        if (getPageGroupById(pageGroup.getId()) != null) {
            logger.warn("There is a PageGroup with the same ID added in " + getName() + "'s Navigation");
        } else {
            this.pageGroups.add(pageGroup);
            pageGroup.setParentModule(this);
        }
        return this;
    }

    public Module addPage(Page page) {
        defaultGroup.addPage(page);
        return this;
    }

    public Collection<PageGroup> getPageGroups() {
        return pageGroups;
    }

    public PageGroup getDefaultPageGroup() {
        return defaultGroup;
    }

    public void setDefaultGroup(PageGroup defaultGroup) {
        this.defaultGroup = defaultGroup;
        if (this.defaultGroup != null) {
            this.defaultGroup.setParentModule(this);
        }
    }

    public PageGroup getPageGroupById(String id) {

        for (PageGroup pg : pageGroups) {
            if (pg.getId().equalsIgnoreCase(id)) {
                return pg;
            }
        }
        return null;
    }

    @Override
    public Module clone() {
        Module m = (Module) super.clone();
        m.setMainPage(mainPage.clone());
        m.setBaseClass(baseClass);

        return m;
    }

    public PageGroup getFirstPageGroup() {
        return getPageGroups().stream().findFirst().orElse(null);
    }

    public Page getFirstPage() {
        if (getFirstPageGroup() != null)
            return getFirstPageGroup().getFirstPage();
        else
            return null;

    }

    public Object addProperty(String key, Object value) {
        return properties.put(key, value);
    }

    public Object removeProperty(String key) {
        return properties.remove(key);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    void reload() {
    }


    public static Module getRef(String id) {
        Module module = new Module();
        module.setId(id);
        module.setReference(true);
        return module;
    }

    @Override
    protected String getLocalizedText(Locale locale, String sufix, String defaultValue) {
        return findLocalizedTextByKey(locale, msgKey(sufix), defaultValue);
    }

    String findLocalizedTextByKey(Locale locale, String key, String defaultValue) {


        ResourceBundle resourceBundle = findBundle(locale, key);

        if (resourceBundle != null && resourceBundle.containsKey(key)) {

            return resourceBundle.getString(key);
        } else {
            return defaultValue;
        }
    }

    private ResourceBundle findBundle(Locale locale, String key) {
        ResourceBundle bundle = null;

        if (baseClass != null) {
            try {
                bundle = ResourceBundle.getBundle(baseClass.getName(), locale);
            } catch (MissingResourceException ex) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Cannot find resource bundle for module base class: " + getName());
                }
                bundle = null;
            }

            if ((bundle == null || !bundle.containsKey(key)) && !additionalBaseClasses.isEmpty()) {
                for (Class addbaseclass : additionalBaseClasses) {
                    try {
                        bundle = ResourceBundle.getBundle(addbaseclass.getName(), locale);
                        if (bundle.containsKey(key)) {
                            break;
                        }
                    } catch (MissingResourceException e) {
                        //not bundle found in additional base class
                    }
                }
            }
        }

        return bundle;
    }


    private String buildBundleCacheKey(Class targetClass, Locale locale) {
        return targetClass.getName() + locale.toString();
    }

    public Class getBaseClass() {
        return baseClass;
    }

    public void setBaseClass(Class baseClass) {
        this.baseClass = baseClass;
    }

    public void addBaseClass(Class baseClass) {
        if (baseClass != null && !additionalBaseClasses.contains(baseClass)) {
            additionalBaseClasses.add(baseClass);
        }
    }

    public boolean isEmpty() {
        return getPageGroups().isEmpty() && getDefaultPageGroup().getPages().isEmpty();
    }

    /**
     * Traverse all pages in all (NON dynamic) groups and subgroups
     *
     */
    public void forEachPage(Consumer<Page> action) {
        var allGroups = new ArrayList<PageGroup>();
        allGroups.add(defaultGroup);
        allGroups.addAll(pageGroups);

        allGroups.forEach(grp -> forEachPageGroup(action, grp));
    }

    private void forEachPageGroup(Consumer<Page> action, PageGroup grp) {
        if (!grp.isDynamic()) {
            var pages = grp.getPages();
            if (pages != null && !pages.isEmpty()) {
                pages.forEach(action);
            }
            if (grp.getPageGroups() != null && !grp.getPageGroups().isEmpty()) {
                grp.getPageGroups().forEach(subgrp -> forEachPageGroup(action, subgrp));
            }
        }
    }
}
