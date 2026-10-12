package tools.dynamia.demos.movies.config;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import tools.dynamia.demos.movies.providers.PublicApiModuleProvider;
import tools.dynamia.navigation.Page;
import tools.dynamia.web.navigation.CrudRestNavigationCustomizer;

/**
 * Keeps the {@code public} module read-only: POST, PUT and DELETE routes are never registered
 * (returning {@code null} disables an endpoint).
 */
@Component
public class PublicApiReadOnlyCustomizer implements CrudRestNavigationCustomizer {

    @Override
    public String customEndpoint(Page page, String actualEndpoint, RequestMethod requestMethod) {
        boolean isPublic = page.getVirtualPath() != null && page.getVirtualPath().startsWith(PublicApiModuleProvider.MODULE_ID + "/");
        if (isPublic && requestMethod != RequestMethod.GET) {
            return null;
        }
        return actualEndpoint;
    }
}
