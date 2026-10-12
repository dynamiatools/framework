package tools.dynamia.modules.reports.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.dynamia.commons.Callback;
import tools.dynamia.domain.query.AbstractQueryCondition;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.ReportGroup;
import tools.dynamia.modules.reports.core.services.impl.ReportsServiceImpl;
import tools.dynamia.modules.saas.api.AccountServiceAPI;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;

/**
 * {@code Report.accountId} is a Hibernate {@code @TenantId}: a query only sees the rows of the tenant that was bound
 * when the session opened. The reports of the system account must therefore be read as the system account, in their own
 * session, and added to the ones of the current account. The fake {@link CrudService} below behaves like that filter.
 */
class ReportsServiceTenantTest {

    static final Long SYSTEM = 1L;
    static final Long CURRENT = 10L;

    /** Tenant bound by {@code withAccount}, null when none. */
    static Long bound;
    /** Tenant of the session that is open, fixed when the transaction starts. */
    static Long sessionTenant;
    static boolean inTransaction;

    List<Report> database;
    ReportsServiceImpl service;

    @BeforeEach
    void setUp() {
        bound = null;
        sessionTenant = null;
        inTransaction = false;
        database = List.of(
                report(10L, "own sales", SYSTEM_GROUP_OF(CURRENT), CURRENT, "own"),
                report(20L, "system sales", SYSTEM_GROUP_OF(SYSTEM), SYSTEM, "shared"),
                report(30L, "system stock", SYSTEM_GROUP_OF(SYSTEM), SYSTEM, "stock"));

        var accounts = mock(AccountServiceAPI.class);
        when(accounts.getSystemAccountId()).thenReturn(SYSTEM);
        when(accounts.getCurrentAccountId()).thenReturn(CURRENT);
        when(accounts.withAccount(any(), any())).thenAnswer(inv -> {
            Long previous = bound;
            bound = inv.getArgument(0);
            try {
                return ((Supplier<?>) inv.getArgument(1)).get();
            } finally {
                bound = previous;
            }
        });

        var crud = mock(CrudService.class);
        doAnswer(inv -> {
            sessionTenant = bound != null ? bound : CURRENT; // the resolver: forced tenant, else the current account
            inTransaction = true;
            try {
                ((Callback) inv.getArgument(0)).doSomething();
            } finally {
                inTransaction = false;
            }
            return null;
        }).when(crud).executeWithinTransaction(any(Callback.class));
        when(crud.find(eq(Report.class), any(QueryParameters.class))).thenAnswer(inv -> visible());
        when(crud.findSingle(eq(Report.class), any(QueryParameters.class))).thenAnswer(inv -> {
            QueryParameters params = inv.getArgument(1);
            return visible().stream()
                    .filter(r -> matches(params.get("id"), r.getId()))
                    .filter(r -> matches(params.get("endpointName"), r.getEndpointName()))
                    .findFirst().orElse(null);
        });

        service = new ReportsServiceImpl(accounts) {
            @Override
            protected CrudService crudService() {
                return crud;
            }
        };
    }

    private static boolean matches(Object wanted, Object actual) {
        if (wanted instanceof AbstractQueryCondition<?> condition) {
            wanted = condition.getValue();
        }
        return wanted == null || wanted.equals(actual);
    }

    /** What Hibernate would return: only the rows of the tenant of the session, and only inside a session. */
    private List<Report> visible() {
        Long tenant = inTransaction ? sessionTenant : CURRENT;
        return database.stream().filter(r -> tenant.equals(r.getAccountId())).toList();
    }

    @Test
    void activeReportsIncludeTheOnesOfTheSystemAccount() {
        var names = service.findActives().stream().map(Report::getName).collect(Collectors.toSet());

        assertEquals(Set.of("own sales", "system sales", "system stock"), names);
    }

    @Test
    void activeReportsOfAGroupIncludeTheOnesOfTheSystemAccount() {
        var group = new ReportGroup();
        group.setId(1L);
        group.setName("sales");

        assertEquals(3, service.findActivesByGroup(group).size());
    }

    @Test
    void exportableReportsIncludeTheOnesOfTheSystemAccount() {
        assertEquals(3, service.findExportableReports(true).size());
    }

    @Test
    void aSystemReportIsLoadedById() {
        assertEquals("system sales", service.loadReportModel(20L).getName());
    }

    @Test
    void aSystemReportIsFoundByEndpoint() {
        assertEquals("system sales", service.findByEndpoint("shared").getName());
        assertEquals("system stock", service.findByEndpoint("sales", "stock").getName());
    }

    private static ReportGroup SYSTEM_GROUP_OF(Long account) {
        var group = new ReportGroup();
        group.setName("sales");
        group.setEndpointName("sales");
        group.setAccountId(account);
        return group;
    }

    private static Report report(Long id, String name, ReportGroup group, Long account, String endpoint) {
        var report = new Report();
        report.setId(id);
        report.setName(name);
        report.setGroup(group);
        report.setAccountId(account);
        report.setEndpointName(endpoint);
        return report;
    }
}
