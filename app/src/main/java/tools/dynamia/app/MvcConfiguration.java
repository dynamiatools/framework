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
import org.springframework.core.Ordered;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.web.ChainableUrlBasedViewResolver;
import tools.dynamia.web.ClassPathViewResolver;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Default Global MVC Configuration for DynamiaTools applications.
 *
 * @author Mario A. Serrano Leones
 */
@EnableWebMvc
public class MvcConfiguration implements WebMvcConfigurer {

    private final LoggingService logger = new SLF4JLoggingService(getClass());

    @Bean
    public ApplicationTemplateResourceHandler templateResourceHandler(ApplicationInfo applicationInfo) {
        var handler = new ApplicationTemplateResourceHandler(applicationInfo);
        if (applicationInfo.isWebCacheEnabled()) {
            handler.setCacheControl(CacheControl.maxAge(Duration.of(31536000, ChronoUnit.SECONDS)));
        }
        return handler;
    }

    @Bean
    public SimpleUrlHandlerMapping templateResourcesMapping(ApplicationTemplateResourceHandler handler) {

        Map<String, Object> map = new HashMap<>();
        ApplicationTemplateResourceHandler.STATIC_PATHS.forEach(pattern -> map.put(pattern, handler));
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
        mapping.setOrder(1);

        return mapping;
    }

    /**
     * Resolve views located in /WEB-INF/views
     *
     * @param applicationInfo
     * @return
     */
    @Bean
    public ViewResolver webinfViewResolver(ApplicationInfo applicationInfo) {
        UrlBasedViewResolver vr = new ChainableUrlBasedViewResolver();
        vr.setOrder(getDefaultViewResolverOrder(applicationInfo));
        vr.setPrefix("/WEB-INF/views/");
        vr.setCache(applicationInfo.isWebCacheEnabled());
        return vr;
    }


    /**
     * Resolve HTML views located in /WEB-INF/views
     *
     * @param applicationInfo
     * @return
     */
    @Bean
    public ViewResolver webInfHtmlViewResolver(ApplicationInfo applicationInfo) {
        UrlBasedViewResolver vr = new ChainableUrlBasedViewResolver();
        vr.setOrder(getDefaultViewResolverOrder(applicationInfo) + 1);
        vr.setPrefix("/WEB-INF/views/");
        vr.setSuffix(".html");
        vr.setCache(applicationInfo.isWebCacheEnabled());
        return vr;
    }

    /**
     * Resolve HTML views located in classpath:/views
     *
     * @param applicationInfo
     * @return
     */
    @Bean
    public ViewResolver classpathHtmlViewResolver(ApplicationInfo applicationInfo) {
        var vr = new ClassPathViewResolver();
        vr.setOrder(getDefaultViewResolverOrder(applicationInfo) + 1);
        vr.setPrefix("views/");
        vr.setSuffix(".html");
        vr.setCache(applicationInfo.isWebCacheEnabled());
        return vr;
    }

    /**
     * Resolve ZUL views located in classpath:/web/views
     *
     * @param applicationInfo
     * @return
     */
    @Bean
    public ViewResolver classpathZulViewResolver(ApplicationInfo applicationInfo) {
        var vr = new ChainableUrlBasedViewResolver();
        vr.setOrder(getDefaultViewResolverOrder(applicationInfo) + 2);
        vr.setPrefix("/zkau/web/views/");
        vr.setSuffix(".zul");
        vr.setCache(applicationInfo.isWebCacheEnabled());
        return vr;
    }


    /**
     * Resolve Zul templates located in classpath:/web/views/templates/CURRENT_TEMPLATE/views
     *
     * @return ViewResolver
     */
    @Bean
    public ViewResolver templateViewResolver(ApplicationInfo applicationInfo) {
        ApplicationTemplateViewResolver vr = new ApplicationTemplateViewResolver(applicationInfo);
        vr.setOrder(getViewResolverOrder(applicationInfo, "templateViewResolverOrder", Ordered.LOWEST_PRECEDENCE));
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

    private int getDefaultViewResolverOrder(ApplicationInfo applicationInfo) {
        return getViewResolverOrder(applicationInfo, "defaultViewResolverOrder", 1);
    }

    protected void log(String message) {
        logger.info(message);
    }

    protected void log(String message, Throwable error) {
        logger.error(message, error);
    }
}
