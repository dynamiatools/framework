package tools.dynamia.app;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.context.annotation.SessionScope;
import tools.dynamia.app.controllers.PWAManifestController;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.commons.UserInfo;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.navigation.Module;
import tools.dynamia.navigation.ModuleProvider;
import tools.dynamia.templates.ApplicationTemplate;
import tools.dynamia.templates.ApplicationTemplates;
import tools.dynamia.web.navigation.PageNavigationConfiguration;
import tools.dynamia.web.pwa.PWAIcon;
import tools.dynamia.web.pwa.PWAManifest;

import java.util.List;

@Import({MvcConfiguration.class, PageNavigationConfiguration.class})
public class DynamiaToolsWebApplication extends DynamiaBaseConfiguration {

    /**
     * Runs after application startup to initialize the selected application template.
     * @param applicationInfo information about the application
     * @param templates list of available application templates
     * @return an ApplicationRunner that initializes the application template
     */
    @Bean
    public ApplicationRunner afterStartupRunner(ApplicationInfo applicationInfo, List<ApplicationTemplate> templates) {
        return args -> {
            LoggingService.get(getClass()).info("Initializing application template: " + applicationInfo.getTemplate());
            ApplicationTemplate template = ApplicationTemplates.findTemplate(applicationInfo.getTemplate(), templates);
            template.init();
        };
    }

    /**
     * Provides a session-scoped {@link UserInfo} bean if none is registered.
     *
     * @return a new UserInfo instance for the session
     */
    @Bean("userInfo")
    @SessionScope
    @ConditionalOnMissingBean(UserInfo.class)
    public UserInfo userInfo() {
        return new UserInfo();
    }


    /**
     * Provides a default {@link PWAManifest} bean if none is registered.
     *
     * @param applicationInfo information about the application
     * @return a default PWAManifest based on application info
     */
    @Bean
    @ConditionalOnMissingBean(PWAManifest.class)
    public PWAManifest defaultManifest(ApplicationInfo applicationInfo) {
        return PWAManifest.builder()
                .name(applicationInfo.getName())
                .shortName(applicationInfo.getShortName())
                .startUrl("/")
                .display("standalone")
                .description(applicationInfo.getDescription())
                .addIcon(PWAIcon.builder()
                        .src(applicationInfo.getDefaultIcon())
                        .build()
                )
                .build();
    }

    @Bean
    public PWAManifestController pwaManifestController(PWAManifest manifest) {
        return new PWAManifestController(manifest);
    }

    /**
     * Provides an empty {@link ModuleProvider} bean if none is registered.
     *
     * @return a ModuleProvider that returns a dummy module with a random name and message
     */
    @Bean
    @ConditionalOnMissingBean(ModuleProvider.class)
    public ModuleProvider emptyModuleProvider() {
        return () -> new Module(StringUtils.randomString(), "No modules registered");
    }

}
