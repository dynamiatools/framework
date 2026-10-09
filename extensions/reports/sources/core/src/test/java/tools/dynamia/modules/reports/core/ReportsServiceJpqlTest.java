package tools.dynamia.modules.reports.core;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.modules.reports.core.domain.enums.DataType;
import tools.dynamia.modules.reports.core.services.impl.ReportsServiceImpl;
import tools.dynamia.modules.reports.core.testentities.TestCustomer;
import tools.dynamia.modules.saas.api.AccountServiceAPI;

import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportsServiceJpqlTest {

    private static LocalContainerEntityManagerFactoryBean factoryBean;
    private static EntityManagerFactory emf;
    private ReportsServiceImpl service;
    private final ReportsSettings settings = new ReportsSettings();

    @BeforeAll
    static void startJpa() {
        var ds = new org.h2.jdbcx.JdbcDataSource();
        ds.setURL("jdbc:h2:mem:jpql" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(ds);
        factoryBean.setPackagesToScan(TestCustomer.class.getPackageName());
        factoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        var props = new Properties();
        props.put("hibernate.hbm2ddl.auto", "create");
        props.put("jakarta.persistence.validation.mode", "none");
        props.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        factoryBean.setJpaProperties(props);
        factoryBean.afterPropertiesSet();
        emf = factoryBean.getObject();

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(new TestCustomer(1L, "Ana", "Bogota", new BigDecimal("10.5")));
        em.persist(new TestCustomer(2L, "Beto", "Cali", new BigDecimal("20")));
        em.persist(new TestCustomer(3L, "Carla", "Bogota", new BigDecimal("30")));
        em.getTransaction().commit();
        em.close();
    }

    @AfterAll
    static void stopJpa() {
        factoryBean.destroy();
    }

    private ReportsServiceImpl service() {
        var accounts = mock(AccountServiceAPI.class);
        var s = new ReportsServiceImpl(accounts);
        s.setSettings(settings);
        return s;
    }

    private static ReportDataSource datasource() {
        return new ReportDataSource("jpa", emf);
    }

    private static String entity() {
        return TestCustomer.class.getName();
    }

    @Test
    void scalarSelectsUseResultColumn() {
        var report = TestDb.sqlReport("names", "select c.name from " + entity() + " c order by c.name");
        report.setQueryLang("jpql");

        var data = service().execute(report, new ReportFilters(), datasource());

        assertEquals(List.of("Result"), data.getFieldNames());
        assertEquals(3, data.getSize());
    }

    @Test
    void multiColumnSelectsGetColumnNamesFromAliasesAndPaths() {
        var report = TestDb.sqlReport("multi", "select c.name, c.city as town, count(c) total from " + entity() + " c group by c.name, c.city order by c.name");
        report.setQueryLang("jpql");

        var data = service().execute(report, new ReportFilters(), datasource());

        assertEquals(List.of("name", "town", "total"), data.getFieldNames());
        var first = data.getEntries().get(0).getValues();
        assertEquals("Ana", first.get("name"));
        assertEquals("Bogota", first.get("town"));
        assertEquals(1L, first.get("total"));
    }

    @Test
    void filtersAreAppliedAsNamedParameters() {
        var report = TestDb.sqlReport("by city", "select c.name, c.city from " + entity() + " c order by c.name");
        report.setQueryLang("jpql");
        var filter = TestDb.filter(report, "city", "c.city = :city", DataType.TEXT);
        var filters = new ReportFilters();
        filters.add(filter, "Bogota");

        var data = service().execute(report, filters, datasource());

        assertEquals(2, data.getSize());
    }

    @Test
    void parametersNotDeclaredInTheQueryAreIgnored() {
        var report = TestDb.sqlReport("extra", "select c.name from " + entity() + " c");
        report.setQueryLang("jpql");
        var filters = new ReportFilters();
        filters.add(TestDb.filter(report, "unused", null, DataType.TEXT), "x");

        assertEquals(3, service().execute(report, filters, datasource()).getSize());
    }

    @Test
    void truncatesAtMaxRows() {
        settings.setMaxRows(2);
        var report = TestDb.sqlReport("limited", "select c.name from " + entity() + " c order by c.name");
        report.setQueryLang("jpql");

        var data = service().execute(report, new ReportFilters(), datasource());

        assertEquals(2, data.getSize());
        assertTrue(data.isTruncated());
    }

    @Test
    void rejectsJpqlThatModifiesData() {
        var report = TestDb.sqlReport("evil", "delete from " + entity());
        report.setQueryLang("jpql");

        assertThrows(ValidationError.class, () -> service().execute(report, new ReportFilters(), datasource()));
    }

    @Test
    void closesTheEntityManagerItCreatesFromTheFactory() {
        var factory = mock(EntityManagerFactory.class);
        var em = mock(EntityManager.class);
        var query = mock(jakarta.persistence.Query.class);
        when(factory.createEntityManager()).thenReturn(em);
        when(em.createQuery(anyString())).thenReturn(query);
        when(query.getParameters()).thenReturn(java.util.Set.of());
        when(query.getResultList()).thenReturn(List.of());
        var report = TestDb.sqlReport("close", "select 1 from X");
        report.setQueryLang("jpql");

        service().execute(report, new ReportFilters(), new ReportDataSource("f", factory));

        verify(em).close();
    }

    @Test
    void doesNotCloseAnEntityManagerOwnedByTheCaller() {
        var em = mock(EntityManager.class);
        var query = mock(jakarta.persistence.Query.class);
        when(em.createQuery(anyString())).thenReturn(query);
        when(query.getParameters()).thenReturn(java.util.Set.of());
        when(query.getResultList()).thenReturn(List.of());
        var report = TestDb.sqlReport("keep", "select 1 from X");
        report.setQueryLang("jpql");

        service().execute(report, new ReportFilters(), new ReportDataSource("e", em));

        verify(em, never()).close();
    }

    @Test
    void closesTheEntityManagerWhenTheQueryFails() {
        var factory = mock(EntityManagerFactory.class);
        var em = mock(EntityManager.class);
        when(factory.createEntityManager()).thenReturn(em);
        when(em.createQuery(anyString())).thenThrow(new IllegalArgumentException("bad jpql"));
        var report = TestDb.sqlReport("fail", "select 1 from X");
        report.setQueryLang("jpql");

        assertThrows(ReportsException.class, () -> service().execute(report, new ReportFilters(), new ReportDataSource("f", factory)));
        verify(em).close();
    }

    @Test
    void selectAliasesAreDerivedFromTheSelectClause() {
        assertEquals(List.of("name", "total", "col3"), ReportData.selectAliases("select distinct c.name, sum(c.balance) as total, coalesce(c.city, 'x') from C c", 3));
        assertEquals(List.of("col1", "col2"), ReportData.selectAliases("from Customer c", 2));
        assertEquals(List.of("a", "col2"), ReportData.selectAliases("select x.a, y.a from T", 2), "duplicated names get a positional name");
    }
}
