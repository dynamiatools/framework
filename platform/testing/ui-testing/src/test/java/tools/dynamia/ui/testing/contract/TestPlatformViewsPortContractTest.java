package tools.dynamia.ui.testing.contract;

import tools.dynamia.ui.contract.ViewsPortContract;
import tools.dynamia.ui.contract.PortDriver;

/** The test platform adapter meets the Views contract. */
class TestPlatformViewsPortContractTest extends ViewsPortContract {

    @Override
    protected PortDriver driver() {
        return new TestPlatformDriver();
    }
}
