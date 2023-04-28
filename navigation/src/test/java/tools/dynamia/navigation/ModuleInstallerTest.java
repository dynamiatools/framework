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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class ModuleInstallerTest {

    /**
     * Test of install method, of class ModuleInstaller.
     */
    @Test
    public void testInstall() {
        ModuleContainer container = new ModuleContainer();

        Module mod1 = createModule1();
        Module mod2 = createModule2();

        container.installModule(mod1);
        container.installModule(mod2);

        assertEquals(1, container.getModuleCount());

        Module modFinal = container.getModuleById("mod");
        assertEquals(1, modFinal.getPageGroups().size());

        for (PageGroup pageGroup : modFinal.getPageGroups()) {
            for (Page page : pageGroup.getPages()) {
            }
        }
    }

    // @Test(expected = PageAlreadyExistsException.class)
    public void testInstallExistingPage() {
        ModuleContainer container = new ModuleContainer();

        // Repetido a proposito
        Module mod1 = createModule2();
        Module mod2 = createModule2();

        container.installModule(mod1);
        container.installModule(mod2);
    }

    private Module createModule1() {
        Module m = new Module("mod", "module");
        m.addPageGroup(new PageGroup("grp1", "grupo").addPage(new Page("page1", "page1", "the/page")).addPage(
                new Page("page2", "page2", "the/page/2")));

        return m;
    }

    private Module createModule2() {
        Module m = new Module("mod", "module");
        m.addPageGroup(new PageGroup("grp1", "grupo").addPage(new Page("page3", "page3", "the/page/3")));
        return m;
    }
}
