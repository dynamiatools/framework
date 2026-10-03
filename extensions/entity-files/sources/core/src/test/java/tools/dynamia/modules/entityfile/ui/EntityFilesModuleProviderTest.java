package tools.dynamia.modules.entityfile.ui;

import org.junit.jupiter.api.Test;
import tools.dynamia.crud.cfg.ConfigPage;
import tools.dynamia.navigation.ModuleContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EntityFilesModuleProviderTest {

    @Test
    void coreDoesNotDependOnZk() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("org.zkoss.zk.ui.Component"));
    }

    @Test
    void registersTheEntityFilesConfigPageInTheSystemModule() {
        var container = new ModuleContainer();
        container.installModule(new EntityFilesModuleProvider().getModule());

        var page = assertInstanceOf(ConfigPage.class, container.findPage("system/config/entityFile"));

        assertEquals("EntityFileCFG", page.getDescriptorId());
    }
}
