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
import tools.dynamia.actions.ActionGroup;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.actions.ReadableOnly;
import tools.dynamia.commons.ClassMessages;
import tools.dynamia.integration.ProgressMonitor;
import tools.dynamia.reports.ExporterColumn;
import tools.dynamia.reports.ReportOutputType;
import tools.dynamia.reports.excel.ExcelCollectionExporter;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.zk.converters.Date;
import tools.dynamia.zk.converters.Time;
import tools.dynamia.zk.converters.Util;
import tools.dynamia.zk.ui.LongOperationMonitorWindow;
import tools.dynamia.zk.util.LongOperation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;

@SuppressWarnings("unchecked")
@InstallAction
public class ExportExcelAction extends AbstractExportAction implements ReadableOnly {

    private static final int LARGE = 5000;
    private static final ClassMessages MESSAGES = ClassMessages.get(ExportExcelAction.class);

    public ExportExcelAction() {
        setName(MESSAGES.get("export_excel"));
        setImage("export-xlsx");
        setGroup(ActionGroup.get("EXPORT"));
    }


    public void export(Collection data, ViewDescriptor descriptor) {
        ExcelCollectionExporter exporter = new ExcelCollectionExporter();

        Viewers.getFields(descriptor).stream().filter(f -> f.isVisible() && !f.isCollection()).forEach(f -> {
            ExporterColumn column = new ExporterColumn(f.getName(), f.getLocalizedLabel(), getFormatPattern(f),
                    f.getFieldClass());
            if (f.getParams().get("entityAlias") != null) {
                column.setEntityAlias((String) f.getParams().get("entityAlias"));
            }
            //noinspection unchecked
            exporter.addColumn(column);
        });
        File temp = createTempFile();
        export(data, temp, exporter);
    }


    @SuppressWarnings("unchecked")
    public static void export(Collection data, File temp, ExcelCollectionExporter exporter) {


        ProgressMonitor monitor = new ProgressMonitor();

        @SuppressWarnings("unchecked") LongOperation operation = LongOperation.create()
                .execute(() -> exporter.export(temp, data, monitor))
                .onFinish(() -> {
                    try {
                        Filedownload.save(temp, ReportOutputType.EXCEL.getContentType());
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .onException(Throwable::printStackTrace)
                .start();

        // Update ui each 4 secs
        LongOperationMonitorWindow monitorWindow = new LongOperationMonitorWindow(operation, monitor);
        monitorWindow.setMessageTemplate(MESSAGES.get("exporting_progress"));
        monitorWindow.setTitle(MESSAGES.get("exporting"));
        monitorWindow.doModal();
    }

    private static String getFormatPattern(Field f) {
        String formatPattern = (String) f.getParams().get(Viewers.PARAM_FORMAT_PATTERN);
        if (formatPattern == null || formatPattern.isEmpty()) {
            String converter = (String) f.getParams().get(Viewers.PARAM_CONVERTER);
            converter = Util.checkConverterClass(converter);
            if (Date.class.getName().equals(converter)) {
                formatPattern = "dd/MM/yyyy";
            } else if (Time.class.getName().equals(converter)) {
                formatPattern = "h:mm a";
            }
        }

        return formatPattern;
    }


    @Override
    public ReportOutputType getOuputType() {
        return ReportOutputType.EXCEL;
    }

}
