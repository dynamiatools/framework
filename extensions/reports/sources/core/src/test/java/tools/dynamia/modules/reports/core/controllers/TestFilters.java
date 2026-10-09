package tools.dynamia.modules.reports.core.controllers;

import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.ReportFilter;
import tools.dynamia.modules.reports.core.domain.enums.DataType;

final class TestFilters {

    private TestFilters() {
    }

    static ReportFilter filter(Report report, String name, DataType type, boolean required) {
        var filter = new ReportFilter(name);
        filter.setReport(report);
        filter.setLabel(name);
        filter.setDataType(type);
        filter.setRequired(required);
        return filter;
    }
}
