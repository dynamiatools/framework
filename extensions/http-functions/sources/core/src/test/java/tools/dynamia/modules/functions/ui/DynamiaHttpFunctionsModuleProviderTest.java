package tools.dynamia.modules.functions.ui;

import org.junit.jupiter.api.Test;
import tools.dynamia.crud.CrudPage;
import tools.dynamia.modules.functions.domain.DynamiaHttpFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DynamiaHttpFunctionsModuleProviderTest {

    @Test
    void coreDoesNotDependOnZk() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("org.zkoss.zk.ui.Component"));
    }

    @Test
    void addsTheFunctionsCrudPageToTheSaasModule() {
        var module = new DynamiaHttpFunctionsModuleProvider().getModule();

        var page = assertInstanceOf(CrudPage.class, module.getPageGroupById("httpFunctions").getPageById("httpFunctions"));

        assertEquals("saas", module.getId());
        assertEquals(DynamiaHttpFunction.class, page.getEntityClass());
    }
}
