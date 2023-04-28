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

/**
 *
 * @author Mario A. Serrano Leones
 */
public class JavaModuleBuilder implements ModuleBuilder {

    private final Module module;
    private PageGroup currentGroup;

    public JavaModuleBuilder(String name) {
        this(name, name);
    }

    public JavaModuleBuilder(String id, String name) {
        this(id, name, null);
    }

    public JavaModuleBuilder(String id, String name, String icon) {
        this(id, name, icon, 0);
    }

    public JavaModuleBuilder(String id, String name, String icon, double position) {
        module = new Module(id, name);
        module.setIcon(icon);
        module.setPosition(position);
    }

    public JavaModuleBuilder addGroup(String name) {
        return addGroup(name.toLowerCase(), name);
    }

    public JavaModuleBuilder addGroup(String id, String name) {
        return addGroup(id, name, 0);
    }

    public JavaModuleBuilder addGroup(String id, String name, double position) {
        currentGroup = new PageGroup(id, name);
        currentGroup.setPosition(position);
        module.addPageGroup(currentGroup);
        return this;

    }

    public JavaModuleBuilder addPage(Page page) {
        currentGroup.addPage(page);
        return this;
    }

    public JavaModuleBuilder addPage(String name, String path) {
        return addPage(name.toLowerCase(), name, path);
    }

    public JavaModuleBuilder addPage(String id, String name, String path) {
        return addPage(id, name, path, null);
    }

    public JavaModuleBuilder addPage(String id, String name, String path, String icon) {
        return addPage(id, name, path, icon, 0);
    }

    public JavaModuleBuilder addPage(String id, String name, String path, String icon, double position) {
        Page page = new Page(id, name, path);
        page.setIcon(icon);
        page.setPosition(position);
        return addPage(page);
    }

    @Override
    public Module build() {
        return module;
    }
}
