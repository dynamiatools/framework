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

import org.springframework.context.annotation.Bean;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
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
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
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


    private final LoggingService logger = new SLF4JLoggingService(getClass());


    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("*.ico").addResourceLocations("/", "classpath:/static/");
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
    }

    @Bean
    public TemplateResourceHandler templateResourceHandler(ApplicationInfo applicationInfo) {
        var handler = new TemplateResourceHandler(applicationInfo);
        handler.setCacheControl(CacheControl.maxAge(Duration.of(31536000, ChronoUnit.SECONDS)));
        return handler;
    }

    @Bean
    public SimpleUrlHandlerMapping templateResourcesMapping(TemplateResourceHandler handler) {

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
        map.put("static/**", handler);

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(map);

        return mapping;
    }

    @Bean
    public ViewResolver webinfViewResolver(ApplicationInfo applicationInfo) {
        UrlBasedViewResolver vr = new ChainableUrlBasedViewResolver();
        vr.setOrder(getViewResolverOrder(applicationInfo, "defaultViewResolverOrder", 1));
        vr.setPrefix("/WEB-INF/views/");
        vr.setCache(applicationInfo.isWebCacheEnabled());
        return vr;
    }

    @Bean
    public ViewResolver zkViewResolver(ApplicationInfo applicationInfo) {
        UrlBasedViewResolver vr = new ChainableUrlBasedViewResolver();
        vr.setOrder(getViewResolverOrder(applicationInfo, "zkViewResolverOrder", 9));
        vr.setPrefix("/zkau/web/views/");
        vr.setSuffix(".zul");
        vr.setCache(applicationInfo.isWebCacheEnabled());
        return vr;
    }

    @Bean
    public ViewResolver themeZulViewResolver(ApplicationInfo applicationInfo) {
        UrlBasedViewResolver vr = new ChainableUrlBasedViewResolver();
        vr.setOrder(getViewResolverOrder(applicationInfo, "themeZulViewResolverOrder", 100));
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
    public ViewResolver templateViewResolver(ApplicationInfo applicationInfo) {
        TemplateViewResolver vr = new TemplateViewResolver(applicationInfo);
        vr.setOrder(getViewResolverOrder(applicationInfo, "templateViewResolverOrder", Integer.MAX_VALUE));
        vr.setCache(applicationInfo.isWebCacheEnabled());
        return vr;
    }

    protected int getViewResolverOrder(ApplicationInfo applicationInfo, String name, int defaultValue) {
        int order = defaultValue;
        try {
            if (name != null && applicationInfo != null && applicationInfo.getProperty(name) != null) {
                order = Integer.parseInt(applicationInfo.getProperty(name));
            }
        } catch (NumberFormatException e) {
            //invalid number, just ignore
        }
        return order;
    }

    protected void log(String message) {
        logger.info(message);
    }

    protected void log(String message, Throwable error) {
        logger.error(message, error);
    }
}
