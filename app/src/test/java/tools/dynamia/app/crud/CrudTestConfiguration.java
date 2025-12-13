package tools.dynamia.app.crud;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import tools.dynamia.app.controllers.CrudServiceRestController;
import tools.dynamia.domain.EntityUtilsProvider;
import tools.dynamia.domain.jpa.JpaCrudService;
import tools.dynamia.domain.jpa.JpaEntityUtilsProvider;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.ObjectContainer;
import tools.dynamia.integration.SpringObjectContainer;

import javax.sql.DataSource;

@Configuration
public class CrudTestConfiguration {

    @Bean
    public DataSource dataSource() {
        var ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl("jdbc:h2:mem:toolsdomain;MODE=MySQL;DB_CLOSE_DELAY=-1");
        ds.setUsername("sa");
        ds.setPassword("sa");
        return ds;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new JpaTransactionManager(entityManagerFactory());
    }

    @Bean
    public EntityManagerFactory entityManagerFactory() {
        var emf = new LocalContainerEntityManagerFactoryBean();
        emf.setPackagesToScan("tools.dynamia.domain.jpa");

        emf.setDataSource(dataSource());


        var hb = new HibernateJpaVendorAdapter();
        hb.setShowSql(true);
        hb.setGenerateDdl(true);
        hb.setDatabasePlatform("org.hibernate.dialect.H2Dialect");


        emf.setJpaVendorAdapter(hb);
        emf.afterPropertiesSet();
        return emf.getObject();
    }

    @Bean
    public CrudService crudService() {
        return new JpaCrudService(null);
    }

    @Bean
    public CrudServiceRestController crudServiceRestController(CrudService crudService) {
        return new CrudServiceRestController(crudService);
    }

    @Bean
    public EntityUtilsProvider entityUtilsProvider() {
        return new JpaEntityUtilsProvider();
    }

    @Bean
    public ObjectContainer objectContainer() {
        return new SpringObjectContainer();
    }


}
