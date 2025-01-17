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

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.dynamia.commons.SimpleCache;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.Containers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * {@link Module} container store modules instances loaded from  {@link ModuleProvider}s
 *
 * @author Ing. Mario Serrano Leones
 */
@Component
public final class ModuleContainer implements Serializable {

    public static ModuleContainer getInstance() {
        return Containers.get().findObject(ModuleContainer.class);
    }

    private final transient LoggingService LOGGER = new SLF4JLoggingService(ModuleContainer.class);

    private final List<Module> modules;
    private final List<Page> featuredPages;

    @Autowired
    private transient List<ModuleProvider> _providers;
    private final SimpleCache<String, Page> PAGE_PATH_INDEX = new SimpleCache<>();
    private final SimpleCache<String, Page> PAGE_PRETTY_PATH_INDEX = new SimpleCache<>();

    private String defaultPagePath;


    public ModuleContainer() {
        LOGGER.info("Creating " + getClass());
        modules = new ArrayList<>();
        featuredPages = new ArrayList<>();
    }

    @PostConstruct
    private void loadModules() {

        if (getModulesProviders() != null) {
            List<Module> modulesReferences = new ArrayList<>();
            LOGGER.info("Loading " + getModulesProviders().size() + " modules");
            for (ModuleProvider moduleProvider : getModulesProviders()) {
                Module module = moduleProvider.getModule();
                if (module != null) {
                    if (module.getBaseClass() == null && !module.isReference()) {
                        module.setBaseClass(moduleProvider.getClass());
                    }

                    if (module.isReference()) {
                        modulesReferences.add(module);
                    } else {
                        installModule(module);
                    }
                }
            }

            for (Module moduleRef : modulesReferences) {
                installModule(moduleRef);
            }
        }
    }

    public List<ModuleProvider> getModulesProviders() {
        return _providers;
    }

    public void installModule(Module module) {
        if (module != null) {
            install(module);
            if (module.isReference()) {
                LOGGER.info("Module Reference [" + module.getId() + "] installed ");
            } else {
                LOGGER.info("Module [" + module.getId() + "] installed ");
            }
        }
    }

    private void install(Module newModule) {
        if (newModule == null) {
            throw new NavigationException("Cannot install null module");
        }

        Module existingModule = this.getModuleById(newModule.getId());
        if (existingModule == null) {
            add(newModule);
        } else {
            if (newModule.getBaseClass() != null) {
                existingModule.addBaseClass(newModule.getBaseClass());
            }

            newModule.getDefaultPageGroup().getPages().forEach(page -> {
                existingModule.addPage(page);
                index(page);
            });

            List<PageGroup> groups = newModule.getPageGroups();
            mergePageGroups(groups, existingModule, null);

        }
    }

    protected void add(Module newModule) {
        if (newModule != null) {
            modules.add(newModule);
            index(newModule);
        }
    }

    private void mergePageGroups(List<PageGroup> newModuleGroups, Module parentModule, PageGroup parentGroup) {
        for (PageGroup newGroup : newModuleGroups) {
            PageGroup existingGroup = parentGroup != null ? parentGroup.getPageGroupById(newGroup.getId()) : parentModule.getPageGroupById(newGroup.getId());

            if (existingGroup == null) {
                if (parentGroup != null) {
                    parentGroup.addPageGroup(newGroup);
                    if (!newGroup.isDynamic()) {
                        newGroup.getPages().forEach(this::index);
                    }
                } else {
                    parentModule.addPageGroup(newGroup);
                }
            } else if (!newGroup.isDynamic()) {
                for (Page newPage : newGroup.getPages()) {
                    index(newPage);
                    Page oldPage = existingGroup.getPageById(newPage.getId());
                    if (oldPage == null) {
                        existingGroup.addPage(newPage);
                    } else {
                        LOGGER.warn("Page " + newPage.getVirtualPath()
                                + " already exists in the module container, another page has same ID. Cannot install  "
                                + newPage.getClass().getSimpleName());
                    }
                }
                if (!newGroup.isDynamic() && newGroup.getPageGroups() != null) {
                    mergePageGroups(newGroup.getPageGroups(), parentModule, existingGroup);
                }
            }
        }
    }


    protected void index(Module module) {
        if (module != null) {
            module.forEachPage(this::index);
        }
    }

    protected void index(Page page) {
        PAGE_PATH_INDEX.add(page.getVirtualPath(), page);
        PAGE_PRETTY_PATH_INDEX.add(page.getPrettyVirtualPath(), page);
        if (page.isFeatured()) {
            featuredPages.add(page);
        }

        if (page.isMain()) {

        }
    }

    void reloadModule(Module module) {
        try {
            module.reload();
        } catch (Exception ex) {
            LOGGER.error("Error reloading web module " + module.getName(), ex);
        }
    }

    public Collection<Module> getModules() {
        if (modules.isEmpty()) {
            loadModules();
        }
        return modules;
    }

    public Module getModuleById(String id) {
        for (Module webModule : modules) {
            if (webModule.getId().equalsIgnoreCase(id)) {
                return webModule;
            }
        }
        return null;
    }

    public int getModuleCount() {
        return modules.size();
    }

    public Module getFirstModule() {
        for (Module module : getModules()) {
            return module;
        }
        return null;
    }

    public Page findPage(String path) {
        Page page = PAGE_PATH_INDEX.get(path);

        try {
            if (page == null) {
                for (Module module : modules) {
                    page = module.findPage(path);
                    if (page != null) {
                        index(page);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }

        if (page != null) {
            return page;
        } else {
            throw new PageNotFoundException("Cannot be found page for path " + path + " in container ");
        }
    }

    public Page findPageByPrettyVirtualPath(String prettyPath) {
        Page page = PAGE_PRETTY_PATH_INDEX.get(prettyPath);

        try {
            if (page == null) {
                for (Module module : modules) {
                    page = module.findPageByPrettyPath(prettyPath);
                    if (page != null) {
                        index(page);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }

        if (page != null) {
            return page;
        } else {
            throw new PageNotFoundException("Cannot be found page for path " + prettyPath);
        }
    }

    public List<Page> findPagesByName(String name) {

        List<Page> result = new ArrayList<>();

        if (name == null || name.isEmpty()) {
            return result;
        }
        name = name.toLowerCase();
        for (Module module : modules) {

            List<PageGroup> modulePageGroups = new ArrayList<>();
            modulePageGroups.add(module.getDefaultPageGroup());
            modulePageGroups.addAll(module.getPageGroups());

            for (PageGroup pageGroup : modulePageGroups) {
                for (Page page : pageGroup.getPages()) {
                    if (page.getName().toLowerCase().contains(name) || (pageGroup.getName() != null && pageGroup.getName().toLowerCase().contains(name))) {
                        result.add(page);
                    }
                }

                for (PageGroup subgroup : pageGroup.getPageGroups()) {
                    for (Page page : subgroup.getPages()) {
                        if (page.getName().toLowerCase().contains(name) || (subgroup.getName() != null && subgroup.getName().toLowerCase().contains(name))) {
                            result.add(page);
                        }
                    }
                }
            }
        }
        return result;
    }

    public Collection<Page> getFeaturedPages() {
        return featuredPages;
    }

    public NavigationElement findElement(String path) {
        NavigationElement elem = PAGE_PATH_INDEX.get(path);
        if (elem != null) {
            return elem;
        }


        Module pathModule = null;

        if (path.contains("/")) {
            try {
                String[] p = path.split("/");
                pathModule = getModuleById(p[0]);
            } catch (Exception ignored) {
            }
        } else {
            pathModule = getModuleById(path);
        }

        if (pathModule == null) {
            return null;
        }

        if (path.equals(pathModule.getVirtualPath())) {
            return pathModule;
        }


        if (path.equals(pathModule.getDefaultPageGroup().getVirtualPath())) {
            return pathModule.getDefaultPageGroup();
        }

        var page = pathModule.getDefaultPageGroup().getPages().stream().filter(pg -> path.equals(pg.getVirtualPath())).findFirst();
        if (page.isPresent()) {
            return page.get();
        }

        var group = pathModule.getPageGroups().stream().filter(pg -> path.equals(pg.getVirtualPath())).findFirst();
        if (group.isPresent()) {
            return group.get();
        }

        var pageGroups = pathModule.getPageGroups();
        var pg = findPage(path, pageGroups);
        if (pg != null) {
            index(pg);
        }

        return pg;
    }


    private Page findPage(String path, Collection<PageGroup> pageGroups) {
        for (PageGroup grp : pageGroups) {
            for (Page pg : grp.getPages()) {
                if (path.equals(pg.getVirtualPath())) {
                    return pg;
                }
            }
            if (grp.getPageGroups() != null && !grp.getPageGroups().isEmpty()) {
                return findPage(path, grp.getPageGroups());
            }
        }
        return null;
    }

    public String getDefaultPagePath() {
        return defaultPagePath;
    }

    /**
     * Set default page path. It will used when no page is active
     *
     * @param defaultPagePath
     */
    public void setDefaultPagePath(String defaultPagePath) {
        this.defaultPagePath = defaultPagePath;
    }


}
