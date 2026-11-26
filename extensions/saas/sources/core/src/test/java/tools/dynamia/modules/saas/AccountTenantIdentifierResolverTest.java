package tools.dynamia.modules.saas;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The root tenant is configurable ({@code dynamia.saas.root-tenant-id}); the resolver and {@link AccountTenants} must
 * always agree on it, because Hibernate only skips the tenant filter for the id the resolver reports as root.
 */
class AccountTenantIdentifierResolverTest {

    @AfterEach
    void restoreDefaultRoot() {
        new AccountTenantIdentifierResolver();
    }

    @Test
    void rootDefaultsToTheRootTenantConstant() {
        var resolver = new AccountTenantIdentifierResolver();

        assertTrue(resolver.isRoot(AccountTenants.ROOT_TENANT_ID));
        assertFalse(resolver.isRoot(1L));
        assertFalse(resolver.isRoot(AccountTenants.NO_TENANT_ID));
        assertFalse(resolver.isRoot(null));
        assertEquals(AccountTenants.ROOT_TENANT_ID, AccountTenants.rootTenantId());
    }

    @Test
    void aConfiguredRootReplacesTheDefaultOne() {
        var resolver = new AccountTenantIdentifierResolver(7L);

        assertTrue(resolver.isRoot(7L));
        assertFalse(resolver.isRoot(AccountTenants.ROOT_TENANT_ID));
        assertFalse(resolver.isRoot(AccountTenants.NO_TENANT_ID));
        assertEquals(7L, AccountTenants.rootTenantId());
    }

    @Test
    void withRootBindsTheConfiguredRoot() throws Exception {
        var resolver = new AccountTenantIdentifierResolver(7L);

        assertEquals(7L, AccountTenants.withRoot(AccountTenants::forcedTenantId));
        assertEquals(7L, AccountTenants.callWithRoot(AccountTenants::forcedTenantId));
        assertEquals(7L, AccountTenants.withRoot(resolver::resolveCurrentTenantIdentifier));
        assertTrue(resolver.isRoot(AccountTenants.withRoot(resolver::resolveCurrentTenantIdentifier)));
    }

    @Test
    void anAccountTenantIsNeverRootUnlessConfiguredAsRoot() {
        var resolver = new AccountTenantIdentifierResolver(7L);

        var tenant = AccountTenants.with(10L, resolver::resolveCurrentTenantIdentifier);

        assertEquals(10L, tenant);
        assertFalse(resolver.isRoot(tenant));
    }

    @Test
    void withoutATenantItStillFailsClosedWhateverTheRoot() {
        var resolver = new AccountTenantIdentifierResolver(7L);

        assertEquals(AccountTenants.NO_TENANT_ID, resolver.resolveCurrentTenantIdentifier());
    }

    @Test
    void theRootCannotBeEmptyOrTheNoTenantSentinel() {
        assertThrows(IllegalArgumentException.class, () -> new AccountTenantIdentifierResolver((Long) null));
        assertThrows(IllegalArgumentException.class, () -> new AccountTenantIdentifierResolver(AccountTenants.NO_TENANT_ID));
        assertEquals(AccountTenants.ROOT_TENANT_ID, AccountTenants.rootTenantId());
    }

    @Test
    void theDefaultPolicyIsClosedAndAnUnboundSessionSeesNothing() {
        var resolver = new AccountTenantIdentifierResolver();
        resolver.ready();

        assertEquals(NoTenantPolicy.CLOSED, resolver.getNoTenantPolicy());
        assertEquals(AccountTenants.NO_TENANT_ID, testapp.TenantlessCaller.run(resolver::resolveCurrentTenantIdentifier));
    }

    @Test
    void strictThrowsNamingTheCallSiteOnceTheContextIsUp() {
        var resolver = new AccountTenantIdentifierResolver(0L, "strict");
        resolver.ready();

        var e = assertThrows(NoTenantException.class,
                () -> testapp.TenantlessCaller.run(resolver::resolveCurrentTenantIdentifier));

        assertTrue(e.getMessage().contains("testapp.TenantlessCaller.run"), e.getMessage());
        assertTrue(e.getMessage().contains("no-tenant-policy=strict"), e.getMessage());
    }

    @Test
    void strictDoesNotThrowWhileTheApplicationIsStarting() {
        var resolver = new AccountTenantIdentifierResolver(0L, "strict");

        // Hibernate resolves a tenant while it starts: that must fail closed, never throw
        assertEquals(AccountTenants.NO_TENANT_ID, testapp.TenantlessCaller.run(resolver::resolveCurrentTenantIdentifier));
    }

    @Test
    void rootRunsAsRootOnceTheContextIsUp() {
        var resolver = new AccountTenantIdentifierResolver(7L, "root");

        assertEquals(AccountTenants.NO_TENANT_ID, testapp.TenantlessCaller.run(resolver::resolveCurrentTenantIdentifier));
        resolver.ready();
        var tenant = testapp.TenantlessCaller.run(resolver::resolveCurrentTenantIdentifier);

        assertEquals(7L, tenant);
        assertTrue(resolver.isRoot(tenant));
    }

    @Test
    void aBoundTenantIsNeverAffectedByThePolicy() {
        var resolver = new AccountTenantIdentifierResolver(0L, "strict");
        resolver.ready();

        assertEquals(10L, AccountTenants.with(10L, resolver::resolveCurrentTenantIdentifier));
    }

    @Test
    void thePolicyCanBeChangedAtRuntime() {
        var resolver = new AccountTenantIdentifierResolver(0L, "strict");
        resolver.ready();

        resolver.setNoTenantPolicy(NoTenantPolicy.CLOSED);

        assertEquals(AccountTenants.NO_TENANT_ID, testapp.TenantlessCaller.run(resolver::resolveCurrentTenantIdentifier));
    }

    @Test
    void anUnknownPolicyIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new AccountTenantIdentifierResolver(0L, "loose"));
        assertEquals(NoTenantPolicy.CLOSED, NoTenantPolicy.parse(null));
        assertEquals(NoTenantPolicy.STRICT, NoTenantPolicy.parse(" Strict "));
    }
}
