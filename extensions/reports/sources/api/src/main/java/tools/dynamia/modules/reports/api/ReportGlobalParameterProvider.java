package tools.dynamia.modules.reports.api;

import java.util.Map;

/**
 * Provide parameters or variables for reports
 */
public interface ReportGlobalParameterProvider {
    Map<String, Object> getParams();
}
