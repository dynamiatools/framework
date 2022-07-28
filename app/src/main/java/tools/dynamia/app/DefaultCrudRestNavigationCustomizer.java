package tools.dynamia.app;

import org.springframework.web.bind.annotation.RequestMethod;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.navigation.Page;

@Provider
public class DefaultCrudRestNavigationCustomizer implements CrudRestNavigationCustomizer {

    @Override
    public String customEndpoint(Page page, String actualEndpoint, RequestMethod requestMethod) {
        return actualEndpoint;
    }
}
