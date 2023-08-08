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

@Configuration
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
                    logger.info("Register route for " + route);

                    var info = RequestMappingInfo.paths(route)
                            .methods(RequestMethod.GET)
                            .options(options)
                            .build();

                    mapping.registerMapping(info, controller, method);
                }));
    }



}
