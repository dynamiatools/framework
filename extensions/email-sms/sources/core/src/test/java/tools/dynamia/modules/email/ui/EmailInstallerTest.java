package tools.dynamia.modules.email.ui;

import org.junit.jupiter.api.Test;
import tools.dynamia.crud.CrudPage;
import tools.dynamia.navigation.Page;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailInstallerTest {

    @Test
    void coreDoesNotDependOnZk() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("org.zkoss.zk.ui.Component"));
    }

    @Test
    void registersTheEmailCrudPagesInTheSystemModule() {
        var module = new EmailInstaller().getModule();

        var pages = module.getPageGroupById("email").getPages();

        assertEquals("system", module.getId());
        assertEquals(List.of("accounts", "templates", "addresses", "emailLog", "smsLog"),
                pages.stream().map(Page::getId).toList());
        pages.forEach(page -> assertInstanceOf(CrudPage.class, page));
    }
}
