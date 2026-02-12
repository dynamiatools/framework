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
package tools.dynamia.zk.reports.actions;

import org.zkoss.zul.Filedownload;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.commons.ClassMessages;
import tools.dynamia.commons.Messages;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.integration.ProgressMonitor;
import tools.dynamia.io.IOUtils;
import tools.dynamia.reports.ExporterColumn;
import tools.dynamia.reports.PlainFileExporter;
import tools.dynamia.reports.ReportOutputType;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.viewers.DataSetView;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.JsonView;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.zk.crud.CrudView;
import tools.dynamia.zk.ui.LongOperationMonitorWindow;
import tools.dynamia.zk.util.LongOperation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

@InstallAction
public class ExportJsonAction extends AbstractExportAction {

    private static final int LARGE = 5000;
    private static final ClassMessages MESSAGES = ClassMessages.get(ExportExcelAction.class);

    public ExportJsonAction() {
        setName(Messages.get(getClass(), "export_json"));

    }

    @Override
    public ReportOutputType getOuputType() {
        return ReportOutputType.JSON;
    }


    public void export(Collection data, ViewDescriptor descriptor) {

        try {
            JsonView jsonView = new JsonView(data, descriptor);
            File temp = createTempFile();
            IOUtils.copy(jsonView.renderJson().getBytes(StandardCharsets.UTF_8), temp);
            download(temp);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected ViewDescriptor getViewDescriptor() {
        var entityClass = crudController.getEntityClass();
        ViewDescriptor viewDescriptor = Viewers.findViewDescriptor(entityClass, "export");
        if (viewDescriptor == null) {
            viewDescriptor = Viewers.findViewDescriptor(entityClass, "json");
        }

        if (viewDescriptor == null) {
            viewDescriptor = Viewers.findViewDescriptor(entityClass, "tree");
        }

        if (viewDescriptor == null) {
            viewDescriptor = Viewers.getViewDescriptor(entityClass, "table");
        }

        return viewDescriptor;
    }
}
