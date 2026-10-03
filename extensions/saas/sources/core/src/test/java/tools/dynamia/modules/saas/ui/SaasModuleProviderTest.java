package tools.dynamia.modules.saas.ui;

import org.junit.jupiter.api.Test;
import tools.dynamia.crud.CrudPage;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.navigation.ModuleContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SaasModuleProviderTest {

    @Test
    void coreDoesNotDependOnZk() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("org.zkoss.zk.ui.Component"));
    }

    @Test
    void registersTheSaasCrudPages() {
        var container = new ModuleContainer();
        container.installModule(new SaasModuleProvider().getModule());

        var module = container.getModuleById("saas");
        var accounts = assertInstanceOf(CrudPage.class, container.findPage("saas/accounts"));

        assertEquals(10, module.getDefaultPageGroup().getPages().size());
        assertEquals(Account.class, accounts.getEntityClass());
    }
}
