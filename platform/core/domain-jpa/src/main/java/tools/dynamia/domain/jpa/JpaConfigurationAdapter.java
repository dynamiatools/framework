/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.domain.jpa;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException;
import org.springframework.jndi.JndiTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import tools.dynamia.commons.PropertiesContainer;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.Containers;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Mario A. Serrano Leones
 */
public class JpaConfigurationAdapter implements ApplicationContextAware {

    private final LoggingService logger = new SLF4JLoggingService(getClass());


    private final PropertiesContainer properties;

    private final Set<String> additionalPackagesToScan = new HashSet<>();

    private ApplicationContext applicationContext;

    public JpaConfigurationAdapter(PropertiesContainer properties) {
        this.properties = properties;
    }

    /**
     * Receives the Spring context that owns this configuration, used to find the application's
     * {@link CurrentTenantIdentifierResolver} bean while the entity manager factory is being created.
     *
     * @param applicationContext the application context
     * @throws BeansException never thrown here
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void addPackageToScan(String packageName) {
        additionalPackagesToScan.add(packageName);
    }

    /**
     * Create a new datasource using JndiObjectFactoryBean, if not JNDI resource
     * is found, try to create a Datasource using ApplicationInfo properties, if
     * not parameters found then try to create a MySQL Datasource using Amazon
     * Web Service System properties, if nothing works an exception is throw.
     * Override this method for custom datasource
     * <p>
     * If datasource is created using ApplicationInfo.properties file, this
     * should contains:
     * <p>
     * prop.jdbcDriverClassName=xxxx prop.jdbcUrl=xxxx prop.jdbcUsername=xxxx
     * prop.jdbcPassword=xxxx
     *
     * @return dataSource
     */
    @Bean(name = "dataSource")
    public DataSource dataSource() {
        DataSource dataSource = getDataSourceFromJndi();

        if (dataSource == null) {
            dataSource = getDataSourceFromApplicationInfo();
        }

        if (dataSource == null) {
            dataSource = getDataSourceFromSystemProperties();
        }

        if (dataSource == null) {
            dataSource = getDataSourceFromAWS();
        }

        if (dataSource == null) {
            throw new DataSourceLookupFailureException("Cannot create Datasource using JNDI, ApplicationInfo neather AWS system properties");
        }

        return dataSource;

    }

    private DataSource getDataSourceFromAWS() {
        try {
            logger.info("Trying to create a MySQL Datasource using AWS System properties");

            String dbName = System.getProperty("RDS_DB_NAME");

            if (properties.getProperty("awsDatabaseName") != null) {
                dbName = properties.getProperty("awsDatabasename");
            }

            String userName = System.getProperty("RDS_USERNAME");
            String password = System.getProperty("RDS_PASSWORD");
            String hostname = System.getProperty("RDS_HOSTNAME");
            String port = System.getProperty("RDS_PORT");
            String jdbcUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + dbName;

            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("com.mysql.jdbc.Driver");
            dataSource.setUrl(jdbcUrl);
            dataSource.setUsername(userName);
            dataSource.setPassword(password);
            dataSource.getConnection(); // test connection
            logger.info("AWS Datasource Created Succesfully");
            return dataSource;
        } catch (Exception ex) {
            logger.warn("Cannot create a MySQL DataSource using AWS System properties. Exception Message: " + ex.getClass() + ": "
                    + ex.getMessage());
            return null;
        }
    }

    private DataSource getDataSourceFromSystemProperties() {
        try {
            logger.info("Trying to create a Datasource using System properties");

            String userName = System.getProperty("DB_USERNAME");
            String password = System.getProperty("DB_PASSWORD");
            String jdbcUrl = System.getProperty("DB_URL");
            String driverClass = System.getProperty("DB_DRIVER");

            DriverManagerDataSource dataSource = null;
            if (driverClass != null && !driverClass.isEmpty() && jdbcUrl != null && !jdbcUrl.isEmpty()) {
                dataSource = new DriverManagerDataSource();
                dataSource.setDriverClassName(driverClass);
                dataSource.setUrl(jdbcUrl);
                if (userName != null && !userName.isEmpty()) {
                    dataSource.setUsername(userName);
                }
                if (password != null && !password.isEmpty()) {
                    dataSource.setPassword(password);
                }
                dataSource.getConnection(); // test connection
                logger.info("System Properties Datasource Created Succesfully");
            }
            return dataSource;

        } catch (Exception ex) {
            logger.warn("Cannot create a MySQL DataSource using AWS System properties. Exception Message: " + ex.getClass() + ": "
                    + ex.getMessage());
            return null;
        }
    }

    private DataSource getDataSourceFromApplicationInfo() {

        try {
            logger.info("Trying to create Datasource using ApplicationInfo properties");
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(properties.getProperty("jdbcDriverClassName"));
            dataSource.setUrl(properties.getProperty("jdbcUrl"));
            dataSource.setUsername(properties.getProperty("jdbcUsername"));
            dataSource.setPassword(properties.getProperty("jdbcPassword"));
            dataSource.getConnection(); // test connection
            logger.info("ApplicationInfo Datasource Created Succesfully");
            return dataSource;
        } catch (Exception ex) {
            logger.warn("Cannot create DataSource using ApplicationInfo properties. Exception Message: " + ex.getClass() + ": "
                    + ex.getMessage());
            return null;
        }
    }

    private DataSource getDataSourceFromJndi() {

        try {
            logger.info("Trying to lookup Datasource resource using JNDI " + jndiName());
            JndiTemplate tp = new JndiTemplate();
            DataSource dataSource = tp.lookup(jndiName(), DataSource.class);
            logger.info("JNDI Datasource " + dataSource + " found succesfully");
            return dataSource;
        } catch (NamingException ex) {
            logger.warn("Cannot create JNDI DataSource using " + jndiName() + ". Exception Message: " + ex.getClass() + ": "
                    + ex.getMessage());
            return null;
        }
    }

    /**
     * Return by default "jdbc/datasource" override for custom jndiname
     *
     */
    protected String jndiName() {
        return properties.getProperty("jdniName");
    }

    /**
     * Gets the jpa dialet. By default return
     * "org.hibernate.dialect.MySQL5InnoDBDialect"
     *
     * @return the jpa dialet
     */
    protected String jpaDialect() {
        return "org.hibernate.dialect.MySQL5InnoDBDialect";
    }

    /**
     * Create an HibernateJpaVendorAdapter and preconfigure with
     * MySQL5InnoDBDialect GenerateDDL and ShowSQL, override for custom
     * JpaVendorAdapter
     *
     * @return JpaVendorAdapter
     */
    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter va = new HibernateJpaVendorAdapter();
        va.setGenerateDdl(true);
        va.setShowSql(true);
        va.setDatabasePlatform(jpaDialect());
        va.getJpaPropertyMap().put("hibernate.hbm2ddl.auto", "update");
        configureJpaVendorAdapter(va);
        return va;
    }

    /**
     * Return a default package "com.dynamia"
     *
     */
    public String[] packagesToScan() {
        List<String> packages = new ArrayList<>();
        packages.add("com.dynamia");
        packages.add("tools.dynamia");
        packages.add("com.dynamiasoluciones.modules");
        String basePackage = properties.getProperty("basePackage");
        if (basePackage != null && !basePackage.isEmpty()) {
            packages.add(basePackage);
        }

        if (!additionalPackagesToScan.isEmpty()) {
            packages.addAll(additionalPackagesToScan);
        }

        return packages.toArray(new String[0]);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        String[] packages = packagesToScan();
        factory.setPackagesToScan(packages);
        factory.setDataSource(dataSource());
        factory.setJpaVendorAdapter(jpaVendorAdapter());
        registerTenantIdentifierResolver(factory);
        registerLazyLoadNoTrans(factory);
        configureEntityManagerFactory(factory);
        logger.info("Setting EntityManagerFactory. Datasource: " + factory.getDataSource().toString() + ".  Packages to Scan: " + Arrays.toString(packages));

        factory.afterPropertiesSet();


        return factory;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new JpaTransactionManager(entityManagerFactory().getObject());
    }

    /**
     * Configure entity manager factory.
     *
     * @param factory the factory
     */
    /**
     * Registers the application's {@link CurrentTenantIdentifierResolver} bean as Hibernate's tenant identifier
     * resolver ({@code hibernate.tenant_identifier_resolver}). Extensions that provide multi-tenancy (for example
     * SaaS) only have to publish the resolver as a bean. When there is none, {@link RootTenantIdentifierResolver} is
     * registered, so entities annotated with {@code @TenantId} also work in applications that are not
     * multi-tenant (a resolver is harmless when no entity uses {@code @TenantId}). It runs before
     * {@link #configureEntityManagerFactory(LocalContainerEntityManagerFactoryBean)}, so an application can still
     * override the property there.
     *
     * @param factory the entity manager factory being configured
     */
    protected void registerTenantIdentifierResolver(LocalContainerEntityManagerFactoryBean factory) {
        CurrentTenantIdentifierResolver<?> resolver = findTenantIdentifierResolver();
        if (resolver == null) {
            logger.warn("No CurrentTenantIdentifierResolver bean found: falling back to " + RootTenantIdentifierResolver.class.getName()
                    + ". Entities annotated with @TenantId will NOT be isolated by tenant.");
            resolver = new RootTenantIdentifierResolver();
        }
        factory.getJpaPropertyMap().put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, resolver);
        logger.info("Hibernate tenant identifier resolver: " + resolver.getClass().getName());
    }

    /**
     * Property that enables {@code hibernate.enable_lazy_load_no_trans}.
     */
    public static final String LAZY_LOAD_NO_TRANS_PROPERTY = "dynamia.app.lazy-load-no-trans";

    /**
     * Safety net for applications that run without Open Persistence In View: when
     * {@code dynamia.app.lazy-load-no-trans=true}, a lazy association touched on a detached entity is loaded in a
     * temporary session instead of throwing {@code LazyInitializationException} (Hibernate's
     * {@code enable_lazy_load_no_trans}). It is <strong>off by default</strong> because every association touched
     * this way opens its own session and connection (N+1 when walking collections), and it hides code that should
     * load what it needs with {@code @InitializeOnLoad} or {@code crudService.reload(entity, "paths")}. Use it as a
     * temporary bridge. Spring Boot applications can set
     * {@code spring.jpa.properties.hibernate.enable_lazy_load_no_trans} directly.
     *
     * @param factory the entity manager factory being configured
     */
    protected void registerLazyLoadNoTrans(LocalContainerEntityManagerFactoryBean factory) {
        boolean enabled = applicationContext != null
                && applicationContext.getEnvironment().getProperty(LAZY_LOAD_NO_TRANS_PROPERTY, Boolean.class, false);
        if (enabled) {
            factory.getJpaPropertyMap().put(AvailableSettings.ENABLE_LAZY_LOAD_NO_TRANS, true);
            logger.warn("Hibernate enable_lazy_load_no_trans is ON (" + LAZY_LOAD_NO_TRANS_PROPERTY + "): lazy associations of detached "
                    + "entities load in temporary sessions. Prefer @InitializeOnLoad / crudService.reload(entity, paths).");
        }
    }

    /**
     * Looks up the application's tenant resolver. The Spring context of this configuration is used first, because
     * {@link Containers} may not have a Spring container yet while the entity manager factory is created; the
     * {@link Containers} lookup is kept as a fallback.
     *
     * @return the resolver bean, or null when the application does not publish one
     */
    private CurrentTenantIdentifierResolver<?> findTenantIdentifierResolver() {
        if (applicationContext != null) {
            var resolver = applicationContext.getBeanProvider(CurrentTenantIdentifierResolver.class)
                    .orderedStream().findFirst().orElse(null);
            if (resolver != null) {
                return resolver;
            }
        }
        return Containers.get().findObject(CurrentTenantIdentifierResolver.class);
    }

    protected void configureEntityManagerFactory(LocalContainerEntityManagerFactoryBean factory) {

    }

    /**
     * Configure jpa vendor adapter.
     *
     * @param va the va
     */
    protected void configureJpaVendorAdapter(HibernateJpaVendorAdapter va) {

    }

    public PropertiesContainer getProperties() {
        return properties;
    }
}
