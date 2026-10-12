package tools.dynamia.demos.movies;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import tools.dynamia.app.Ehcache3CacheManager;
import tools.dynamia.app.EnableDynamiaTools;
import tools.dynamia.navigation.DefaultPageProvider;

/**
 * Dynamia Movies: a movie catalog with a Vue backoffice (Dynamical Vue theme) and a public site
 * that reads the automatic REST API of DynamiaTools.
 */
@SpringBootApplication
@EnableDynamiaTools
@EnableCaching
@EntityScan({"tools.dynamia.demos.movies", "tools.dynamia"})
public class MoviesApplication {

    static void main(String[] args) {
        SpringApplication.run(MoviesApplication.class, args);
    }

    @Bean
    CacheManager cacheManager() {
        return new Ehcache3CacheManager();
    }

    @Bean
    DefaultPageProvider defaultPageProvider() {
        return () -> "insights/dashboard";
    }
}
