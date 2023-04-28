/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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

import org.zkoss.util.media.Media;
import org.zkoss.zul.Fileupload;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.commons.ClassMessages;
import tools.dynamia.io.IOUtils;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.reports.BIRTReportUtils;
import tools.dynamia.zk.reports.ui.BIRTReportEditor;

import java.io.File;
import java.io.IOException;

@InstallAction
public class UploadBIRTReportAction extends BIRTReportAction {

    private final ClassMessages messages = ClassMessages.get(UploadBIRTReportAction.class);

    public UploadBIRTReportAction() {
        setName(messages.get("UploadReport"));
        setImage("up");
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        Fileupload.get(e -> {
            Media media = e.getMedia();

            if (media.getName().endsWith(".rptdesign")) {
                save(media, evt.getSource());
            } else {
                UIMessages.showMessage(messages.get("NotValidReportFile"), MessageType.ERROR);
            }
        });

    }

    private void save(Media media, Object source) {
        try {
            File reportDir = new File(BIRTReportUtils.getReportsPath());
            File reportFile = new File(reportDir, media.getName());
            IOUtils.copy(media.getStreamData(), reportFile);
            UIMessages.showMessage(messages.get("ReportUploaded", media.getName()));

            BIRTReportEditor editor = (BIRTReportEditor) source;
            editor.refresh();
        } catch (IOException e) {
            UIMessages.showMessage(messages.get("UploadReportError", e.getMessage()), MessageType.ERROR);
        }

    }

}
