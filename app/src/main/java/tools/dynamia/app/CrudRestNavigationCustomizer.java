package tools.dynamia.app;

import org.springframework.web.bind.annotation.RequestMethod;
import tools.dynamia.navigation.Page;


/**
 * Crud REST customizer
 */
public interface CrudRestNavigationCustomizer {


    /**
     * Override autogenerate REST API endpoints. You can return null if want to disable endpoint
     *
     * @param page
     * @param actualEndpoint
     * @param requestMethod
     * @return
     */
    String customEndpoint(Page page, String actualEndpoint, RequestMethod requestMethod);
}
