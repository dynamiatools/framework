package tools.dynamia.modules.saas;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.integration.scheduling.SchedulerUtil;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The tenant bound with {@link AccountTenants} travels into async tasks started with {@link SchedulerUtil}.
 */
class AccountTenantContextCapturerTest {

    private static void registerCapturer() {
        var beans = new SimpleObjectContainer();
        beans.addObject(new AccountTenantContextCapturer());
        Containers.get().installObjectContainer(beans);
    }

    @AfterEach
    void cleanUp() {
        Containers.get().removeAllContainers();
    }

    @Test
    void anAsyncTaskSeesTheTenantBoundByTheCaller() throws Exception {
        registerCapturer();

        Long seen = AccountTenants.with(7L, () -> {
            try {
                return SchedulerUtil.runWithResult(AccountTenants::forcedTenantId).get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });

        assertEquals(7L, seen);
    }

    @Test
    void aCallerWithoutATenantStartsATaskWithoutATenant() throws Exception {
        registerCapturer();

        Long seen = SchedulerUtil.runWithResult(AccountTenants::forcedTenantId).get(5, TimeUnit.SECONDS);

        assertNull(seen);
    }

    @Test
    void theRootTenantIsCarriedToo() throws Exception {
        registerCapturer();

        Long seen = AccountTenants.withRoot(() -> {
            try {
                return SchedulerUtil.runWithResult(AccountTenants::forcedTenantId).get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });

        assertEquals(AccountTenants.ROOT_TENANT_ID, seen);
    }
}
