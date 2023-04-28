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
import tools.dynamia.app.template.ApplicationTemplate;
import tools.dynamia.app.template.ApplicationTemplates;
import tools.dynamia.app.template.TemplateContext;
import tools.dynamia.commons.LocaleProvider;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.commons.SystemLocaleProvider;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.services.ValidatorService;
import tools.dynamia.domain.services.impl.DefaultValidatorService;
import tools.dynamia.io.IOUtils;
import tools.dynamia.io.Resource;
import tools.dynamia.navigation.Module;
import tools.dynamia.navigation.ModuleProvider;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

@EnableAspectJAutoProxy
public class RootAppConfiguration {

    @Autowired
    private List<ApplicationTemplate> templates;

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

    @Bean
    @Primary
    public LoggingService defaultLoggingService() {
        return logger;
    }

    @Bean
    @Primary
    public ValidatorService defaultValidatorService() {
        return new DefaultValidatorService();
    }

    @Bean
    public GlobalSearchProvider pageGlobalSearchProvider() {
        try {
            return new PageGlobalSearchProvider();
        } catch (Throwable e) {
            return new NoOpGlobalSearchProvider();
        }
    }

    @Bean
    public ApplicationInfo applicationInfo() {
        try {
            logger.info("Initializing Application Info");
            ApplicationInfo applicationInfo = loadApplicationInfo();
            if (applicationInfo.getName() == null) {
                applicationInfo.setName(environment.getProperty("spring.application.name"));
            }
            if (applicationInfo.getName() == null || applicationInfo.getName().isEmpty()) {
                applicationInfo.setName("DynamiaTools App");
            }

            ApplicationTemplate template = ApplicationTemplates.findTemplate(applicationInfo.getTemplate(), templates);

            TemplateContext context = new TemplateContext(applicationInfo);
            template.init(context);

            return applicationInfo;
        } catch (IOException e) {
            logger.error("Error loading applicationInfo using Dummy: " + e.getLocalizedMessage(), e);
            return ApplicationInfo.dummy();
        }
    }

    @Bean
    @ConditionalOnMissingBean(ModuleProvider.class)
    public ModuleProvider emptyModuleProvider() {
        return () -> new Module(StringUtils.randomString(), "No modules registered");
    }

    @Bean
    public LocaleProvider systemLocaleProvider() {
        return new SystemLocaleProvider();
    }

}
