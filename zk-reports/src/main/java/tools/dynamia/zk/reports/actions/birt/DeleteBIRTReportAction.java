/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.zk.reports.actions.birt;

import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.commons.ClassMessages;
import tools.dynamia.io.FileInfo;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.reports.ui.BIRTReportEditor;

@InstallAction
public class DeleteBIRTReportAction extends BIRTReportAction {

    private final ClassMessages messages = ClassMessages.get(DeleteBIRTReportAction.class);

    public DeleteBIRTReportAction() {
        setName(messages.get("DeleteReport"));
        setImage("delete");
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        FileInfo report = (FileInfo) evt.getData();
        if (report != null) {
            UIMessages.showQuestion(messages.get("ConfirmDeleteReport", report.getDescription()), () -> delete(report, evt.getSource()));
        } else {
            UIMessages.showMessage(messages.get("SelectReportError"), MessageType.ERROR);
        }

    }

    private void delete(FileInfo report, Object source) {
        try {
            report.getFile().delete();
            BIRTReportEditor editor = (BIRTReportEditor) source;
            editor.refresh();
            UIMessages.showMessage(messages.get("ReportDeleted", report.getDescription()));
        } catch (Exception e) {
            UIMessages.showMessage(messages.get("DeleteReportError", report.getDescription()), MessageType.ERROR);
        }
    }
}
