package tools.dynamia.web.navigation;

import org.springframework.web.bind.annotation.RequestMethod;
import tools.dynamia.navigation.Page;


/**
 * Crud REST customizer
 */
public interface CrudRestNavigationCustomizer {


    /**
     * Override autogenerate REST API endpoints. You can return null if want to disable endpoint
     *
     */
    String customEndpoint(Page page, String actualEndpoint, RequestMethod requestMethod);
}
