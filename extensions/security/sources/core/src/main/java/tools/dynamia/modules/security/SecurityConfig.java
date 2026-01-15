
/*
 * Copyright (c) 2009 - 2021 Dynamia Soluciones IT SAS  All Rights Reserved
 *
 * Todos los Derechos Reservados  2009 - 2021
 *
 * Este archivo es propiedad de Dynamia Soluciones IT NIT 900302344-1 en Colombia / Sur America,
 * esta estrictamente prohibida su copia o distribución sin previa autorización del propietario.
 * Puede contactarnos a info@dynamiasoluciones.com o visitar nuestro sitio web
 * https://www.dynamiasoluciones.com
 *
 * Autor: Ing. Mario Serrano Leones <mario@dynamiasoluciones.com>
 */

package tools.dynamia.modules.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.DefaultEntityReferenceRepository;
import tools.dynamia.domain.EntityReferenceRepository;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.ms.MessageService;
import tools.dynamia.modules.security.domain.Profile;
import tools.dynamia.modules.security.domain.User;
import tools.dynamia.modules.security.services.ProfileService;
import tools.dynamia.modules.security.services.SecurityService;
import tools.dynamia.modules.security.services.UserService;
import tools.dynamia.modules.security.services.impl.ProfileServiceImpl;
import tools.dynamia.modules.security.services.impl.SecurityServiceImpl;
import tools.dynamia.modules.security.services.impl.UserServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Mario Serrano Leones
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {


    private final List<IgnoringSecurityMatcher> ignorings;

    private final List<SecurityConfigurationInterceptor> configInterceptors;

    private final MessageService messageService;

    private LoggingService logger = new SLF4JLoggingService(SecurityConfig.class);


    public SecurityConfig(List<IgnoringSecurityMatcher> ignorings,
                          List<SecurityConfigurationInterceptor> configInterceptors,
                          MessageService messageService) {
        this.ignorings = ignorings;
        this.configInterceptors = configInterceptors;
        this.messageService = messageService;
    }

    @Bean
    public UserService userService(CrudService crudService) {
        return new UserServiceImpl(crudService);
    }

    @Bean
    public ProfileService profileService(CrudService crudService) {
        return new ProfileServiceImpl(crudService);
    }

    @Bean
    public SecurityService seguridadService(ProfileService profileService, CrudService crudService, PasswordEncoder passwordEncoder) {
        return new SecurityServiceImpl(profileService, crudService, passwordEncoder);
    }


    @Bean("seguridadAuthManager")
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder,
                                                       UserDetailsService userDetailService) throws Exception {

        var builder = http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailService)
                .passwordEncoder(passwordEncoder)
                .and();


        if (configInterceptors != null) {
            for (SecurityConfigurationInterceptor interceptor : configInterceptors) {
                interceptor.configure(builder);
            }
        }

        return builder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           SecurityService userDetailsService,
                                           AuthenticationManager authMgr,
                                           SavedRequestAwareAuthenticationSuccessHandler successHandler,
                                           HandlerMappingIntrospector introspector) throws Exception {

        http
                .formLogin(c -> c
                        .successHandler(successHandler)
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", false)
                        .loginPage("/login")
                        .permitAll()
                ).logout(c -> c
                        .logoutUrl("/logout")
                        .permitAll()
                ).csrf(AbstractHttpConfigurer::disable)
                .userDetailsService(userDetailsService)
                .requestCache(RequestCacheConfigurer::disable)
                .addFilter(new UserTokenAuthenticationFilter(authMgr));
        ;


        http.securityContext(c -> c.
                securityContextRepository(new HttpSessionSecurityContextRepository())
        );

        if (configInterceptors != null) {
            for (SecurityConfigurationInterceptor interceptor : configInterceptors) {
                interceptor.configure(http);
            }
        }

        configureIgnores(http, introspector);
        http.authorizeHttpRequests(c -> c.anyRequest().authenticated());
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> {
            web.httpFirewall(firewall());
            if (configInterceptors != null) {
                for (SecurityConfigurationInterceptor interceptor : configInterceptors) {
                    interceptor.configure(web);
                }
            }
        };
    }

    @Bean
    public HttpFirewall firewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowBackSlash(true);
        firewall.setAllowSemicolon(true);

        return firewall;
    }

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(5);
    }


    @Bean
    public SavedRequestAwareAuthenticationSuccessHandler savedRequestAwareAuthenticationSuccessHandler() {
        SavedRequestAwareAuthenticationSuccessHandler auth = new SavedRequestAwareAuthenticationSuccessHandler();
        auth.setTargetUrlParameter("targetUrl");
        return auth;
    }


    @Bean
    public EntityReferenceRepository<Long> usuariosEntityReferenceRepository() {
        DefaultEntityReferenceRepository<Long> repo = new DefaultEntityReferenceRepository<>(User.class, "username");
        repo.setCacheable(true);

        return repo;
    }

    @Bean
    public EntityReferenceRepository<Long> perfilUsuarioEntityReferenceRepository() {
        DefaultEntityReferenceRepository<Long> repo = new DefaultEntityReferenceRepository<>(Profile.class, "nombre");
        repo.setCacheable(true);

        return repo;
    }


    private void configureIgnores(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {
        if (ignorings != null) {
            for (IgnoringSecurityMatcher ism : ignorings) {
                logger.info("Permiting " + ism.getClass().getSimpleName() + " paths: " + Arrays.toString(ism.matchers()));
                var builder = new MvcRequestMatcher.Builder(introspector);
                RequestMatcher[] matchers = Stream.of(ism.matchers()).map(builder::pattern).toArray(RequestMatcher[]::new);
                http.authorizeHttpRequests(c -> c
                        .requestMatchers(matchers)
                        .permitAll());
            }
        }
    }

}
