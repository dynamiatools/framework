package tools.dynamia.modules.reports.core;

import tools.dynamia.integration.CacheManagerUtils;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.ReportGroup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Reports {

    public static final String CACHE_NAME = "reports";

    public static List<Reports> loadAll() {
        List<Reports> reports = new ArrayList<>();

        Report.findActives().forEach(rp -> {
            Reports currentReports = reports.stream()
                    .filter(report -> report.getGroup().getName().equals(rp.getGroup().getName()))
                    .findFirst()
                    .orElse(null);

            if (currentReports == null) {
                currentReports = new Reports(rp.getGroup());
                reports.add(currentReports);
            }

            currentReports.getList().add(rp);
        });

        reports.sort(Comparator.comparing(a -> a.getGroup().getName()));

        return reports;
    }

    public static void clearCache() {
        CacheManagerUtils.clearCache(CACHE_NAME);
    }





    private ReportGroup group;
    private List<Report> list = new ArrayList<>();

    public Reports() {
    }

    public Reports(ReportGroup group) {
        this.group = group;
    }

    // Getters and setters
    public ReportGroup getGroup() {
        return group;
    }

    public void setGroup(ReportGroup group) {
        this.group = group;
    }

    public List<Report> getList() {
        return list;
    }

    public void setList(List<Report> list) {
        this.list = list;
    }
}
