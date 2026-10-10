package tools.dynamia.ui.testing.contract;

import tools.dynamia.ui.contract.NavigationPortContract;
import tools.dynamia.ui.contract.PortDriver;

/** The test platform adapter meets the Navigation contract. */
class TestPlatformNavigationPortContractTest extends NavigationPortContract {

    @Override
    protected PortDriver driver() {
        return new TestPlatformDriver();
    }
}
