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

import org.zkoss.zk.ui.Component;
import tools.dynamia.commons.Messages;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.reports.core.ReportDataSource;
import tools.dynamia.modules.reports.core.ReportsUtils;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.services.ReportsService;
import tools.dynamia.modules.reports.ui.actions.ViewReportAction;
import tools.dynamia.zk.navigation.ComponentPage;

public class ReportPage extends ComponentPage {

    private final Report report;

    public ReportPage(final Report report) {
        super("report" + report.getId(), report.getName(), null);
        this.report = report;
        this.setAlwaysAllowed(true);
        final String title = Messages.get(ViewReportAction.class, "pageTitle");
        setLongNameSupplier(() -> title + ": " + report.getName());
    }

    @Override
    public Component renderPage() {
        ReportDataSource datasource = ReportsUtils.findDatasource(report);
        ReportsService service = Containers.get().findObject(ReportsService.class);
        ReportViewer viewer = new ReportViewer(service, report, datasource);
        viewer.execute();
        return viewer;
    }


}
