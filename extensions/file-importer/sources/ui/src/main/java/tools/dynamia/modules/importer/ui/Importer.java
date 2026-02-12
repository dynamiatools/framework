
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

package tools.dynamia.modules.importer.ui;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.*;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.ActionEventBuilder;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.integration.ProgressMonitor;
import tools.dynamia.modules.importer.ImportAction;
import tools.dynamia.modules.importer.ImportOperation;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.impl.DefaultViewDescriptor;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.zk.actions.ActionToolbar;
import tools.dynamia.zk.actions.ButtonActionRenderer;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.viewers.table.TableView;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Mario Serrano Leones
 */
public class Importer extends Window implements ActionEventBuilder {

    private ActionToolbar toolbar = new ActionToolbar(this);

    private Progressmeter progress = new Progressmeter();
    private Label progressLabel = new Label();

    private Borderlayout layout = new Borderlayout();
    private ImportAction currentAction;
    private ImportOperation currentOperation;

    private Button btnProcesar;
    private Button btnCancelar;

    private DefaultViewDescriptor tableDescriptor;

    private boolean operationRunning;

    private TableView table;
    private String formatFileName = "ImportFormat";

    public Importer() {
        buildLayout();
        toolbar.setActionRenderer(new ButtonActionRenderer());
        addAction(new GenerateImportFormatAction("Descargar Formato"));
        tableDescriptor =  new DefaultViewDescriptor(null, "table");
        updateDescriptorId();
    }

    private void updateDescriptorId() {
        tableDescriptor.setId("Importer_"+getFormatFileName());
    }

    @Override
    public ActionEvent buildActionEvent(Object source, Map<String, Object> params) {

        resetProgress();
        return new ActionEvent(null, this, params);
    }

    private void resetProgress() {
        progress.setVisible(false);
        progress.setValue(0);
        progressLabel.setValue("");
        clearBusy();
    }

    public void updateProgress(ProgressMonitor monitor) {

        if (monitor.getCurrent() > 0) {
            try {

                int value = (int) (monitor.getCurrent() * 100 / monitor.getMax());
                if (!progress.isVisible()) {
                    progress.setVisible(true);
                }
                progress.setValue(value);
                progressLabel.setValue(monitor.getMessage());

                showBusy(monitor.getMessage());
            } catch (Exception e) {
                // TODO: handle exception
            }
        }

    }

    public void showBusy(String message) {
        Clients.showBusy(layout.getCenter().getFirstChild(), message);
    }

    public void clearBusy() {
        Clients.clearBusy(layout.getCenter().getFirstChild());
    }

    private void buildLayout() {
        setHflex("1");

        setVflex("1");
        appendChild(layout);
        layout.setHflex("1");
        layout.setVflex("1");

        layout.appendChild(new Center());
        layout.appendChild(new North());
        layout.appendChild(new South());


        layout.getNorth().appendChild(toolbar);

        Hlayout controls = new Hlayout();
        layout.getSouth().appendChild(controls);

        this.btnProcesar = new Button("Procesar datos importados");
        btnProcesar.setStyle("margin:4px");
        btnProcesar.addEventListener(Events.ON_CLICK, event -> processImportedData());

        this.btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle("margin:4px");
        btnCancelar.addEventListener(Events.ON_CLICK, event -> {
            if (currentOperation == null) {
                return;
            }

            UIMessages.showQuestion("Esta seguro que desea cancelar: " + currentOperation.getName() + "?",
                    () -> cancel());

        });

        progress.setHflex("1");
        progress.setVisible(false);
        progress.setStyle("margin-top: 10px");
        controls.appendChild(btnProcesar);
        controls.appendChild(btnCancelar);
        controls.appendChild(progress);

        setOperationStatus(false);
    }

    private void processImportedData() {
        if (currentAction != null) {

            if (currentOperation != null) {
                UIMessages.showMessage("Existe un proceso de importacion ejecuntandose en este momento",
                        MessageType.WARNING);
                return;
            }

            UIMessages.showQuestion(
                    "Esta seguro que desea procesar los datos importados? Esta accion puede tardar varios minutos.",
                    () -> {
                        resetProgress();
                        currentAction.processImportedData(Importer.this);
                    });

        } else {
            UIMessages.showMessage("NO HAY DATOS QUE PROCESAR", MessageType.WARNING);
        }
    }

    public void cancel() {
        if (currentOperation != null) {
            currentOperation.cancelGracefully();
            currentOperation = null;

        }
    }

    public void addAction(ImportAction action) {
        toolbar.addAction(action);
    }

    public void setCurrentAction(ImportAction importAction) {
        this.currentAction = importAction;
    }

    public void setCurrentOperation(ImportOperation currentProcess) {
        this.currentOperation = currentProcess;
    }

    public ImportOperation getCurrentOperation() {
        return currentOperation;
    }

    public void setOperationStatus(boolean running) {
        this.operationRunning = running;
        checkRunning();
        progress.setVisible(running);
    }

    @Override
    public void onClose() {
        cancel();
    }

    private void checkRunning() {
        btnCancelar.setDisabled(!operationRunning);
        btnProcesar.setDisabled(operationRunning);
        setClosable(!operationRunning);

        for (Component child : toolbar.getChildren()) {
            if (child instanceof Button) {
                Button button = (Button) child;
                button.setDisabled(operationRunning);
            }
        }

        if (!isOperationRunning()) {
            resetProgress();
        }
    }

    public boolean isOperationRunning() {
        return operationRunning;
    }

    public Field addColumn(String name) {
        return addColumn(name, false);
    }

    public Field addColumn(String name, boolean required) {
        String firstChar = StringUtils.getFirstCharacter(name).toLowerCase();
        name = firstChar + name.substring(1);
        String label = StringUtils.capitalizeAllWords(name);
        return addColumn(label, name, required);
    }

    public Field addColumn(String name, String path) {
        return addColumn(name, path, false);
    }

    public Field addColumn(String name, String path, boolean required) {
        Field field = new Field(path);
        field.setLabel(name);
        field.setRequired(required);
        setupRequiredField(field, required);
        tableDescriptor.addField(field);
        return field;
    }


    public Field addColumn(String name, String path, String reference) {
        return addColumn(name, path, reference, false);
    }

    public Field addColumn(String name, String path, String reference, boolean required) {
        Field field = new Field(path);
        field.setLabel(name);
        field.setComponent("entityreflabel");
        field.addParam("entityAlias", reference);
        field.setRequired(required);
        setupRequiredField(field, required);
        tableDescriptor.addField(field);
        return field;

    }

    private void setupRequiredField(Field field, boolean required) {
        if (required) {
            field.setLabel(field.getLabel()+"*");
        }
    }

    public void initTable(List data) {
        this.table = (TableView) Viewers.getView(tableDescriptor);

        table.setSizedByContent(true);

        if (data != null && !data.isEmpty()) {
            table.setValue(data);
        }

        layout.getCenter().getChildren().clear();
        layout.getCenter().appendChild(table);
    }

    public void clearTable() {
        initTable(Collections.EMPTY_LIST);

    }

    public void show(String title) {
        ZKUtil.showDialog(title, this, "90%", "90%");

    }

    public Object getSelected() {
        if (table != null) {
            return table.getSelected();
        } else {
            return null;
        }
    }

    public ViewDescriptor getTableDescriptor() {
        return tableDescriptor;
    }

    public TableView getTable() {
        return table;
    }

    public String getFormatFileName() {
        return formatFileName;
    }

    public void setFormatFileName(String formatFileName) {
        this.formatFileName = formatFileName;
        updateDescriptorId();
    }

    public String[] getColumnsFieldsName(){
        return tableDescriptor.getFields().stream().map(Field::getName).toArray(String[]::new);
    }
}
