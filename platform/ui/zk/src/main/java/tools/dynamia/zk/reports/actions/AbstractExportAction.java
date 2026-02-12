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
import org.zkoss.zul.TreeModel;
import tools.dynamia.actions.AbstractAction;
import tools.dynamia.actions.AbstractClassAction;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.ActionGroup;
import tools.dynamia.actions.ReadableOnly;
import tools.dynamia.commons.ApplicableClass;
import tools.dynamia.commons.ClassMessages;
import tools.dynamia.commons.Messages;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudState;
import tools.dynamia.domain.query.DataSet;
import tools.dynamia.reports.ReportOutputType;
import tools.dynamia.reports.SimpleReportDescriptor;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.viewers.DataSetView;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.zk.crud.CrudController;
import tools.dynamia.zk.crud.CrudView;
import tools.dynamia.zk.crud.ui.EntityTreeModel;
import tools.dynamia.zk.util.ZKUtil;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public abstract class AbstractExportAction extends AbstractAction implements ReadableOnly {

    protected CrudController crudController;
    protected final int LARGE = 5000;
    protected final ClassMessages MESSAGES = ClassMessages.get(AbstractExportAction.class);

    public AbstractExportAction() {
        setName(Messages.get(getClass(), "export_" + getOuputType().getExtension()));
        setImage("export-" + getOuputType().getExtension());
        setGroup(ActionGroup.get("EXPORT"));
        setVisible(false);
    }

    public void actionPerformed(ActionEvent evt) {
        DataSet dataSet = crudController != null ? crudController.getQueryResult() : null;
        Collection data = null;
        if (dataSet != null && dataSet.getData() != null) {
            if (dataSet.getData() instanceof Collection collection) {
                data = collection;
            } else if (dataSet.getData() instanceof TreeModel treeModel) {
                data = ZKUtil.flatTreeModel(treeModel);
            }
        }

        if (data != null) {

            if (data.size() > LARGE) {
                Collection finalData = data;
                UIMessages.showQuestion(MESSAGES.get("confirm_large_export"),
                        () -> export(finalData, getViewDescriptor()));
            } else {
                export(data, getViewDescriptor());
            }
        }
    }

    protected abstract void export(Collection data, ViewDescriptor descriptor);


    public abstract ReportOutputType getOuputType();

    protected void customizeReportDescriptor(SimpleReportDescriptor descriptor) {

    }

    protected ViewDescriptor getViewDescriptor() {
        var entityClass = crudController.getEntityClass();
        ViewDescriptor viewDescriptor = Viewers.findViewDescriptor(entityClass, "export");
        if (viewDescriptor == null) {
            viewDescriptor = Viewers.findViewDescriptor(entityClass, "tree");
        }

        if (viewDescriptor == null) {
            viewDescriptor = Viewers.getViewDescriptor(entityClass, "table");
        }

        return viewDescriptor;
    }

    public CrudController getCrudController() {
        return crudController;
    }

    public void setCrudController(CrudController crudController) {
        this.crudController = crudController;
    }

    protected File createTempFile() {
        try {
            return File.createTempFile("export_" + System.currentTimeMillis(), "." + getOuputType().getExtension());
        } catch (IOException e) {
            log("Error creating temp file", e);
            UIMessages.showMessage("Error: " + e.getMessage(), MessageType.ERROR);
            return null;
        }
    }

    protected void download(File temp) {
        try {
            Filedownload.save(temp, null);
        } catch (Exception e) {
            log("Error downloading file", e);
        }
    }

}
