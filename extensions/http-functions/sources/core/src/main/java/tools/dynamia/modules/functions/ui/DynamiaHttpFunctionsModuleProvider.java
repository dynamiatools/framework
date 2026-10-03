package tools.dynamia.modules.functions.ui;

import tools.dynamia.crud.CrudPage;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.modules.functions.domain.DynamiaHttpFunction;
import tools.dynamia.navigation.Module;
import tools.dynamia.navigation.ModuleProvider;
import tools.dynamia.navigation.PageGroup;

/**
 * Contributes the Http Functions back office pages to the existing {@code saas} module (see
 * {@code tools.dynamia.modules.saas.ui.SaasModuleProvider}), instead of registering a brand-new
 * top-level module, since functions are managed as part of the account/integration configuration.
 *
 * @author Mario A. Serrano Leones
 */
@Provider
public class DynamiaHttpFunctionsModuleProvider implements ModuleProvider {

    @Override
    public Module getModule() {
        Module saas = Module.getRef("saas");

        PageGroup group = new PageGroup("httpFunctions", "Http Functions");
        group.addPage(new CrudPage("httpFunctions", "Functions", DynamiaHttpFunction.class)
                .icon("bolt")
                .longName("Http Functions"));

        saas.addPageGroup(group);
        return saas;
    }
}
