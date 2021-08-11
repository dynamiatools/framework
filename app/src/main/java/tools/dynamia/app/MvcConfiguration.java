/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import tools.dynamia.app.template.ChainableUrlBasedViewResolver;
import tools.dynamia.app.template.TemplateResourceHandler;
import tools.dynamia.app.template.TemplateViewResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configure ZK with Spring MVC
 *
 * @author Mario A. Serrano Leones
 */
@EnableWebMvc
public abstract class MvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private ApplicationInfo applicationInfo;

    @Autowired
    private TemplateResourceHandler handler;


    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("*.ico").addResourceLocations("/");
        registry.addResourceHandler("static/**").addResourceLocations("/static/");
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.defaultContentType(MediaType.APPLICATION_JSON);

    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new ByteArrayHttpMessageConverter());
        converters.add(new StringHttpMessageConverter());
        converters.add(new ResourceHttpMessageConverter(false));
        converters.add(new StringHttpMessageConverter());
        converters.add(new MappingJackson2HttpMessageConverter());
        converters.add(new MappingJackson2XmlHttpMessageConverter());
    }

    @Bean
    public SimpleUrlHandlerMapping templateResourcesMapping() {

        Map<String, Object> map = new HashMap<>();
        map.put("root/**", handler);
        map.put("css/**", handler);
        map.put("styles/**", handler);
        map.put("img/**", handler);
        map.put("images/**", handler);
        map.put("assets/**", handler);
        map.put("js/**", handler);
        map.put("fonts/**", handler);
        map.put("font/**", handler);
        map.put("plugins/**", handler);
        map.put("vendor/**", handler);
        map.put("vendors/**", handler);

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(map);

        return mapping;
    }

    @Bean
    public ViewResolver defaultViewResolver() {
        UrlBasedViewResolver vr = new ChainableUrlBasedViewResolver();
        vr.setOrder(0);
        vr.setPrefix("/WEB-INF/views/");
        vr.setCache(applicationInfo.isWebCacheEnabled());
        return vr;
    }

    @Bean
    public ViewResolver zkViewResolver() {
        UrlBasedViewResolver vr = new ChainableUrlBasedViewResolver();
        vr.setOrder(1);
        vr.setPrefix("/zkau/web/views/");
        vr.setSuffix(".zul");
        vr.setCache(applicationInfo.isWebCacheEnabled());
        return vr;
    }

    @Bean
    public ViewResolver themeZulViewResolver() {
        UrlBasedViewResolver vr = new ChainableUrlBasedViewResolver();
        vr.setOrder(2);
        vr.setPrefix("/zkau/web/templates/" + applicationInfo.getTemplate().toLowerCase() + "/views/");
        vr.setSuffix(".zul");
        vr.setCache(applicationInfo.isWebCacheEnabled());
        return vr;
    }

    /**
     * Resolve ZHTML templates
     *
     * @return ViewResolver
     */
    @Bean
    public ViewResolver templateViewResolver() {
        TemplateViewResolver vr = new TemplateViewResolver(applicationInfo);
        vr.setOrder(Integer.MAX_VALUE);
        vr.setCache(applicationInfo.isWebCacheEnabled());
        return vr;
    }

}
