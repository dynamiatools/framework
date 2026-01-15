package tools.dynamia.web.navigation;

import org.springframework.web.bind.annotation.RequestMethod;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.navigation.Page;
import tools.dynamia.web.navigation.CrudRestNavigationCustomizer;

@Provider
public class DefaultCrudRestNavigationCustomizer implements CrudRestNavigationCustomizer {

    @Override
    public String customEndpoint(Page page, String actualEndpoint, RequestMethod requestMethod) {
        return actualEndpoint;
    }
}
