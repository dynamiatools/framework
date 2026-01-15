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

package tools.dynamia.modules.importer;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.zkoss.util.media.Media;
import org.zkoss.zul.Fileupload;

import tools.dynamia.actions.ActionRenderer;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.ProgressMonitor;
import tools.dynamia.modules.importer.ui.Importer;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.actions.ToolbarbuttonActionRenderer;

public abstract class ImportExcelAction<T> extends ImportAction {

    private List<T> data;
    private boolean showProgress = false;

    public ImportExcelAction() {
        setName("Import");
        setImage("export-xlsx");
        setAttribute("background", "#5cb85c");
        setAttribute("color", "white");
    }

    public ImportExcelAction(String name) {
        this();
        setName(name);
    }

    public ImportExcelAction(String name, boolean showProgress) {
        this();
        setName(name);
        this.showProgress = showProgress;
    }

    @Override
    public void actionPerformed(Importer importer) {
        Fileupload.get(event -> {
            final Media media = event.getMedia();
            if (media != null) {
                String format = media.getFormat();

                if (format.endsWith("xls") || format.endsWith("xlsx")) {
                    if (showProgress) {
                        importer.initTable(Collections.emptyList());
                        importer.showBusy("Importando");
                        var op = new ImportOperation(getName(), importer) {
                            @Override
                            public void execute(ProgressMonitor monitor) throws Exception {
                                setMonitor(monitor);
                                ImportExcelAction.this.doImport(importer, media);
                            }

                            @Override
                            protected void onFinish(ProgressMonitor monitor) {
                                showData(importer);
                            }
                        };

                        op.start();

                    } else {
                        doImport(importer, media);
                        showData(importer);
                    }
                } else {
                    UIMessages.showMessage("El archivo debe ser en formato excel", MessageType.ERROR);
                }

            } else {
                UIMessages.showMessage("Seleccione archivo de excel para importar", MessageType.ERROR);
            }
        });
    }

    private void showData(Importer importer) {
        importer.initTable(data);
        if (data == null || data.isEmpty()) {
            UIMessages.showMessage("No se obtuvieron datos validos", MessageType.WARNING);
        }
    }

    protected void doImport(Importer win, Media media) {
        try {
            data = importFromExcel(media.getStreamData(), getMonitor());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract List<T> importFromExcel(InputStream excelFile, ProgressMonitor monitor) throws Exception;

    @Override
    public void processImportedData(Importer importer) {
        CrudService crudService = Containers.get().findObject(CrudService.class);

        crudService.executeWithinTransaction(() -> {
            getData().forEach(crudService::save);
        });
        UIMessages.showMessage("Import OK");
        importer.clearTable();
    }

    public List<T> getData() {
        if (data == null) {
            data = Collections.EMPTY_LIST;
        }
        return data;
    }

    @Override
    public ActionRenderer getRenderer() {
        return new ToolbarbuttonActionRenderer(true);
    }

    public boolean isShowProgress() {
        return showProgress;
    }

    public void setShowProgress(boolean showProgress) {
        this.showProgress = showProgress;
    }
}
