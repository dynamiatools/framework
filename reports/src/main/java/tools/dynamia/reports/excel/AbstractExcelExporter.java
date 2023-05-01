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
package tools.dynamia.reports.excel;

import tools.dynamia.integration.ProgressMonitor;
import tools.dynamia.reports.EnumValueType;
import tools.dynamia.reports.ExporterColumn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mario A. Serrano Leones
 */
public abstract class AbstractExcelExporter<T, DATA> {

    private final List<ExporterColumn<T>> columns = new ArrayList<>();
    private final Map<String, String> columnsTitle = new HashMap<>();
    private EnumValueType defaultEnumValueType = EnumValueType.NAME;

    private ExcelFileWriter excelWriter;

    public AbstractExcelExporter() {
    }

    public AbstractExcelExporter(String... columns) {
        for (String string : columns) {
            addColumn(string);
        }
    }

    public AbstractExcelExporter addColumn(String column) {
        String[] columData = column.trim().split(":");
        if (columData.length == 2) {
            addColumn(columData[0], columData[1]);
        } else {
            addColumn(column, column);
        }
        return this;
    }

    public AbstractExcelExporter addColumn(String name, String title) {
        columns.add(new ExporterColumn<>(name, title));

        return this;
    }

    public AbstractExcelExporter addColumn(String name, String title, EnumValueType enumValueType) {
        ExporterColumn<T> col = new ExporterColumn<>(name, title);
        col.setEnumValueType(enumValueType);
        columns.add(col);

        return this;
    }


    public AbstractExcelExporter addColumn(ExporterColumn<T> column) {
        columns.add(column);
        return this;
    }

    public void export(File file, DATA data) {
        export(file, data, null);
    }

    public void export(File file, DATA data, ProgressMonitor monitor) {
        try {
            ExcelFileWriter writer = new ExcelFileWriter(file);
            writeColumns(writer);
            writeRows(writer, data, monitor);
            writer.write();
            writer.close();
        } catch (IOException ex) {
            throw new ExcelExporterException("Error exporting excel file: " + file, ex);
        }
    }

    private void writeColumns(ExcelFileWriter writer) {
        for (ExporterColumn<T> col : columns) {
            String title = col.getTitle();
            writer.addCell(title);
        }
    }

    /**
     * Start writing file using multiple data sources. Invoke appendData(data)
     * and the stop() to complete
     *
     */
    public void start(File outpufile) {
        if (outpufile != null) {
            excelWriter = new ExcelFileWriter(outpufile);
            writeColumns(excelWriter);
        }
    }

    /**
     * Stop current file
     */
    public void stop() {
        if (excelWriter != null) {
            try {
                excelWriter.write();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            excelWriter.close();
            excelWriter = null;
        }
    }

    /**
     * Append rows to current file. Invoke start(File outpufile) method first.
     *
     */
    public void appendData(DATA data, ProgressMonitor monitor) {
        if (excelWriter == null) {
            throw new ExcelExporterException(
                    "Cant append data, you should invoke start(outputFile) method first. Invoke stop() to complete");
        }

        writeRows(excelWriter, data, monitor);
    }

    /**
     * Append rows to current file. Invoke start(File outpufile) method first
     *
     */
    public void appendData(DATA data) {
        appendData(data, null);
    }

    public List<ExporterColumn<T>> getColumns() {
        return columns;
    }

    protected abstract void writeRows(ExcelFileWriter writer, DATA data, ProgressMonitor monitor);

    protected ExcelFileWriter getExcelWriter() {
        return excelWriter;
    }

    public EnumValueType getDefaultEnumValueType() {
        return defaultEnumValueType;
    }

    public void setDefaultEnumValueType(EnumValueType defaultEnumValueType) {
        this.defaultEnumValueType = defaultEnumValueType;
    }
}
