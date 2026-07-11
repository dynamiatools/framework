package tools.dynamia.modules.functions;

import tools.dynamia.domain.InstallValidator;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.Validator;
import tools.dynamia.domain.ValidatorUtil;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.modules.functions.domain.DynamiaHttpFunction;

/**
 * Enforces the versioning rules described in the extension's README for
 * {@link DynamiaHttpFunction}: versions start at 1, {@code (name, functionVersion)} is unique, and a
 * new version of an existing function must be strictly greater than the current maximum version.
 *
 * @author Mario A. Serrano Leones
 */
@InstallValidator
public class DynamiaHttpFunctionValidator implements Validator<DynamiaHttpFunction> {

    @Override
    public void validate(DynamiaHttpFunction function) throws ValidationError {
        ValidatorUtil.validateEmpty(function.getName(), "Function name is required");
        ValidatorUtil.validateEmpty(function.getUrl(), "Function url is required");

        if (function.getFunctionVersion() < 1) {
            throw new ValidationError("Function version must start at 1");
        }

        CrudService crudService = DomainUtils.lookupCrudService();

        if (function.getId() == null) {
            QueryParameters params = QueryParameters.with("name", function.getName())
                    .add("functionVersion", function.getFunctionVersion());
            if (crudService.count(DynamiaHttpFunction.class, params) > 0) {
                throw new ValidationError("Function [%s] version [%s] already exists", function.getName(), function.getFunctionVersion());
            }

            Integer maxVersion = findMaxVersion(crudService, function.getName());
            if (maxVersion != null && function.getFunctionVersion() <= maxVersion) {
                throw new ValidationError("New version of function [%s] must be greater than the current maximum version [%s]",
                        function.getName(), maxVersion);
            }
        }
    }

    private Integer findMaxVersion(CrudService crudService, String name) {
        var versions = crudService.find(DynamiaHttpFunction.class,
                QueryParameters.with("name", name).orderBy("functionVersion", false));
        return versions.isEmpty() ? null : versions.get(0).getFunctionVersion();
    }
}
