package tools.dynamia.ui.testing.contract;

import tools.dynamia.ui.contract.MessagesPortContract;
import tools.dynamia.ui.contract.PortDriver;

/** The test platform adapter meets the Messages contract. */
class TestPlatformMessagesPortContractTest extends MessagesPortContract {

    @Override
    protected PortDriver driver() {
        return new TestPlatformDriver();
    }
}
