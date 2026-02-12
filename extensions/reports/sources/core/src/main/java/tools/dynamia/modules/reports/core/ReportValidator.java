package tools.dynamia.modules.reports.core;

import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.Validator;
import tools.dynamia.domain.ValidatorUtil;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.modules.reports.core.domain.Report;

@Provider
public class ReportValidator implements Validator<Report> {

    @Override
    public void validate(Report report) throws ValidationError {

        if (report.getExportEndpoint()) {
            ValidatorUtil.validateEmpty(report.getGroup().getEndpointName(), "Group endpoint name is required when export endpoint is enabled");
            ValidatorUtil.validateEmpty(report.getEndpointName(), "Endpoint name is required when export endpoint is enabled");
        }
    }
}
