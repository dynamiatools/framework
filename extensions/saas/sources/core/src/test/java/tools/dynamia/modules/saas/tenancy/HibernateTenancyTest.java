package tools.dynamia.modules.saas.tenancy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.PropertyValueException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.modules.saas.AccountTenantIdentifierResolver;
import tools.dynamia.modules.saas.AccountTenants;
import tools.dynamia.modules.saas.api.AccountServiceAPI;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Hibernate multi-tenancy over the SaaS base entities: the {@code accountId} of {@code SimpleEntitySaaS} is a
 * {@code @TenantId} resolved by {@link AccountTenantIdentifierResolver}. Uses the same wiring as the platform:
 * Spring ORM, a shared EntityManager and a JpaTransactionManager.
 */
class HibernateTenancyTest {

    private static EntityManagerFactory emf;
    private static EntityManager em;

    @BeforeAll
    static void start() {
        var factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(new DriverManagerDataSource("jdbc:h2:mem:tenancy;DB_CLOSE_DELAY=-1", "sa", ""));
        factory.setPackagesToScan("tools.dynamia.modules.saas.tenancy");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.getJpaPropertyMap().put("hibernate.hbm2ddl.auto", "create-drop");
        factory.getJpaPropertyMap().put("jakarta.persistence.validation.mode", "none");
        factory.getJpaPropertyMap().put("hibernate.tenant_identifier_resolver", new AccountTenantIdentifierResolver());
        factory.afterPropertiesSet();
        emf = factory.getObject();
        em = SharedEntityManagerCreator.createSharedEntityManager(emf);

        var container = new SimpleObjectContainer();
        container.addObject(new JpaTransactionManager(emf));
        Containers.get().installObjectContainer(container);
    }

    @AfterAll
    static void stop() {
        Containers.get().removeAllContainers();
        emf.close();
    }

    private static Long create(Long account, String text) {
        return AccountTenants.runAs(account, () -> {
            var note = new TenantNote(text);
            em.persist(note);
            return note.getId();
        });
    }

    private static List<String> texts(Long account) {
        return AccountTenants.runAs(account, () -> em.createQuery("from TenantNote", TenantNote.class)
                .getResultList().stream().map(TenantNote::getText).sorted().toList());
    }

    @Test
    void persistTakesTheAccountOfTheCurrentTenant() {
        var id = create(10L, "mine");

        var accountId = AccountTenants.runAs(10L, () -> em.find(TenantNote.class, id).getAccountId());

        assertEquals(10L, accountId);
    }

    @Test
    void queriesAndLoadsByIdAreIsolatedByAccount() {
        var idOfA = create(21L, "a-note");
        create(22L, "b-note");

        assertEquals(List.of("a-note"), texts(21L));
        assertEquals(List.of("b-note"), texts(22L));
        assertNull(AccountTenants.runAs(22L, () -> em.find(TenantNote.class, idOfA)));
    }

    @Test
    void bulkUpdatesOnlyTouchTheCurrentAccount() {
        create(31L, "x");
        create(32L, "x");

        int updated = AccountTenants.runAs(31L, () -> em.createQuery("update TenantNote set text = 'y'").executeUpdate());

        assertEquals(1, updated);
        assertEquals(List.of("x"), texts(32L));
    }

    @Test
    void anAccountCannotPersistDataForAnotherAccount() {
        assertThrows(PropertyValueException.class, () -> AccountTenants.runAs(41L, () -> {
            var note = new TenantNote("forged");
            note.setAccountId(42L);
            em.persist(note);
        }));
    }

    @Test
    void rootSeesEveryAccountAndMayPersistForAnyAccount() {
        AccountTenants.runAsRoot(() -> {
            var note = new TenantNote("created-by-initializer");
            note.setAccountId(51L);
            em.persist(note);
        });
        create(52L, "other");

        assertEquals(List.of("created-by-initializer"), texts(51L));
        var all = AccountTenants.runAsRoot(() -> em.createQuery("select n.text from TenantNote n", String.class).getResultList());
        assertTrue(all.containsAll(List.of("created-by-initializer", "other")));
    }

    @Test
    void runAsRestoresThePreviousTenantAndRejectsANullAccount() {
        var inner = AccountTenants.runAs(61L, () -> {
            AccountTenants.runAs(62L, () -> null);
            return new AccountTenantIdentifierResolver().resolveCurrentTenantIdentifier();
        });

        assertEquals(61L, inner);
        assertThrows(IllegalArgumentException.class, () -> AccountTenants.runAs((Long) null, () -> null));
    }

    @Test
    void withRootBindsRootToTheThreadForCodeThatManagesItsOwnSessions() throws Exception {
        create(81L, "w-a");
        create(82L, "w-b");

        List<String> seen = AccountTenants.withRoot(() -> {
            EntityManager own = emf.createEntityManager();
            try {
                return own.createQuery("select n.text from TenantNote n where n.text like 'w-%'", String.class).getResultList()
                        .stream().sorted().toList();
            } finally {
                own.close();
            }
        });
        // a worker thread does not inherit the binding: it has to bind again, as the migration export does
        var inWorker = new java.util.concurrent.atomic.AtomicReference<Long>();
        var thread = Thread.ofVirtual().start(() -> inWorker.set(AccountTenants.forcedTenantId()));
        thread.join();

        assertEquals(List.of("w-a", "w-b"), seen);
        assertNull(AccountTenants.forcedTenantId());
        assertNull(inWorker.get());
        assertEquals(AccountTenants.ROOT_TENANT_ID, AccountTenants.callWithRoot(AccountTenants::forcedTenantId));
    }

    @Test
    void theBindingIsUndoneWhenTheWorkFails() {
        assertThrows(IllegalStateException.class, () -> AccountTenants.with(91L, () -> {
            throw new IllegalStateException("boom");
        }));

        assertNull(AccountTenants.forcedTenantId());
    }

    @Test
    void resolverUsesTheRequestAttributeAndFallsBackToRoot() {
        var resolver = new AccountTenantIdentifierResolver();
        assertEquals(AccountTenants.ROOT_TENANT_ID, resolver.resolveCurrentTenantIdentifier());
        assertTrue(resolver.isRoot(AccountTenants.ROOT_TENANT_ID));

        var request = new MockHttpServletRequest();
        request.setAttribute(AccountServiceAPI.CURRENT_ACCOUNT_ID_ATTRIBUTE, 71L);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        try {
            assertEquals(71L, resolver.resolveCurrentTenantIdentifier());
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }
}
