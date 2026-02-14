package tools.dynamia.web.navigation;

import org.springframework.web.bind.annotation.RequestMethod;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.navigation.Page;
import tools.dynamia.web.navigation.CrudRestNavigationCustomizer;

/**
 * Default implementation of the {@link CrudRestNavigationCustomizer} interface that provides a basic customization for CRUD REST API endpoints. This implementation simply returns the actual endpoint without any modifications, allowing for a straightforward mapping of CRUD operations to their respective endpoints based on the page's virtual path. Developers can extend this class and override the `customEndpoint` method to implement specific customizations for their application's REST API routes as needed.
 *
 * @author Mario A. Serrano Leones
 */
@Provider
public class DefaultCrudRestNavigationCustomizer implements CrudRestNavigationCustomizer {

    @Override
    public String customEndpoint(Page page, String actualEndpoint, RequestMethod requestMethod) {
        return actualEndpoint;
    }
}
