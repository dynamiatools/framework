package tools.dynamia.modules.reports.core;

import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.Validator;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.modules.reports.core.domain.ReportDataSourceConfig;

/**
 * Checks the driver and URL of a datasource when it is saved, see {@link ReportDataSourceValidator}.
 */
@Provider
public class ReportDataSourceConfigValidator implements Validator<ReportDataSourceConfig> {

    @Override
    public void validate(ReportDataSourceConfig config) throws ValidationError {
        ReportDataSourceValidator.validate(config);
    }
}
