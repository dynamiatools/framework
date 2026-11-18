package tools.dynamia.modules.saas.listeners;

import org.junit.jupiter.api.Test;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.modules.saas.domain.AccountProfile;
import tools.dynamia.modules.saas.services.AccountService;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;

class AccountCrudListenerTest {

    private final AccountProfile defaultProfile = new AccountProfile();

    private final AccountCrudListener listener = new AccountCrudListener((AccountService) Proxy.newProxyInstance(
            AccountService.class.getClassLoader(), new Class<?>[]{AccountService.class},
            (proxy, method, args) -> "checkDefaultProfile".equals(method.getName()) ? defaultProfile : null));

    private static Account account(boolean autoInit, boolean remote) {
        var account = new Account();
        account.setAutoInit(autoInit);
        account.setRemote(remote);
        return account;
    }

    @Test
    void aNewAutoInitAccountWithoutProfileGetsTheDefaultOne() {
        var account = account(true, false);
        listener.beforeCreate(account);
        assertSame(defaultProfile, account.getProfile());
    }

    @Test
    void otherAccountsAreLeftAsTheyAre() {
        var noAutoInit = account(false, false);
        listener.beforeCreate(noAutoInit);
        assertNull(noAutoInit.getProfile());

        var remote = account(true, true);
        listener.beforeCreate(remote);
        assertNull(remote.getProfile());

        var own = account(true, false);
        var profile = new AccountProfile();
        own.setProfile(profile);
        listener.beforeCreate(own);
        assertSame(profile, own.getProfile());
    }
}
