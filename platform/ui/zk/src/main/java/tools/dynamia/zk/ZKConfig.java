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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import tools.dynamia.web.ChainableUrlBasedViewResolver;
import tools.dynamia.zk.spring.ApplicationScope;
import tools.dynamia.zk.spring.DesktopScope;
import tools.dynamia.zk.spring.ExecutionScope;
import tools.dynamia.zk.spring.PageScope;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ZKConfig {

    @Bean
    public static CustomScopeConfigurer zkscopes() {
        CustomScopeConfigurer configurer = new CustomScopeConfigurer();
        Map<String, Object> scopes = new HashMap<>();
        scopes.put("zk-desktop", new DesktopScope());
        scopes.put("zk-page", new PageScope());
        scopes.put("zk-application", new ApplicationScope());
        scopes.put("zk-execution", new ExecutionScope());
        configurer.setScopes(scopes);
        return configurer;
    }

    @Bean
    public ViewResolver zkViewResolver() {
        UrlBasedViewResolver vr = new ChainableUrlBasedViewResolver();
        vr.setOrder(9);
        vr.setPrefix("/zkau/web/views/");
        vr.setSuffix(".zul");
        return vr;
    }

    @Bean
    public ViewResolver themeZulViewResolver(@Value("${dynamia.app.template}") String template) {
        if (template == null || template.isBlank()) {
            template = "default";
        }

        UrlBasedViewResolver vr = new ChainableUrlBasedViewResolver();
        vr.setOrder(100);
        vr.setPrefix("/zkau/web/templates/" + template.toLowerCase() + "/views/");
        vr.setSuffix(".zul");
        return vr;
    }


}
