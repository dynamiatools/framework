package tools.dynamia.modules.reports.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.dynamia.commons.Callback;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.ReportGroup;
import tools.dynamia.modules.reports.core.services.ReportsService;
import tools.dynamia.modules.reports.core.services.impl.ReportsServiceImpl;
import tools.dynamia.modules.saas.api.AccountServiceAPI;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Cached report lists must never be shared between accounts.
 */
class ReportsServiceCacheTest {

    static final AtomicLong CURRENT_ACCOUNT = new AtomicLong(10);
    static CrudService crud;

    /**
     * The service with the CrudService replaced, so no database is needed.
     */
    static class TestReportsService extends ReportsServiceImpl {
        TestReportsService(AccountServiceAPI accounts) {
            super(accounts);
        }

        @Override
        protected CrudService crudService() {
            return crud;
        }
    }

    @Configuration
    @EnableCaching
    static class Config {
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("reports");
        }

        @Bean
        AccountServiceAPI accounts() {
            var accounts = mock(AccountServiceAPI.class);
            when(accounts.getSystemAccountId()).thenReturn(1L);
            when(accounts.getCurrentAccountId()).thenAnswer(inv -> CURRENT_ACCOUNT.get());
            when(accounts.withAccount(any(), any())).thenAnswer(inv -> ((Supplier<?>) inv.getArgument(1)).get());
            return accounts;
        }

        @Bean
        ReportsService reportsService(AccountServiceAPI accounts) {
            return new TestReportsService(accounts);
        }
    }

    private AnnotationConfigApplicationContext context;
    private ReportsService service;

    @BeforeEach
    void setUp() {
        crud = mock(CrudService.class);
        doAnswer(inv -> {
            ((Callback) inv.getArgument(0)).doSomething();
            return null;
        }).when(crud).executeWithinTransaction(any(Callback.class));
        // every query answers a report named after the account that is current when the query runs
        when(crud.find(eq(Report.class), any(QueryParameters.class))).thenAnswer(inv -> {
            var report = new Report();
            report.setName("report of account " + CURRENT_ACCOUNT.get());
            return List.of(report);
        });
        context = new AnnotationConfigApplicationContext(Config.class);
        service = context.getBean(ReportsService.class);
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void activeReportsAreCachedPerAccount() {
        CURRENT_ACCOUNT.set(10);
        assertEquals("report of account 10", service.findActives().get(0).getName());

        CURRENT_ACCOUNT.set(20);
        assertEquals("report of account 20", service.findActives().get(0).getName());

        CURRENT_ACCOUNT.set(10);
        assertEquals("report of account 10", service.findActives().get(0).getName());
        verify(crud, times(4)).find(eq(Report.class), any(QueryParameters.class)); // 2 accounts per miss; third call came from the cache
    }

    @Test
    void exportableReportsAreCachedPerAccountAndSystemFlag() {
        CURRENT_ACCOUNT.set(10);
        assertEquals("report of account 10", service.findExportableReports(false).get(0).getName());
        CURRENT_ACCOUNT.set(20);
        assertEquals("report of account 20", service.findExportableReports(false).get(0).getName());
        CURRENT_ACCOUNT.set(10);
        service.findExportableReports(true);
        service.findExportableReports(false);
        verify(crud, times(6)).find(eq(Report.class), any(QueryParameters.class)); // 2 accounts per miss
    }

    @Test
    void reportsOfAGroupAreCachedPerAccount() {
        var group = new ReportGroup();
        group.setId(5L);
        group.setName("sales");

        CURRENT_ACCOUNT.set(10);
        assertEquals("report of account 10", service.findActivesByGroup(group).get(0).getName());
        CURRENT_ACCOUNT.set(20);
        assertEquals("report of account 20", service.findActivesByGroup(group).get(0).getName());
    }

    @Test
    void groupQueryReadsTheSystemAndTheCurrentAccount() {
        var group = new ReportGroup();
        group.setId(5L);
        group.setName("sales");
        CURRENT_ACCOUNT.set(10);

        service.findActivesByGroup(group);

        var captor = org.mockito.ArgumentCaptor.forClass(QueryParameters.class);
        verify(crud, times(2)).find(eq(Report.class), captor.capture());
        assertEquals(List.of(1L, 10L), captor.getAllValues().stream().map(q -> q.get("accountId")).toList());
    }
}
