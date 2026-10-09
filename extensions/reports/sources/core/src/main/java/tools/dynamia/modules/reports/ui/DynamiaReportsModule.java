/*
 * Copyright (C)  2020. Dynamia Soluciones IT S.A.S - NIT 900302344-1 All Rights Reserved.
 * Colombia - South America
 *
 * This file is free software: you can redistribute it and/or modify it  under the terms of the
 *  GNU Lesser General Public License (LGPL v3) as published by the Free Software Foundation,
 *   either version 3 of the License, or (at your option) any later version.
 *
 *  This file is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *   without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *   See the GNU Lesser General Public License for more details. You should have received a copy of the
 *   GNU Lesser General Public License along with this file.
 *   If not, see <https://www.gnu.org/licenses/>.
 *
 */
package tools.dynamia.modules.reports.ui;

import tools.dynamia.crud.CrudPage;
import tools.dynamia.modules.reports.core.ReportsUi;
import tools.dynamia.modules.reports.core.navigation.ReportViewerPage;
import tools.dynamia.navigation.Module;
import tools.dynamia.navigation.Page;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.ReportDataSourceConfig;
import tools.dynamia.modules.reports.core.domain.ReportGroup;

/**
 * Helper module to configure DynamiaReports very easy. The viewer page depends on {@link ReportsUi}: a
 * {@link ReportViewerPage} for the Vue front (default) or a plain ZK page.
 */
public class DynamiaReportsModule extends Module {
    private Page reportDesignPage;
    private Page reportViewerPage;
    private Page reportGroupsPage;
    private Page reportDatasourcesPage;

    /**
     * Creates the module at the last position.
     * 
     * @param id          module id
     * @param name        module name
     * @param description module description
     */
    public DynamiaReportsModule(String id, String name, String description) {
        this(id, name, description, Double.MAX_VALUE);
    }

    /**
     * Creates the module with groups, design, datasources and viewer pages.
     * 
     * @param id          module id
     * @param name        module name
     * @param description module description
     * @param position    position in the navigation
     */
    public DynamiaReportsModule(String id, String name, String description, double position) {
        super(id, name, description);

        this.reportGroupsPage = new CrudPage("groups", "Reports Groups", ReportGroup.class);
        this.reportDesignPage = new CrudPage("design", "Reports Design", Report.class);
        this.reportDatasourcesPage = new CrudPage("datasources", "Reports Datasource", ReportDataSourceConfig.class);
        this.reportViewerPage = createViewerPage();


        addPage(reportGroupsPage);
        addPage(reportDesignPage);
        addPage(reportDatasourcesPage);
        addPage(reportViewerPage);
        setIcon("report");
        setPosition(position);
    }

    private static Page createViewerPage() {
        String legacyPath = "classpath:/zk/dynamia/reports/pages/viewer.zul";
        if (ReportsUi.current() == ReportsUi.VUE) {
            return new ReportViewerPage("viewer", "Reports Viewer", legacyPath);
        }
        return new Page("viewer", "Reports Viewer", legacyPath);
    }

    /**
     * @return the page that manages report groups
     */
    public Page getReportGroupsPage() {
        return reportGroupsPage;
    }

    /**
     * @return the page that designs reports
     */
    public Page getReportDesignPage() {
        return reportDesignPage;
    }

    /**
     * @return the page that manages external datasources
     */
    public Page getReportDatasourcesPage() {
        return reportDatasourcesPage;
    }

    /**
     * @return the page that lists and runs reports
     */
    public Page getReportViewerPage() {
        return reportViewerPage;
    }


}
