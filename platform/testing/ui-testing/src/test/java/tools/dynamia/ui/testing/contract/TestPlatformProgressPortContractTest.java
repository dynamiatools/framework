package tools.dynamia.ui.testing.contract;

import tools.dynamia.ui.contract.ProgressPortContract;
import tools.dynamia.ui.contract.PortDriver;

/** The test platform adapter meets the Progress contract. */
class TestPlatformProgressPortContractTest extends ProgressPortContract {

    @Override
    protected PortDriver driver() {
        return new TestPlatformDriver();
    }
}
