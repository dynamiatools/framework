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
package tools.dynamia.zk.reports;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Column;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Row;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.reports.excel.ExcelFileWriter;

import java.io.File;
import java.io.IOException;

public class GridExporter {

    public static void export(Grid grid, String fileName) throws IOException {
        File outfile = File.createTempFile(fileName + "_", ".xlsx");
        ExcelFileWriter efw = new ExcelFileWriter(outfile);
        efw.setShowCellBorders(true);
        exportHeaders(grid, efw);
        exportRows(grid, efw);
        efw.write();
        Filedownload.save(outfile, "application/excel");

    }

    private static void exportRows(Grid grid, ExcelFileWriter efw) {
        for (Component cmp : grid.getRows().getChildren()) {
            if (cmp instanceof Row) {
                efw.newRow();

                for (Component rowcell : cmp.getChildren()) {
                    Object value = getValue(rowcell);
                    efw.addCell(value);
                }
            }
        }

    }

    private static Object getValue(Component rowcell) {
        try {
            return BeanUtils.invokeGetMethod(rowcell, "value");
        } catch (Exception e) {
            try {
                return BeanUtils.invokeGetMethod(rowcell, "label");
            } catch (Exception e2) {
            }

        }
        return "";
    }

    private static void exportHeaders(Grid grid, ExcelFileWriter efw) {
        efw.newRow();
        for (Component cmp : grid.getColumns().getChildren()) {
            if (cmp instanceof Column) {
                Column column = (Column) cmp;
                efw.addCell(column.getLabel());
            }
        }
    }

    private GridExporter() {
    }
}
