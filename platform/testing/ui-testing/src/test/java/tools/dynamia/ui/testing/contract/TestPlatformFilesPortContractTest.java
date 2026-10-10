package tools.dynamia.ui.testing.contract;

import tools.dynamia.ui.contract.FilesPortContract;
import tools.dynamia.ui.contract.PortDriver;

/** The test platform adapter meets the Files contract. */
class TestPlatformFilesPortContractTest extends FilesPortContract {

    @Override
    protected PortDriver driver() {
        return new TestPlatformDriver();
    }
}
