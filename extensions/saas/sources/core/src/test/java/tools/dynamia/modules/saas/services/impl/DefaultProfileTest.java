package tools.dynamia.modules.saas.services.impl;

import org.junit.jupiter.api.Test;
import tools.dynamia.modules.saas.domain.AccountProfile;
import tools.dynamia.modules.saas.domain.AccountProfileRestriction;
import tools.dynamia.modules.saas.domain.enums.AccessControl;
import tools.dynamia.navigation.Module;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultProfileTest {

    private final List<Module> modules = List.of(new Module("books", "Books"), new Module("system", "System"), new Module("saas", "SaaS"));

    private static AccessControl access(AccountProfile profile, String module) {
        return profile.getRestrictions().stream().filter(r -> r.getValue().equals(module)).findFirst().orElseThrow().getAccessControl();
    }

    @Test
    void everyModuleIsAllowedExceptSaas() {
        var profile = new AccountProfile();

        assertTrue(AccountServiceImpl.addMissingModules(profile, modules));

        assertEquals(3, profile.getRestrictions().size());
        assertEquals(AccessControl.ALLOWED, access(profile, "books"));
        assertEquals(AccessControl.ALLOWED, access(profile, "system"));
        assertEquals(AccessControl.DENIED, access(profile, "saas"));
        assertTrue(profile.getRestrictions().stream().allMatch(r -> r.getProfile() == profile));
    }

    @Test
    void whatTheAdministratorDecidedIsKeptAndNothingIsDuplicated() {
        var profile = new AccountProfile();
        var books = new AccountProfileRestriction("Books", "ACCESS", "BOOKS"); // different case
        books.setAccessControl(AccessControl.DENIED);
        profile.getRestrictions().add(books);

        assertTrue(AccountServiceImpl.addMissingModules(profile, modules));
        assertEquals(3, profile.getRestrictions().size());
        assertEquals(AccessControl.DENIED, books.getAccessControl());

        assertFalse(AccountServiceImpl.addMissingModules(profile, modules), "a second run changes nothing");
        assertEquals(3, profile.getRestrictions().size());
    }
}
