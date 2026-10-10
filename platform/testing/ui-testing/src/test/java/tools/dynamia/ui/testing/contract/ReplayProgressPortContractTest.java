package tools.dynamia.ui.testing.contract;

import tools.dynamia.ui.contract.ProgressPortContract;
import tools.dynamia.ui.contract.PortDriver;

/** The replay (remote client) adapter meets the Progress contract. */
class ReplayProgressPortContractTest extends ProgressPortContract {

    @Override
    protected PortDriver driver() {
        return new ReplayPortDriver(new tools.dynamia.crud.headless.HeadlessViewsContributor(), tools.dynamia.ui.testing.TestDescriptors.autoFields());
    }
}
