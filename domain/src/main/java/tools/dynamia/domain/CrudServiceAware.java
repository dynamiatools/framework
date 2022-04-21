package tools.dynamia.domain;

import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.DomainUtils;

/**
 * Helper class to obtain {@link CrudService} instances
 */
public interface CrudServiceAware {

    /**
     * Lookup a {@link CrudService} instance
     * @return
     */
    default CrudService crudService() {
        return DomainUtils.lookupCrudService();
    }
}
