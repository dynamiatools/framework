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
package tools.dynamia.app;

import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import tools.dynamia.commons.*;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.services.ValidatorService;
import tools.dynamia.domain.services.impl.DefaultValidatorService;
import tools.dynamia.io.IOUtils;
import tools.dynamia.io.Resource;

import java.io.IOException;
import java.util.Properties;

/**
 * RootAppConfiguration defines the main Spring Boot application configuration for DynamiaTools.
 * <p>
 * It provides beans for application info, module provider, locale provider, time zone provider, and user info.
 * Beans are conditionally created if missing, and some are marked as primary for dependency injection.
 *
 * @author Mario
 */
@EnableAspectJAutoProxy
public class RootAppConfiguration {

    @Autowired
    private Environment environment;


    private final LoggingService logger = new SLF4JLoggingService();

    public RootAppConfiguration() {
        logger.info("Starting Application Configuration ");
    }

    static ApplicationInfo loadApplicationInfo() throws IOException {

        Resource resource = IOUtils.getResource("classpath:META-INF/applicationInfo.properties");
        if (!resource.exists()) {
            resource = IOUtils.getResource("classpath:applicationInfo.properties");
        }
        if (!resource.exists()) {
            resource = IOUtils.getResource("./application.properties");
        }

        if (!resource.exists()) {
            resource = IOUtils.getResource("classpath:application.properties");
        }

        Properties pro = new Properties();
        if (resource.exists()) {
            pro.load(resource.getInputStream());
        }

        return ApplicationInfo.load(pro);
    }

    /**
     * Provides the primary {@link LoggingService} bean.
     *
     * @return the default LoggingService implementation
     */
    @Bean
    @ConditionalOnMissingBean(LoggingService.class)
    public LoggingService defaultLoggingService() {
        return logger;
    }

    /**
     * Provides the default {@link ValidatorService} bean if none is registered.
     *
     * @return the default ValidatorService implementation
     */
    @Bean
    @ConditionalOnMissingBean(ValidatorService.class)
    public ValidatorService defaultValidatorService() {
        return new DefaultValidatorService();
    }


    /**
     * Provides the primary {@link ApplicationInfo} bean.
     * <p>
     * This method creates an {@link ApplicationInfo} instance from the {@link ApplicationConfigurationProperties}
     * if available. If not, it attempts to load from applicationInfo.properties files.
     * Falls back to using spring.application.name property or a default name if no configuration is found.
     * </p>
     *
     * @param appCfgProps the application configuration properties (nullable, auto-injected by Spring)
     * @return the ApplicationInfo instance for the application
     */
    @Bean("applicationInfo")
    @Primary
    public ApplicationInfo applicationInfo(@Autowired(required = false) @Nullable ApplicationConfigurationProperties appCfgProps) {
        try {
            logger.info("Initializing Application Info");
            ApplicationInfo applicationInfo;

            if (appCfgProps != null) {
                logger.info("Loading ApplicationInfo from ApplicationConfigurationProperties");
                applicationInfo = ApplicationInfo.load(appCfgProps);
            } else {
                logger.info("ApplicationConfigurationProperties not available, loading from properties files");
                applicationInfo = loadApplicationInfo();
            }

            if (applicationInfo.getName() == null) {
                applicationInfo.setName(getEnvProperty("spring.application.name"));
            }
            if (applicationInfo.getName() == null || applicationInfo.getName().isEmpty()) {
                applicationInfo.setName("DynamiaTools App");
            }


            logger.info("Application Info Loaded: " + applicationInfo);
            return applicationInfo;
        } catch (IOException e) {
            logger.error("Error loading applicationInfo using Dummy: " + e.getLocalizedMessage(), e);
            return ApplicationInfo.dummy();
        }
    }



    /**
     * Provides the primary {@link LocaleProvider} bean using the system locale.
     *
     * @return a LocaleProvider instance using the system default locale
     */
    @Bean
    @Primary
    public LocaleProvider systemLocaleProvider() {
        return new SystemLocaleProvider();
    }

    /**
     * Provides the primary {@link TimeZoneProvider} bean using the system time zone.
     *
     * @return a TimeZoneProvider instance using the system default time zone
     */
    @Bean
    @Primary
    public TimeZoneProvider systemTimeZoneProvider() {
        return new SystemTimeZoneProvider();
    }



    public String getEnvProperty(String key) {
        if(environment!=null) {
            return environment.getProperty(key);
        }else{
            var property = System.getProperty(key);
            if(property==null){
                property=System.getenv(key);
            }
            return property;
        }
    }

    /**
     * Simple logging methods for the configuration class.
     */
    public void log(String message) {
        logger.info(message);
    }

    /**
     * Logs an error message with an associated throwable.
     *
     * @param message the error message to log
     * @param t       the throwable associated with the error
     */
    public void log(String message, Throwable t) {
        logger.error(message, t);
    }

}
