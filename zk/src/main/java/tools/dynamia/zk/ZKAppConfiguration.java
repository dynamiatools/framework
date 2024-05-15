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
package tools.dynamia.zk;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextListener;
import org.zkoss.zk.au.http.DHtmlUpdateServlet;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.http.DHtmlLayoutServlet;
import org.zkoss.zk.ui.http.HttpSessionListener;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.ui.MessageDisplayer;
import tools.dynamia.zk.ui.MessageNotification;
import tools.dynamia.zk.util.ZKUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mario Serrano Leones
 */
@Configuration
public class ZKAppConfiguration {

    private final LoggingService logger = new SLF4JLoggingService(getClass());


    public static void updateSkin(String skin) {
        if (ZKUtil.isInEventListener()) {
            Executions.getCurrent().sendRedirect("/?skin=" + skin, true);
        }
    }


    @Bean
    @ConditionalOnMissingBean(MessageDisplayer.class)
    public MessageDisplayer messageDialog() {
        return new MessageNotification();
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
        logger.info("ZK DHTML Layout servlet registered");
        return reg;
    }

    @Bean
    public ServletRegistrationBean<DHtmlUpdateServlet> dHtmlUpdateServlet() {
        Map<String, String> params = new HashMap<>();
        params.put("update-uri", "/zkau/*");
        var reg = new ServletRegistrationBean<>(new DHtmlUpdateServlet(), "/zkau/*");
        reg.setLoadOnStartup(2);
        reg.setInitParameters(params);
        logger.info("ZK DHTML Update servlet registered");
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
