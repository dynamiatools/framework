package tools.dynamia.ui.testing.contract;

import tools.dynamia.ui.contract.NavigationPortContract;
import tools.dynamia.ui.contract.PortDriver;

/** The replay (remote client) adapter meets the Navigation contract. */
class ReplayNavigationPortContractTest extends NavigationPortContract {

    @Override
    protected PortDriver driver() {
        return new ReplayPortDriver(new tools.dynamia.crud.headless.HeadlessViewsContributor(), tools.dynamia.ui.testing.TestDescriptors.autoFields());
    }
}
