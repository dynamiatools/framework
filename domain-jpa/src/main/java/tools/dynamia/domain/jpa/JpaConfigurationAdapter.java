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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
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
import tools.dynamia.domain.services.CrudService;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.*;

/**
 * @author Mario A. Serrano Leones
 */
public class JpaConfigurationAdapter {

    private final LoggingService logger = new SLF4JLoggingService(getClass());


    private final PropertiesContainer properties;

    private final Set<String> additionalPackagesToScan = new HashSet<>();

    public JpaConfigurationAdapter(PropertiesContainer properties) {
        this.properties = properties;
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
            ex.printStackTrace();
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
            ex.printStackTrace();
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
            ex.printStackTrace();
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
            ex.printStackTrace();
            logger.warn("Cannot create JNDI DataSource using " + jndiName() + ". Exception Message: " + ex.getClass() + ": "
                    + ex.getMessage());
            return null;
        }
    }

    /**
     * Return by default "jdbc/datasource" override for custom jndiname
     *
     * @return
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
     * @return
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
