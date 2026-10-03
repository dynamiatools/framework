package tools.dynamia.modules.security.ui;

import org.junit.jupiter.api.Test;
import tools.dynamia.navigation.ModuleContainer;
import tools.dynamia.navigation.Page;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityProvidersMergeTest {

    @Test
    void myProfileAndTheCorePagesMergeIntoOneGroupWithMyProfileFirst() {
        var container = new ModuleContainer();
        container.installModule(new SecurityModuleProvider().getModule());
        container.installModule(new SecurityProfileModuleProvider().getModule());

        assertEquals(1, container.getModuleCount());
        var group = container.getModuleById("system").getPageGroupById("security");
        var pages = group.getPages().stream().sorted(java.util.Comparator.comparingDouble(Page::getPosition)).toList();

        assertEquals(List.of("myProfile", "users", "profiles", "tokens"), pages.stream().map(Page::getId).toList());
        assertTrue(container.findPage("system/security/myProfile").isAlwaysAllowed());
        assertEquals("system/security/myProfile", container.findPage("system/security/myProfile").getVirtualPath());
    }
}
