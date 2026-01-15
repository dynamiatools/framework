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

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.zkoss.zul.Filedownload;
import tools.dynamia.modules.importer.ImportAction;
import tools.dynamia.reports.ExporterColumn;
import tools.dynamia.reports.excel.ExcelCollectionExporter;
import tools.dynamia.reports.excel.ExcelFileWriter;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.ViewDescriptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class GenerateImportFormatAction extends ImportAction {

    GenerateImportFormatAction(String name) {
        setName(name);
        setImage("down");
        setBackground(".green");
        setColor("white");
    }

    @Override
    public void actionPerformed(Importer importer) {

        try {
            ViewDescriptor descriptor = importer.getTableDescriptor();


            Workbook workbook = WorkbookFactory.create(true);
            Sheet sheet = workbook.createSheet("DATA");

            Row row = sheet.createRow(0);
            Drawing drawing = sheet.createDrawingPatriarch();
            int column = 0;

            Font font = workbook.createFont();
            font.setBold(true);

            Font redFont = workbook.createFont();
            redFont.setBold(true);
            redFont.setColor(Font.COLOR_RED);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(font);

            CellStyle headerRequiredStyle = workbook.createCellStyle();
            headerRequiredStyle.setFont(redFont);


            for (Field field : descriptor.getFields()) {
                Cell cell = row.createCell(column, CellType.STRING);
                cell.setCellValue(field.getLabel());
                cell.setCellStyle(headerStyle);

                if (field.isRequired()) {
                    cell.setCellStyle(headerRequiredStyle);
                }

                if (field.getDescription() != null && !field.getDescription().isEmpty()) {
                    Comment comment = drawing.createCellComment(drawing.createAnchor(100, 1000, 100, 1000, column, row.getRowNum(), column + 3, row.getRowNum() + 3));
                    comment.setString(new XSSFRichTextString(field.getDescription()));
                    cell.setCellComment(comment);
                }

                column++;
            }
            File file = File.createTempFile(importer.getFormatFileName() + "_", ".xlsx");
            workbook.write(new FileOutputStream(file));
            Filedownload.save(file, "application/excel");
            UIMessages.showMessage(file.getName() + " OK");
        } catch (IOException e) {
            UIMessages.showMessage("Error generating format: " + e.getMessage(), MessageType.ERROR);
            log("Error generating format for importer: " + importer.getFormatFileName(), e);
        }

    }

    @Override
    public void processImportedData(Importer importer) {

    }

    @Override
    public boolean isProcesable() {
        return false;
    }
}
