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

import org.zkoss.zul.Filedownload;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.commons.ClassMessages;
import tools.dynamia.io.FileInfo;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;

import java.io.FileNotFoundException;

@InstallAction
public class DownloadBIRTReportAction extends BIRTReportAction {

    private ClassMessages messages = ClassMessages.get(DownloadBIRTReportAction.class);

    public DownloadBIRTReportAction() {
        setName(messages.get("DownloadReport"));
        setImage("down");
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        FileInfo report = (FileInfo) evt.getData();
        if (report != null) {
            try {
                Filedownload.save(report.getFile(), null);
                UIMessages.showMessage(messages.get("ReportDownloaded"));
            } catch (FileNotFoundException e) {
                
                e.printStackTrace();
            }
        } else {
            UIMessages.showMessage(messages.get("SelectReportError"), MessageType.ERROR);
        }

    }
}
