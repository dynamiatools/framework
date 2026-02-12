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
import tools.dynamia.navigation.Module;
import tools.dynamia.navigation.Page;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.domain.ReportDataSourceConfig;
import tools.dynamia.modules.reports.core.domain.ReportGroup;

/**
 * Helper module to configure DynamiaReports very easy
 */
public class DynamiaReportsModule extends Module {
    private Page reportDesignPage;
    private Page reportViewerPage;
    private Page reportGroupsPage;
    private Page reportDatasourcesPage;

    public DynamiaReportsModule(String id, String name, String description) {
        this(id, name, description, Double.MAX_VALUE);
    }

    public DynamiaReportsModule(String id, String name, String description, double position) {
        super(id, name, description);

        this.reportGroupsPage = new CrudPage("groups", "Reports Groups", ReportGroup.class);
        this.reportDesignPage = new CrudPage("design", "Reports Design", Report.class);
        this.reportDatasourcesPage = new CrudPage("datasources", "Reports Datasource", ReportDataSourceConfig.class);
        this.reportViewerPage = new Page("viewer", "Reports Viewer", "classpath:/zk/dynamia/reports/pages/viewer.zul");


        addPage(reportGroupsPage);
        addPage(reportDesignPage);
        addPage(reportDatasourcesPage);
        addPage(reportViewerPage);
        setIcon("report");
        setPosition(position);
    }

    public Page getReportGroupsPage() {
        return reportGroupsPage;
    }

    public Page getReportDesignPage() {
        return reportDesignPage;
    }

    public Page getReportDatasourcesPage() {
        return reportDatasourcesPage;
    }

    public Page getReportViewerPage() {
        return reportViewerPage;
    }


}
