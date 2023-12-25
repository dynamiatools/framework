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
package tools.dynamia.zk.app;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.context.request.RequestContextListener;
import org.zkoss.zk.au.http.DHtmlUpdateServlet;
import org.zkoss.zk.ui.http.DHtmlLayoutServlet;
import org.zkoss.zk.ui.http.HttpSessionListener;
import tools.dynamia.app.DynamiaAppConfiguration;
import tools.dynamia.app.RootAppConfiguration;
import tools.dynamia.app.VelocityTemplateEngine;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.services.impl.NoOpCrudService;
import tools.dynamia.integration.ms.MessageService;
import tools.dynamia.integration.ms.SimpleMessageService;
import tools.dynamia.templates.TemplateEngine;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ing. Mario Serrano Leones
 */

@Import(RootAppConfiguration.class)
public class DynamiaToolsZKSpringBootApplication extends DynamiaAppConfiguration {


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

    /*
     * ZK servlets
     */
    @Bean
    public ServletRegistrationBean<DHtmlLayoutServlet> dHtmlLayoutServlet() {
        Map<String, String> params = new HashMap<>();
        params.put("update-uri", "/zkau");
        DHtmlLayoutServlet dHtmlLayoutServlet = new DHtmlLayoutServlet();
        var reg = new ServletRegistrationBean<>(dHtmlLayoutServlet, "*.zul", "*.zhtml");
        reg.setLoadOnStartup(1);
        reg.setInitParameters(params);
        log("ZK DHTML Layout servlet registered");
        return reg;
    }

    @Bean
    public ServletRegistrationBean<DHtmlUpdateServlet> dHtmlUpdateServlet() {
        Map<String, String> params = new HashMap<>();
        params.put("update-uri", "/zkau/*");
        var reg = new ServletRegistrationBean<>(new DHtmlUpdateServlet(), "/zkau/*");
        reg.setLoadOnStartup(2);
        reg.setInitParameters(params);
        log("ZK DHTML Update servlet registered");
        return reg;
    }

    @Bean
    public HttpSessionListener httpSessionListener() {
        return new HttpSessionListener();
    }

    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }

}
