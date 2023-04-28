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

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ExcelFileWriter {

    private static final LoggingService LOGGER = new SLF4JLoggingService(ExcelFileWriter.class);

    private final File file;
    private Workbook workbook;
    private Sheet sheet;
    private int lastRowNum;
    private int lastColNum;
    private Row lastRow;
    private boolean showCellBorders;
    private CellStyle borderStyle;
    private CellStyle dateStyle;
    private CellStyle mixStyle;
    private final Map<String, CellStyle> CACHE = new HashMap<>();


    public ExcelFileWriter(File file) {
        super();
        this.file = file;
        init();
    }

    private void init() {
        workbook = new SXSSFWorkbook(500);
        sheet = workbook.createSheet();

        newRow();
        createStyles();
    }

    private void createStyles() {
        borderStyle = workbook.createCellStyle();
        borderStyle.setBorderBottom(BorderStyle.THIN);
        borderStyle.setBorderLeft(BorderStyle.THIN);
        borderStyle.setBorderRight(BorderStyle.THIN);
        borderStyle.setBorderTop(BorderStyle.THIN);

        DataFormat poiFormat = workbook.createDataFormat();
        String format = DateFormatConverter.getJavaDatePattern(DateFormat.MEDIUM, Locale.getDefault());
        dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(poiFormat.getFormat(format));

        mixStyle = workbook.createCellStyle();
        mixStyle.cloneStyleFrom(borderStyle);
        mixStyle.cloneStyleFrom(dateStyle);

        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(font);

    }

    public ExcelFileWriter newRow() {
        lastRow = sheet.createRow(lastRowNum);
        lastColNum = 0;
        lastRowNum++;
        return this;
    }

    public ExcelFileWriter addCell(Object value, String formatPattern) {

        Cell cell = lastRow.createCell(lastColNum);
        CellStyle style = null;
        lastColNum++;
        if (formatPattern != null && !formatPattern.isEmpty()) {
            if (value instanceof Date) {
                value = new SimpleDateFormat(formatPattern).format(value);
            } else if (value instanceof Number) {
                value = new DecimalFormat(formatPattern).format(value);
            }
        }

        if (value != null) {
            if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else if (value instanceof Date) {
                cell.setCellValue((Date) value);
                if (isShowCellBorders()) {
                    style = mixStyle;
                } else {
                    style = dateStyle;
                }

            } else if (value instanceof Boolean) {
                cell.setCellValue((Boolean) value);
            } else {
                try {
                    cell.setCellValue(value.toString());
                } catch (Exception e) {
                    cell.setCellValue("");
                    LOGGER.error("Error writing cell. Using empty string as value", e);
                }
            }
        }
        if (isShowCellBorders() && style == null) {
            style = borderStyle;
        }

        if (style != null) {
            cell.setCellStyle(style);
        }
        return this;

    }

    public ExcelFileWriter addCell(Object value) {
        return addCell(value, null);
    }

    public void write() throws IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            workbook.write(out);
        }
    }

    public void close() {
        try {
            workbook.close();
        } catch (IOException e) {
            throw new ExcelExporterException("Error closing excel file", e);
        }
    }

    public boolean isShowCellBorders() {
        return showCellBorders;
    }

    public void setShowCellBorders(boolean showCellBorders) {
        this.showCellBorders = showCellBorders;
    }

}
