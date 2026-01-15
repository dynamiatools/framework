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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to parse and generate a new excel file using an excel template. Add variables using key as coordinates.
 * Like addVar("A1","Name")
 */
public class ImportExcelTemplate {


    private InputStream excelTemplate;
    private Map<String, Object> variables = new HashMap<>();

    public ImportExcelTemplate(InputStream excelTemplate) {
        this.excelTemplate = excelTemplate;
    }

    public ImportExcelTemplate(File excelTemplate) {
        try {
            this.excelTemplate = new FileInputStream(excelTemplate);
        } catch (FileNotFoundException e) {
            throw new ImportOperationException("Invalid file", e);
        }
    }

    public ImportExcelTemplate(URL url) {
        try {
            this.excelTemplate = url.openStream();
        } catch (IOException e) {
            throw new ImportOperationException("Error opening stream from URL", e);
        }
    }

    public void addVar(String cellName, Object value) {
        variables.put(cellName, value);
    }

    public void addVars(Map<String, Object> vars) {
        variables.putAll(vars);
    }

    public File parse(String outputname) throws IOException {

        Workbook workbook = WorkbookFactory.create(excelTemplate);
        Sheet sheet = workbook.getSheetAt(0);

        variables.forEach((k, v) -> {
            Cell cell = ImportUtils.findCellByCoordinate(sheet, k);
            ImportUtils.setCellValue(cell, v);
        });

        File outfile = File.createTempFile(outputname, ".xlsx");
        workbook.write(new FileOutputStream(outfile));
        workbook.close();

        return outfile;
    }


}
