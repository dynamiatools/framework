package tools.dynamia.modules.saas.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.modules.saas.domain.Account;
import tools.dynamia.modules.saas.domain.AccountType;
import tools.dynamia.modules.saas.services.AccountService;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountApiControllerTest {

    private Long systemAccountId;
    private Long currentAccountId;
    private Account accountBySubdomain;
    private Account storedAccount;

    @SuppressWarnings("unchecked")
    private static <T> T fake(Class<T> type, java.util.function.BiFunction<String, Object[], Object> handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type},
                (proxy, method, args) -> handler.apply(method.getName(), args));
    }

    private AccountApiController controller() {
        var service = fake(AccountService.class, (name, args) ->
                name.equals("getAccount") && args[0] instanceof String ? accountBySubdomain : null);
        var api = fake(AccountServiceAPI.class, (name, args) -> switch (name) {
            case "getSystemAccountId" -> systemAccountId;
            case "getCurrentAccountId" -> currentAccountId;
            default -> null;
        });
        return new AccountApiController(service, api);
    }

    private static MockHttpServletRequest request(String host) {
        var request = new MockHttpServletRequest("GET", "/api/saas/account/u1");
        request.setServerName(host);
        return request;
    }

    @BeforeEach
    void setUp() {
        Containers.get().removeAllContainers();
        var crud = fake(CrudService.class, (name, args) -> name.equals("findSingle") ? storedAccount : null);
        var container = new SimpleObjectContainer();
        container.addObject(crud);
        Containers.get().installObjectContainer(container);
        storedAccount = null;
        accountBySubdomain = null;
        systemAccountId = 1L;
        currentAccountId = 2L;
    }

    @AfterEach
    void tearDown() {
        Containers.get().removeAllContainers();
    }

    @Test
    void isAuthorizedOnlyForTheSystemAccount() {
        currentAccountId = 1L;
        assertTrue(controller().isAuthorized(request("acme.example.com")));

        currentAccountId = 2L;
        assertFalse(controller().isAuthorized(request("acme.example.com")));
    }

    @Test
    void isAuthorizedIsFalseWhenNoSystemAccountIsConfigured() {
        systemAccountId = null;
        currentAccountId = null;

        assertFalse(controller().isAuthorized(request("acme.example.com")));
    }

    @Test
    void isSameAccountIsFalseWhenNoAccountMatchesTheSubdomain() {
        accountBySubdomain = null;

        assertFalse(controller().isSameAccount("u1", request("unknown.example.com")));
    }

    @Test
    void isSameAccountIsFalseWhenTheRequestHasNoSubdomain() {
        assertFalse(controller().isSameAccount("u1", request("localhost")));
    }

    @Test
    void isSameAccountComparesTheUuidOfTheSubdomainAccount() {
        accountBySubdomain = new Account();
        accountBySubdomain.setUuid("u1");

        assertTrue(controller().isSameAccount("u1", request("acme.example.com")));
        assertFalse(controller().isSameAccount("other", request("acme.example.com")));
    }

    @Test
    void getAccountIs401WhenNeitherSystemNorSameAccountEvenWithoutAnAccountForTheSubdomain() {
        accountBySubdomain = null;

        var response = controller().getAccount("u1", request("unknown.example.com"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    private static Account licensedAccount() {
        var account = new Account();
        account.setUuid("u1");
        account.setType(new AccountType());
        account.setRequiredInstanceUuid(true);
        account.setInstanceUuid("hw-1");
        return account;
    }

    @Test
    void getAccountIs400WhenTheInstanceUuidIsRequiredButMissing() {
        currentAccountId = 1L; // system account: authorized
        storedAccount = licensedAccount();

        var response = controller().getAccount("u1", request("acme.example.com"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getAccountFlagsAnInstanceUuidMismatchAsAnInvalidLicense() {
        currentAccountId = 1L;
        storedAccount = licensedAccount();
        var request = request("acme.example.com");
        request.setParameter("uuid", "hw-other");

        var response = controller().getAccount("u1", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Licencia invalida", response.getBody().getStatusDescription());
    }
}
