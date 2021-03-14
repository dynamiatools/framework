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
package tools.dynamia.reports.excel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Test;
import tools.dynamia.commons.collect.PagedList;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ExcelPagedListDatasourceTest {

    @Test
    public void test() throws Exception {
        Workbook workbook = WorkbookFactory.create(getClass().getResourceAsStream("/1000RowsExcel.xlsx"));
        Sheet firstSheet = workbook.getSheetAt(0);

        int pagedSize = 20;
        int expectedTotalSize = 1000;
        int expectedPages = expectedTotalSize / pagedSize;

        PagedList<String> list = new PagedList<>(new ExcelPagedListDatasource<String>(firstSheet, pagedSize) {

            @Override
            public String parseRowData(Row row) {
                return row.getCell(0).getStringCellValue() + " >>> " + row.getRowNum();
            }
        });

        assertEquals(expectedTotalSize, list.size());
        assertEquals(expectedPages, list.getDataSource().getPageCount());

        List<String> inMemoryList = new ArrayList<>();
        for (String string : list) {
            inMemoryList.add(string);
        }
        assertEquals(inMemoryList.size(), list.size());
    }

}
