package tools.dynamia.ui.testing.contract;

import tools.dynamia.ui.contract.FilesPortContract;
import tools.dynamia.ui.contract.PortDriver;

/** The replay (remote client) adapter meets the Files contract. */
class ReplayFilesPortContractTest extends FilesPortContract {

    @Override
    protected PortDriver driver() {
        return new ReplayPortDriver(new tools.dynamia.crud.headless.HeadlessViewsContributor(), tools.dynamia.ui.testing.TestDescriptors.autoFields());
    }
}
