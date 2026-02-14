package tools.dynamia.web.navigation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.navigation.ModuleContainer;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Configuration class that dynamically registers page navigation routes based on the pages defined in the application's modules.
 *
 * <p>This class listens for the application context to be fully initialized and then iterates through all modules and their associated pages to create dynamic routes for page navigation. Each page's virtual path is used to construct a unique URI, which is then mapped to the {@link PageNavigationController#route} method.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @Configuration
 * public class MyAppPageNavigationConfig extends PageNavigationConfiguration {
 *     // Additional configuration or overrides can be added here if needed
 * }
 * }</pre>
 *
 * @author Mario A. Serrano Leones
 */
public class PageNavigationConfiguration {

    public static final String PAGE_URI = "/page/";

    private final LoggingService logger = new SLF4JLoggingService(PageNavigationConfiguration.class);

    private final RequestMappingInfo.BuilderConfiguration options;

    public PageNavigationConfiguration() {
        options = new RequestMappingInfo.BuilderConfiguration();
        options.setPatternParser(new PathPatternParser());
    }

    @Autowired
    public void setPageMapper(ModuleContainer container, List<RequestMappingHandlerMapping> mappings,
                              PageNavigationController controller) throws NoSuchMethodException {

        Method method = PageNavigationController.class.getMethod("route", HttpServletRequest.class, HttpServletResponse.class);
        var mapping = mappings.stream().findFirst().get();
        container.getModules().forEach(module ->
                module.forEachPage(p -> {
                    var route = PAGE_URI + p.getPrettyVirtualPath();
                    logger.info("Register PAGE route for " + route);

                    var info = RequestMappingInfo.paths(route)
                            .methods(RequestMethod.GET)
                            .options(options)
                            .build();

                    mapping.registerMapping(info, controller, method);
                }));
    }



}
