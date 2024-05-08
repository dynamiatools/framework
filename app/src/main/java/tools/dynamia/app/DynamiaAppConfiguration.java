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

/**
 * @author Mario A. Serrano Leones
 */
@ComponentScan(value = {"tools.dynamia", "com.dynamia", "com.dynamiasoluciones"})
@EnableConfigurationProperties(ApplicationConfigurationProperties.class)
@Import({RootAppConfiguration.class, MvcConfiguration.class})
public class DynamiaAppConfiguration {


    @Bean
    @ConditionalOnMissingBean(MessageService.class)
    public MessageService messageService() {
        return new SimpleMessageService();
    }

    @Bean
    @ConditionalOnMissingBean(CrudService.class)
    public CrudService noOpCrudService() {
        return new NoOpCrudService();
    }

    @Bean
    @ConditionalOnMissingBean(TemplateEngine.class)
    public TemplateEngine defaultTemplateEngine() {
        return new VelocityTemplateEngine();
    }

    @Bean
    @ConditionalOnMissingBean(SearchService.class)
    public SearchService defaultSearchService() {
        return new DefaultSearchService();
    }

    @Bean
    @ConditionalOnMissingBean(SearchResultProvider.class)
    public SearchResultProvider defaultSearchProvider() {
        return new NoOpSearchProvider();
    }

}
