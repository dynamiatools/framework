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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.web.context.annotation.SessionScope;
import tools.dynamia.commons.*;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.services.ValidatorService;
import tools.dynamia.domain.services.impl.DefaultValidatorService;
import tools.dynamia.io.IOUtils;
import tools.dynamia.io.Resource;
import tools.dynamia.navigation.Module;
import tools.dynamia.navigation.ModuleProvider;
import tools.dynamia.templates.ApplicationTemplate;
import tools.dynamia.templates.ApplicationTemplates;
import tools.dynamia.web.pwa.PWAIcon;
import tools.dynamia.web.pwa.PWAManifest;

import java.io.IOException;
import java.util.List;
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
    private List<ApplicationTemplate> templates;

    @Autowired
    private Environment environment;

    @Autowired(required = false)
    private ApplicationConfigurationProperties appCfgProps;

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
    @Primary
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
     *
     * @return the ApplicationInfo instance for the application
     */
    @Bean
    public ApplicationInfo applicationInfo() {
        try {
            logger.info("Initializing Application Info");
            ApplicationInfo applicationInfo = null;

            if (appCfgProps != null) {
                applicationInfo = ApplicationInfo.load(appCfgProps);
            } else {
                applicationInfo = loadApplicationInfo();
            }

            if (applicationInfo.getName() == null) {
                applicationInfo.setName(environment.getProperty("spring.application.name"));
            }
            if (applicationInfo.getName() == null || applicationInfo.getName().isEmpty()) {
                applicationInfo.setName("DynamiaTools App");
            }

            ApplicationTemplate template = ApplicationTemplates.findTemplate(applicationInfo.getTemplate(), templates);
            template.init();

            logger.info("Application Info Loaded: " + applicationInfo);
            return applicationInfo;
        } catch (IOException e) {
            logger.error("Error loading applicationInfo using Dummy: " + e.getLocalizedMessage(), e);
            return ApplicationInfo.dummy();
        }
    }

    /**
     * Provides an empty {@link ModuleProvider} bean if none is registered.
     *
     * @return a ModuleProvider that returns a dummy module with a random name and message
     */
    @Bean
    @ConditionalOnMissingBean(ModuleProvider.class)
    public ModuleProvider emptyModuleProvider() {
        return () -> new Module(StringUtils.randomString(), "No modules registered");
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

    /**
     * Provides a session-scoped {@link UserInfo} bean if none is registered.
     *
     * @return a new UserInfo instance for the session
     */
    @Bean("userInfo")
    @SessionScope
    @ConditionalOnMissingBean(UserInfo.class)
    public UserInfo userInfo() {
        return new UserInfo();
    }


    @Bean
    @ConditionalOnMissingBean(PWAManifest.class)
    public PWAManifest defaultManifest(ApplicationInfo applicationInfo) {
        return PWAManifest.builder()
                .name(applicationInfo.getName())
                .shortName(applicationInfo.getShortName())
                .startUrl("/")
                .display("standalone")
                .description(applicationInfo.getDescription())
                .addIcon(PWAIcon.builder()
                        .src(applicationInfo.getDefaultIcon())
                        .build()
                )
                .build();
    }

}
