package tools.dynamia.ui.testing.contract;

import tools.dynamia.ui.contract.ViewsPortContract;
import tools.dynamia.ui.contract.PortDriver;

/** The replay (remote client) adapter meets the Views contract. */
class ReplayViewsPortContractTest extends ViewsPortContract {

    @Override
    protected PortDriver driver() {
        return new ReplayPortDriver(new tools.dynamia.crud.headless.HeadlessViewsContributor(), tools.dynamia.ui.testing.TestDescriptors.autoFields());
    }
}
