package tools.dynamia.modules.saas;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.integration.scheduling.SchedulerUtil;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.dynamia.modules.saas.api.AccountServiceAPI;

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

    @Test
    void theTenantOfTheRequestIsCarriedToo() throws Exception {
        registerCapturer();
        var request = new MockHttpServletRequest();
        request.setAttribute(AccountServiceAPI.CURRENT_ACCOUNT_ID_ATTRIBUTE, 9L);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        try {
            Long seen = SchedulerUtil.runWithResult(AccountTenants::forcedTenantId).get(5, TimeUnit.SECONDS);

            assertEquals(9L, seen);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void theNoTenantSentinelIsNeverCarried() {
        registerCapturer();

        var binding = AccountTenants.with(AccountTenants.NO_TENANT_ID, () -> new AccountTenantContextCapturer().capture());

        assertNull(binding);
    }
}
