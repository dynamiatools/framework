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
import org.zkoss.zul.Filedownload;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.commons.Messages;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.services.ReportsService;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;

import java.io.File;

@InstallAction
public class ExportReportAction extends AbstractCrudAction {


    private final ReportsService service;

    @Autowired
    public ExportReportAction(ReportsService service) {
        this.service = service;
        setName(Messages.get(ExportReportAction.class, "export"));
        setImage("down");
        setApplicableClass(Report.class);
        setMenuSupported(true);

    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        Report report = (Report) evt.getData();
        if (report != null) {
            try {
                File file = service.exportReport(crudService().load(Report.class, report.getId()));
                Filedownload.save(file, "text/json");
            } catch (Exception e) {
                UIMessages.showMessage("Error exporting report: " + e.getMessage(), MessageType.ERROR);
            }
        } else {
            UIMessages.showMessage("Select report to export", MessageType.WARNING);
        }

    }

}
