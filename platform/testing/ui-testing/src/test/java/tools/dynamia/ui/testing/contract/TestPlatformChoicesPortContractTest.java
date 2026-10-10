package tools.dynamia.ui.testing.contract;

import tools.dynamia.ui.contract.ChoicesPortContract;
import tools.dynamia.ui.contract.PortDriver;

/** The test platform adapter meets the Choices contract. */
class TestPlatformChoicesPortContractTest extends ChoicesPortContract {

    @Override
    protected PortDriver driver() {
        return new TestPlatformDriver();
    }
}
