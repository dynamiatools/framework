package tools.dynamia.modules.security.ui;

import org.junit.jupiter.api.Test;
import tools.dynamia.crud.CrudPage;
import tools.dynamia.navigation.ModuleContainer;
import tools.dynamia.navigation.Page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurityModuleProviderTest {

    @Test
    void coreDoesNotDependOnZk() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("org.zkoss.zk.ui.Component"));
    }

    @Test
    void registersTheCrudPagesWithoutAnyZkPage() {
        var container = new ModuleContainer();
        container.installModule(new SecurityModuleProvider().getModule());

        var pages = container.getModuleById("system").getPageGroupById("security").getPages();

        assertEquals(java.util.List.of("users", "profiles", "tokens"), pages.stream().map(Page::getId).toList());
        pages.forEach(page -> assertInstanceOf(CrudPage.class, page));
    }
}
