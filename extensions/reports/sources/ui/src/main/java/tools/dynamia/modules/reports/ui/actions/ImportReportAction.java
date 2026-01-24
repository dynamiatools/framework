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
import org.zkoss.zul.Fileupload;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.commons.Messages;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.io.IOUtils;
import tools.dynamia.modules.reports.core.domain.Report;
import tools.dynamia.modules.reports.core.services.ReportsService;
import tools.dynamia.ui.UIMessages;

import java.io.File;
import java.util.stream.Stream;

@InstallAction
public class ImportReportAction extends AbstractCrudAction {

    private final ReportsService service;

    @Autowired
    public ImportReportAction(ReportsService service) {
        this.service = service;
        setName(Messages.get(ImportReportAction.class, "import"));
        setApplicableClass(Report.class);
        setImage("up");
    }

    @Override
    public void actionPerformed(final CrudActionEvent evt) {


        Fileupload.get(uevt -> {
            Stream.of(uevt.getMedias()).forEach(m -> {
                try {
                    if (m.getName().endsWith(".json")) {
                        var file = File.createTempFile("report", ".json");
                        IOUtils.copy(m.getStreamData(), file);
                        service.importReport(file);
                    }
                    UIMessages.showMessage("Imported OK");
                    evt.getController().doQuery();
                } catch (Exception e) {
                    UIMessages.showMessage("Error importing: " + e.getMessage());
                }
            });
        });
    }


}
