package tools.dynamia.ui.testing.contract;

import tools.dynamia.ui.contract.ChoicesPortContract;
import tools.dynamia.ui.contract.PortDriver;

/** The replay (remote client) adapter meets the Choices contract. */
class ReplayChoicesPortContractTest extends ChoicesPortContract {

    @Override
    protected PortDriver driver() {
        return new ReplayPortDriver(new tools.dynamia.crud.headless.HeadlessViewsContributor(), tools.dynamia.ui.testing.TestDescriptors.autoFields());
    }
}
