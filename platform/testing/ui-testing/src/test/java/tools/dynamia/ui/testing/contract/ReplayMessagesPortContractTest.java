package tools.dynamia.ui.testing.contract;

import tools.dynamia.ui.contract.MessagesPortContract;
import tools.dynamia.ui.contract.PortDriver;

/** The replay (remote client) adapter meets the Messages contract. */
class ReplayMessagesPortContractTest extends MessagesPortContract {

    @Override
    protected PortDriver driver() {
        return new ReplayPortDriver(new tools.dynamia.crud.headless.HeadlessViewsContributor(), tools.dynamia.ui.testing.TestDescriptors.autoFields());
    }
}
