package tools.dynamia.modules.saas;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.modules.saas.services.AccountService;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The account bound with {@link AccountTenants} is the current account, so it matches the tenant Hibernate filters by.
 */
class AccountContextTenantTest {

    @AfterEach
    void cleanUp() {
        Containers.get().removeAllContainers();
    }

    private AccountContext contextWithAccount(long id) {
        var account = new Account();
        account.setId(id);
        var service = (AccountService) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{AccountService.class},
                (proxy, method, args) -> "getAccountById".equals(method.getName()) && id == (Long) args[0] ? account : null);
        var beans = new SimpleObjectContainer();
        beans.addObject(service);
        Containers.get().installObjectContainer(beans);
        return new AccountContext(List.of(() -> null));
    }

    @Test
    void theBoundAccountIsTheCurrentAccount() {
        var context = contextWithAccount(7L);

        Long seen = AccountTenants.with(7L, () -> context.getAccount().getId());

        assertEquals(7L, seen);
    }

    @Test
    void withoutABoundAccountTheResolversDecide() {
        var context = contextWithAccount(7L);

        assertNull(context.getAccount());
    }

    @Test
    void theRootTenantIsNotAnAccount() {
        var context = contextWithAccount(7L);

        assertNull(AccountTenants.withRoot(context::getAccount));
    }
}
