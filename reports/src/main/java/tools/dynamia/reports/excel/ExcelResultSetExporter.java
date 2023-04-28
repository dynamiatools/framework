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
import tools.dynamia.reports.ExporterColumn;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class ExcelResultSetExporter extends AbstractExcelExporter<Object, ResultSet> {

    public ExcelResultSetExporter() {

    }

    public void columnsFromMetadata(ResultSet resultSet) throws SQLException {
        getColumns().clear();
        ResultSetMetaData metaData = resultSet.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String column = metaData.getColumnLabel(i);
            addColumn(column);
        }
    }

    public void appendCurrentRow(ResultSet data) {
        try {
            writeRow(getExcelWriter(), data);
        } catch (SQLException e) {
            throw new ExcelExporterException("Error exporting current row", e);
        }
    }

    @Override
    protected void writeRows(ExcelFileWriter writer, ResultSet data, ProgressMonitor monitor) {
        int row = 0;

        if (monitor != null) {
            try {
                if (data.last()) {
                    monitor.setMax(data.getRow());
                    data.beforeFirst();
                }
            } catch (SQLException e) {

                e.printStackTrace();
            }
        }

        try {
            while (data.next()) {
                writeRow(writer, data);
                row++;
                if (monitor != null) {
                    monitor.setCurrent(row);
                    if (monitor.isStopped()) {
                        return;
                    }
                }
            }
        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    private void writeRow(ExcelFileWriter writer, ResultSet data) throws SQLException {
        writer.newRow();
        for (ExporterColumn col : getColumns()) {
            Object value = data.getObject(col.getName());
            writer.addCell(value, col.getFormatPattern());
        }
    }

}
