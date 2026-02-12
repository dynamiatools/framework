package tools.dynamia.modules.security;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.ForwardedHeaderFilter;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.domain.AutoEvictEntityCacheCrudListener;
import tools.dynamia.domain.DefaultEntityReferenceRepository;
import tools.dynamia.domain.EntityReferenceRepository;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.ms.MessageService;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.modules.security.domain.Profile;
import tools.dynamia.modules.security.domain.User;
import tools.dynamia.modules.security.listeners.SpringSecurtyApplicationListener;
import tools.dynamia.modules.security.services.JWTService;
import tools.dynamia.modules.security.services.ProfileService;
import tools.dynamia.modules.security.services.SecurityService;
import tools.dynamia.modules.security.services.UserService;
import tools.dynamia.modules.security.services.impl.ProfileServiceImpl;
import tools.dynamia.modules.security.services.impl.SecurityServiceImpl;
import tools.dynamia.modules.security.services.impl.UserServiceImpl;
import tools.dynamia.web.util.HttpUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Central Spring Security configuration for the application.
 * <p>
 * Configures security filter chains for API (stateless), OAuth2, and web (stateful),
 * including authentication managers, password encoding, cookie helpers and utilities.
 *
 * @author Mario Serrano Leones
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@EnableConfigurationProperties(DynamiaSecurityConfigurationProperties.class)
@Order(1)
public class DynamiaSecurityConfig {

    public static final String CACHE_NAME = "security";
    public static final String CHANNEL_NAME = "DynamiaSecurity";
    public static final String TOPIC_USER_LOGIN = "userLogin";
    public static final String HEADER_USER_ROLES = "userRoles";
    public static final String HEADER_USER_ID = "userId";
    public static final String JWT_COOKIE_NAME = "DYNAMIA_JWT";
    private static Boolean productionEnv;

    static {
        AutoEvictEntityCacheCrudListener.register(CACHE_NAME, User.class, u -> List.of("User-" + u.getUsername() + "-" + u.getAccountId()));
    }


    private final List<tools.dynamia.modules.security.IgnoringSecurityMatcher> ignorings;

    private final List<SecurityConfigurationInterceptor> configInterceptors;

    private final MessageService messageService;

    private final static LoggingService logger = LoggingService.get(DynamiaSecurityConfig.class);


    public DynamiaSecurityConfig(List<tools.dynamia.modules.security.IgnoringSecurityMatcher> ignorings,
                                 List<SecurityConfigurationInterceptor> configInterceptors,
                                 MessageService messageService) {
        this.ignorings = ignorings;
        this.configInterceptors = configInterceptors;
        this.messageService = messageService;
        logger.info("Security configuration initialized.");

    }


    @Bean
    @Primary
    public SecurityService securityUserDetailsService(ProfileService profileService, CrudService crudService, PasswordEncoder passwordEncoder) {
        return new SecurityServiceImpl(profileService, crudService, passwordEncoder);
    }

    @Bean
    public ProfileService profileService(CrudService crudService) {
        return new ProfileServiceImpl(crudService);
    }

    @Bean
    public UserService userService(CrudService crudService) {
        return new UserServiceImpl(crudService);
    }


    /**
     * Configures the AuthenticationManager with a DaoAuthenticationProvider and a PasswordEncoder.
     */
    @Bean("securityAuthManager")
    @Primary
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder,
                                                       UserDetailsService userDetailsService) {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        if (configInterceptors != null) {
            for (SecurityConfigurationInterceptor interceptor : configInterceptors) {
                interceptor.configure(provider);
            }
        }

        return new ProviderManager(provider);
    }


    /**
     * Security configuration for RESTful API endpoints.
     * Configures stateless security for paths under /api/**.
     * Disables CSRF and the security context for these endpoints.
     * Allows public access to /api/public/** and requires authentication for other routes.
     * Adds token-based authentication filters before the username/password filter.
     *
     * @param http              HttpSecurity
     * @param accountServiceAPI AccountServiceAPI
     * @param jwtService        JWTService
     * @return SecurityFilterChain
     * @throws Exception if an error occurs
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http, AccountServiceAPI accountServiceAPI, JWTService jwtService) throws Exception {

        http.securityMatcher("/api/**")
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .securityContext(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**", "/api/v2/public/**", "/api/security/login").permitAll()
                        .anyRequest().authenticated()
                )

                .addFilterBefore(new JWTAuthenticationFilter(jwtService, accountServiceAPI), UsernamePasswordAuthenticationFilter.class)

                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json;charset=UTF-8");
                            var message = ex.getMessage();
                            if (message == null || message.isBlank()) {
                                message = "Unauthorized access";
                            }
                            res.getWriter().write("""
                                    {"status":401,"error":"Unauthorized","message":"%s"}
                                    """.formatted(message.replace("\"", "'")));
                        })
                );

        logger.info("Configured API SecurityFilterChain (stateless)");
        return http.build();
    }


    /**
     * Web security configuration for stateful web flows (sessions, forms and CSRF by default).
     * <p>
     * Configures form-based authentication, session management and security context repository.
     * This chain applies to non-API and non-OAuth2 requests.
     *
     * @param http               HttpSecurity
     * @param userDetailsService UserDetailsService
     * @param authMgr            AuthenticationManager
     * @param successHandler     SavedRequestAwareAuthenticationSuccessHandler
     * @return SecurityFilterChain
     * @throws Exception if an error occurs
     */
    @Bean
    @Order(3)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http,
                                                      UserDetailsService userDetailsService,
                                                      AuthenticationManager authMgr,
                                                      SavedRequestAwareAuthenticationSuccessHandler successHandler,
                                                      AccountServiceAPI accountServiceAPI, JWTService jwtService) throws Exception {


        http.securityMatcher("/**")
                .securityMatcher((request) ->
                        !request.getRequestURI().startsWith("/api/")
                                && !request.getRequestURI().startsWith("/oauth2/"))
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(4)
                        .maxSessionsPreventsLogin(false))
                .userDetailsService(userDetailsService)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/login/**", "/logout", "/actuator/**")
                        .permitAll()

                )
                .formLogin(c -> c
                        .successHandler(successHandler)
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", false)
                        .loginPage("/login")
                        .permitAll()
                )
                .logout(c -> c
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .addLogoutHandler((request, response, authentication) -> {
                            removeJwtCookie(request, response);
                        })
                        .permitAll()
                ).securityContext(ctx -> ctx
                        .securityContextRepository(new HttpSessionSecurityContextRepository())
                ).addFilterBefore(new JsonUsernamePasswordAuthFilter(authMgr), UsernamePasswordAuthenticationFilter.class);


        // Interceptores de configuración adicionales (extensibilidad)
        if (configInterceptors != null) {
            for (SecurityConfigurationInterceptor interceptor : configInterceptors) {
                logger.info("Processing SecurityConfigurationInterceptor: " + interceptor.getClass().getSimpleName());
                interceptor.configure(http);
            }
        }


        http.authorizeHttpRequests(c -> c.anyRequest().authenticated());
        logger.info("Configured Web SecurityFilterChain (stateful)");
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(HttpFirewall firewall) {
        return (web) -> {
            // Configura el firewall personalizado
            web.httpFirewall(firewall);

            // Configura canales o cualquier otra extensión
            configureMessageChannel();

            // Ejecuta interceptores de configuración externa
            if (configInterceptors != null && !configInterceptors.isEmpty()) {
                for (SecurityConfigurationInterceptor interceptor : configInterceptors) {
                    try {
                        interceptor.configure(web);
                        logger.info("Applied SecurityConfigurationInterceptor: {}", interceptor.getClass().getSimpleName());
                    } catch (Exception e) {
                        logger.warn("Error applying interceptor {}: {}", interceptor.getClass().getSimpleName(), e.getMessage());
                    }
                }
            }

            // Aplica rutas completamente ignoradas (sin filtros de seguridad)
            if (ignorings != null) {
                for (IgnoringSecurityMatcher ism : ignorings) {
                    logger.info("Ignoring completely " + ism.getClass().getSimpleName()
                            + " paths: " + Arrays.toString(ism.matchers()));
                    web.ignoring().requestMatchers(ism.matchers());
                }
            }
        };
    }

    @Bean
    public HttpFirewall firewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowBackSlash(false);
        firewall.setAllowSemicolon(false);
        firewall.setAllowUrlEncodedSlash(true); // útil si tu front usa rutas codificadas %2F
        return firewall;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }


    @Bean
    public SavedRequestAwareAuthenticationSuccessHandler savedRequestAwareAuthenticationSuccessHandler() {
        SavedRequestAwareAuthenticationSuccessHandler auth = new SavedRequestAwareAuthenticationSuccessHandler();
        auth.setTargetUrlParameter("targetUrl");
        return auth;
    }


    @Bean
    public EntityReferenceRepository<Long> usersEntityReferenceRepository() {
        DefaultEntityReferenceRepository<Long> repo = new DefaultEntityReferenceRepository<>(User.class, "username");
        repo.setCacheable(true);

        return repo;
    }

    @Bean
    public EntityReferenceRepository<Long> userProfileEntityReferenceRepository() {
        DefaultEntityReferenceRepository<Long> repo = new DefaultEntityReferenceRepository<>(Profile.class, "nombre");
        repo.setCacheable(true);

        return repo;
    }

    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }


    private void configureMessageChannel() {
        if (messageService != null) {
            messageService.createChannel(CHANNEL_NAME, null);
        }

    }


    public static void clearCache() {
        try {
            var mgr = Containers.get().findObject(CacheManager.class);
            if (mgr != null) {
                var cache = mgr.getCache(CACHE_NAME);
                if (cache != null) {
                    cache.clear();
                }
            }

        } catch (Exception e) {
        }
    }

    /**
     * Configura el contexto de seguridad directamente con la autenticación proporcionada.
     * Maneja atributos específicos para solicitudes API y contextos multi-tenant.
     *
     * @param authentication la autenticación a establecer en el contexto
     * @param request        la solicitud HTTP actual
     * @param response       la respuesta HTTP actual
     * @return el contexto de seguridad configurado
     */
    public static SecurityContext directAuthentication(Authentication authentication,
                                                       HttpServletRequest request,
                                                       HttpServletResponse response) {

        // Crear un contexto limpio para evitar contaminación entre hilos
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        if (authentication == null) {
            logger.warn("Attempted to set null authentication context");
            return context;
        }

        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User user)) {
            logger.warn("Authentication principal is not instance of User: {}",
                    principal != null ? principal.getClass().getName() : "null");
            return context;
        }

        boolean apiRequest = isApiRequest(request);

        // Propaga atributos del request para contexto de tenant
        if (apiRequest) {
            try {
                RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
            } catch (IllegalStateException ex) {
                logger.error("Request context not available to set attributes", ex);
            }

            logger.debug("API request detected → accountId: " + user.getAccountId());

            // Asegura aislamiento del contexto multi-tenant
            AccountServiceAPI accountService = Containers.get().findObject(AccountServiceAPI.class);
            if (accountService != null) {
                accountService.setCurrentAccount(user.getAccountId());
            } else {
                logger.error("AccountServiceAPI not found in Containers — tenant context not set");
            }

            SpringSecurtyApplicationListener.fireOnUserTokenLoginListeners(user);
        } else {
            SpringSecurtyApplicationListener.fireOnUserLoginListeners(user);
        }


        logger.info("User '{}' authenticated via {} (api={}, roles={})",
                user.getUsername(),
                authentication.getClass().getSimpleName(),
                apiRequest,
                user.getAuthorities());

        return context;
    }

    /**
     * Detects whether the request belongs to the API domain or path.
     * Prefers URI detection over subdomain for robustness.
     *
     * @param request the current HttpServletRequest
     * @return true if the request targets the API, false otherwise
     */
    public static boolean isApiRequest(HttpServletRequest request) {
        if (request == null) return false;

        String uri = request.getRequestURI();
        if (uri != null && uri.startsWith("/api/")) return true;

        // Si usas subdominios: api.dynamiaerp.com
        String subdomain = HttpUtils.getSubdomain(request);
        return "api".equalsIgnoreCase(subdomain);
    }

    /**
     * Builds a secure ResponseCookie with recommended attributes.
     * Uses SameSite=Strict, HttpOnly, root path and domain derived from the request.
     *
     * @param name    the cookie name
     * @param value   the cookie value
     * @param maxAge  the maximum age for the cookie
     * @param request the current HttpServletRequest used to determine domain
     * @return the configured ResponseCookie
     */
    public static ResponseCookie buildSecureCookie(String name, String value, Duration maxAge, HttpServletRequest request) {

        String domain = request.getServerName();

        var builder = ResponseCookie.from(name, value)
                .httpOnly(isProductionEnv())
                .path("/")
                .sameSite(isProductionEnv() ? "Strict" : "Lax")
                .secure(isProductionEnv())
                .maxAge(maxAge);

        if (isProductionEnv()) {
            builder.domain(domain);
        }

        return builder.build();

    }

    /**
     * Converts a ResponseCookie to a traditional javax.servlet.http.Cookie.
     *
     * @param responseCookie the ResponseCookie to convert
     * @return an equivalent Cookie instance
     */
    public static Cookie toCookie(ResponseCookie responseCookie) {
        Cookie cookie = new Cookie(responseCookie.getName(), responseCookie.getValue());
        cookie.setHttpOnly(responseCookie.isHttpOnly());
        cookie.setPath(responseCookie.getPath());
        cookie.setDomain(responseCookie.getDomain());
        cookie.setSecure(responseCookie.isSecure());
        cookie.setAttribute("SameSite", responseCookie.getSameSite());
        cookie.setMaxAge((int) responseCookie.getMaxAge().getSeconds());
        return cookie;
    }

    /**
     * Standard session duration (30 minutes).
     *
     * @return Duration of a standard session
     */
    public static Duration getSessionDuration() {
        return Duration.ofMinutes(60);
    }

    /**
     * "Remember-me" session duration (7 days).
     *
     * @return Duration used for remember-me sessions
     */
    public static Duration getRememberMeDuration() {
        return Duration.ofDays(15);
    }

    public static void sendJwtCookie(HttpServletRequest request, HttpServletResponse response, User user) {
        sendJwtCookie(request, response, user, null);
    }

    /**
     * Sends a secure JWT cookie to the client based on session configuration.
     *
     * @param request  the current HttpServletRequest
     * @param response the current HttpServletResponse
     * @param user     the authenticated user used to generate the JWT token
     */
    public static void sendJwtCookie(HttpServletRequest request, HttpServletResponse response, User user, Duration customDuration) {
        if (user == null || request == null || response == null) {
            return;
        }

        Duration sessionDuration = DynamiaSecurityConfig.getSessionDuration();
        if ("true".equals(request.getHeader("X-Remember-Me"))) {
            sessionDuration = DynamiaSecurityConfig.getRememberMeDuration();
        }

        if (customDuration != null) {
            sessionDuration = customDuration;
        }

        var jwt = user.generateJWTToken(sessionDuration);
        var jwtCookie = buildSecureCookie(DynamiaSecurityConfig.JWT_COOKIE_NAME, jwt, sessionDuration, request);
        response.addCookie(toCookie(jwtCookie));
    }

    /**
     * Removes the JWT cookie from the client by setting its max age to zero.
     *
     * @param request  the current HttpServletRequest
     * @param response the current HttpServletResponse
     */
    public static void removeJwtCookie(HttpServletRequest request, HttpServletResponse response) {
        if (request == null || response == null) {
            return;
        }

        var jwtCookie = buildSecureCookie(DynamiaSecurityConfig.JWT_COOKIE_NAME, "", Duration.ofSeconds(0), request);
        response.addCookie(toCookie(jwtCookie));
    }

    /**
     * Retrieves the JWT cookie from the given HTTP request.
     *
     * @param request the current HttpServletRequest
     * @return the JWT Cookie if present, or null if not found
     */
    public static Cookie getJwtCookie(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (JWT_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    public static boolean isProductionEnv() {
        if (productionEnv == null) {
            Environment env = Containers.get().findObject(Environment.class);
            if (env == null) {
                productionEnv = false;
            } else {
                String prod = env.getProperty("prod", "false");
                productionEnv = "true".equalsIgnoreCase(prod);
            }
        }
        return productionEnv;
    }

}
