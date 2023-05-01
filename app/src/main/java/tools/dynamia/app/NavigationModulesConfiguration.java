package tools.dynamia.app;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;
import tools.dynamia.app.controllers.PageNavigationController;
import tools.dynamia.app.controllers.RestNavigationController;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.crud.CrudPage;
import tools.dynamia.navigation.ModuleContainer;
import tools.dynamia.navigation.Page;

import java.lang.reflect.Method;
import java.util.List;

@Configuration
public class NavigationModulesConfiguration {

    public static final String PAGE_URI = "/page/";
    public static final String API_URI = "/api/";
    private final LoggingService logger = new SLF4JLoggingService(NavigationModulesConfiguration.class);

    private final RequestMappingInfo.BuilderConfiguration options;

    public NavigationModulesConfiguration() {
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


    @Autowired
    public void setRestMapper(ModuleContainer container, List<RequestMappingHandlerMapping> mappings,
                              RestNavigationController controller, List<CrudRestNavigationCustomizer> customizers) throws NoSuchMethodException {


        Method routeReadAll = RestNavigationController.class.getMethod("routeReadAll",
                HttpServletRequest.class);
        Method routeReadOne = RestNavigationController.class.getMethod("routeReadOne",
                Long.class, HttpServletRequest.class);
        Method routeCreate = RestNavigationController.class.getMethod("routeCreate",
                String.class, HttpServletRequest.class);
        Method routeUpdate = RestNavigationController.class.getMethod("routeUpdate",
                Long.class, String.class, HttpServletRequest.class);

        Method routeDelete = RestNavigationController.class.getMethod("routeDelete",
                Long.class, HttpServletRequest.class);


        var mapping = mappings.stream().findFirst().get();
        container.getModules().forEach(module ->
                module.forEachPage(page -> {
                    if (page instanceof CrudPage) {
                        addRoute(controller, "", RequestMethod.GET, routeReadAll, mapping, page, customizers);
                        addRoute(controller, "/{id}", RequestMethod.GET, routeReadOne, mapping, page, customizers);
                        addRoute(controller, "", RequestMethod.POST, routeCreate, mapping, page, customizers);
                        addRoute(controller, "/{id}", RequestMethod.PUT, routeUpdate, mapping, page, customizers);
                        addRoute(controller, "/{id}", RequestMethod.DELETE, routeDelete, mapping, page, customizers);
                    }
                }));
    }

    private void addRoute(RestNavigationController controller, String uri, RequestMethod requestMethod, Method method, RequestMappingHandlerMapping mapping, Page p, List<CrudRestNavigationCustomizer> customizers) {
        var route = API_URI + p.getVirtualPath() + uri;
        var routeAlt = API_URI + p.getPrettyVirtualPath() + uri;
        logger.info("Register REST route for " + requestMethod + " " + route);

        boolean custom = false;
        for (var customizer : customizers) {
            var customRoute = customizer.customEndpoint(p, route, requestMethod);
            if (!route.equals(customRoute)) {
                route = customRoute;
                custom = true;
                break;
            }
        }
        if (route != null && !route.isBlank()) {
            var info = RequestMappingInfo.paths(route)
                    .methods(requestMethod)
                    .options(options)
                    .build();

            mapping.registerMapping(info, controller, method);

            if (!route.equals(routeAlt) && !custom) {
                var infoAlt = RequestMappingInfo.paths(routeAlt)
                        .methods(requestMethod)
                        .options(options)
                        .build();
                mapping.registerMapping(infoAlt, controller, method);
            }
        }
    }

}
