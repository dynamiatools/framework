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

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import tools.dynamia.app.reports.JasperReportCompiler;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.services.impl.NoOpCrudService;
import tools.dynamia.integration.ms.MessageService;
import tools.dynamia.integration.ms.SimpleMessageService;
import tools.dynamia.integration.search.DefaultSearchService;
import tools.dynamia.integration.search.NoOpSearchProvider;
import tools.dynamia.integration.search.SearchResultProvider;
import tools.dynamia.integration.search.SearchService;
import tools.dynamia.reports.ReportCompiler;
import tools.dynamia.templates.TemplateEngine;
import tools.dynamia.web.navigation.RestApiNavigationConfiguration;

/**
 * Base configuration class for DynamiaTools framework.
 * <p>
 * This configuration class serves as the foundation for DynamiaTools applications by:
 * <ul>
 *   <li>Enabling component scanning for DynamiaTools packages</li>
 *   <li>Enabling {@link ApplicationConfigurationProperties} for application configuration</li>
 *   <li>Importing REST API navigation configuration</li>
 *   <li>Providing default bean implementations for core services when not present</li>
 * </ul>
 * </p>
 *
 * <p>This class extends {@link RootAppConfiguration} which provides essential beans like
 * {@link ApplicationInfo}, {@link tools.dynamia.commons.LocaleProvider}, and {@link tools.dynamia.commons.TimeZoneProvider}.
 * </p>
 *
 * <p><b>Component Scanning:</b> This configuration scans the following base packages:
 * <ul>
 *   <li>tools.dynamia</li>
 *   <li>com.dynamia</li>
 *   <li>com.dynamiasoluciones</li>
 * </ul>
 * </p>
 *
 * <p><b>Configuration Properties:</b> Enables {@link ApplicationConfigurationProperties} which binds
 * properties prefixed with {@code dynamia.app} from application.properties or application.yml.
 * </p>
 *
 * <p><b>Usage:</b> This configuration is automatically imported when using {@link EnableDynamiaTools} annotation.
 * No manual import is required.
 * </p>
 *
 * @author Mario A. Serrano Leones
 * @see RootAppConfiguration
 * @see ApplicationConfigurationProperties
 * @see EnableDynamiaTools
 */
@ComponentScan(value = {"tools.dynamia", "com.dynamia", "com.dynamiasoluciones"})
@EnableConfigurationProperties(ApplicationConfigurationProperties.class)
@Import(RestApiNavigationConfiguration.class)
public class DynamiaBaseConfiguration extends RootAppConfiguration {

    /**
     * Provides a default {@link MessageService} implementation if none is registered.
     * <p>
     * This bean creates a {@link SimpleMessageService} for basic message handling.
     * Applications can override this by providing their own MessageService bean.
     * </p>
     *
     * @return the default MessageService implementation
     */
    @Bean
    @ConditionalOnMissingBean(MessageService.class)
    public MessageService messageService() {
        log("Registering default SimpleMessageService");
        return new SimpleMessageService();
    }

    /**
     * Provides a no-operation {@link CrudService} implementation if none is registered.
     * <p>
     * This bean creates a {@link NoOpCrudService} that does nothing. It's intended as a
     * placeholder when no real CRUD service is configured. Applications should provide
     * their own CrudService implementation for database operations.
     * </p>
     *
     * @return the no-op CrudService implementation
     */
    @Bean
    @ConditionalOnMissingBean(CrudService.class)
    public CrudService noOpCrudService() {
        log("No CrudService implementation found, registering NoOpCrudService");
        return new NoOpCrudService();
    }



    /**
     * Provides a default {@link SearchService} implementation if none is registered.
     * <p>
     * This bean creates a {@link DefaultSearchService} for search functionality.
     * Applications can override this by providing their own SearchService bean.
     * </p>
     *
     * @return the default SearchService implementation
     */
    @Bean
    @ConditionalOnMissingBean(SearchService.class)
    public SearchService defaultSearchService() {
        log("Registering default SearchService");
        return new DefaultSearchService();
    }

    /**
     * Provides a default {@link SearchResultProvider} implementation if none is registered.
     * <p>
     * This bean creates a {@link NoOpSearchProvider} that provides no search results.
     * Applications should provide their own SearchResultProvider implementation for actual search functionality.
     * </p>
     *
     * @return the default no-op SearchResultProvider implementation
     */
    @Bean
    @ConditionalOnMissingBean(SearchResultProvider.class)
    public SearchResultProvider defaultSearchProvider() {
        return new NoOpSearchProvider();
    }


    /**
     * Provides a default {@link ReportCompiler} implementation if none is registered.
     * <p>
     * This bean creates a {@link JasperReportCompiler} for compiling Jasper reports.
     * Applications can override this by providing their own ReportCompiler bean.
     * </p>
     *
     * @return the JasperReportCompiler implementation
     */
    @Bean
    @ConditionalOnMissingBean(ReportCompiler.class)
    @ConditionalOnClass(name = "net.sf.jasperreports.engine.JasperReport")
    public ReportCompiler reportCompiler() {
        log("JasperReports detected, registering JasperReportCompiler as ReportCompiler");
        return new JasperReportCompiler();
    }

   /**
     * Provides a default {@link TemplateEngine} implementation if none is registered.
     * <p>
     * This bean creates a {@link VelocityTemplateEngine} for template processing.
     * Applications can override this by providing their own TemplateEngine bean.
     * </p>
     *
     * @return the VelocityTemplateEngine implementation
     */
    @Bean
    @ConditionalOnMissingBean(TemplateEngine.class)
    public TemplateEngine templateEngine() {
        return new VelocityTemplateEngine();
    }



}
