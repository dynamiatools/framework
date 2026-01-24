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
package tools.dynamia.modules.reports.ui.actions;

import org.springframework.beans.factory.annotation.Autowired;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.commons.Messages;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudState;
import tools.dynamia.navigation.NavigationManager;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.services.ReportsService;
import tools.dynamia.modules.reports.ui.ReportPage;

@InstallAction
public class ViewReportAction extends AbstractCrudAction {

    private final ReportsService service;

    @Autowired
    public ViewReportAction(ReportsService service) {
        this.service = service;

        setName(Messages.get(ViewReportAction.class, "view"));
        setApplicableClass(Report.class);
        setApplicableStates(CrudState.get(CrudState.READ, CrudState.UPDATE));
        setMenuSupported(true);
        setImage("play");
        setBackground("#28a5d4");
        setColor("white");
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        if (evt.getData() instanceof Report report) {
            view(report, true);
        }

    }

    public void view(Report report, boolean reloable) {
        if (reloable && report.getId() != null) {
            report = crudService().load(Report.class, report.getId());
        }
        NavigationManager.getCurrent().setCurrentPage(new ReportPage(report));
    }


}
