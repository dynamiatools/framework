package tools.dynamia.web.navigation;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.crud.CrudPage;
import tools.dynamia.integration.Containers;
import tools.dynamia.navigation.ModuleContainer;
import tools.dynamia.navigation.Page;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Configuration class that dynamically registers REST API routes for CRUD operations based on the pages defined in the application's modules.
 *
 * <p>This class listens for the application context to be fully initialized and then iterates through all modules and their associated pages to create dynamic REST API routes for CRUD operations. Each page's virtual path is used to construct unique URIs for the standard CRUD operations (Create, Read, Update, Delete), which are then mapped to the corresponding methods in the {@link RestNavigationController}.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @Configuration
 * public class MyAppRestApiNavigationConfig extends RestApiNavigationConfiguration {
 *     // Additional configuration or overrides can be added here if needed
 * }
 * }</pre>
 *
 * @author Mario A. Serrano Leones
 */
public class RestApiNavigationConfiguration {


    public static final String API_URI = "/api/";
    private final LoggingService logger = new SLF4JLoggingService(RestApiNavigationConfiguration.class);

    private final RequestMappingInfo.BuilderConfiguration options;


    public RestApiNavigationConfiguration() {
        options = new RequestMappingInfo.BuilderConfiguration();
        options.setPatternParser(new PathPatternParser());
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

        String base = API_URI;
        RestApiBasePathProvider pathProvider = Containers.get().findObject(RestApiBasePathProvider.class);
        if(pathProvider!=null){
            base = pathProvider.getBaseApiPath();
        }


        var route = base + p.getVirtualPath() + uri;
        var routeAlt = base + p.getPrettyVirtualPath() + uri;
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
