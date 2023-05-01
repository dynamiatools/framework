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

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import tools.dynamia.domain.query.DataPaginator;
import tools.dynamia.domain.query.DataPaginatorPagedListDataSource;

import java.util.ArrayList;
import java.util.List;

public abstract class ExcelPagedListDatasource<T> extends DataPaginatorPagedListDataSource<T> {

    private final Sheet sheet;
    private final boolean containsHeaders;

    /**
     * POI Excel Sheet, default page size is 100
     *
     */
    public ExcelPagedListDatasource(Sheet sheet) {
        this(sheet, 100);
    }

    /**
     * POI Excel sheet to paginate
     *
     */
    public ExcelPagedListDatasource(Sheet sheet, int pageSize) {
        this(sheet, pageSize, true);
    }

    /**
     *
     */
    public ExcelPagedListDatasource(Sheet sheet, int pageSize, boolean containsHeaders) {
        super(new DataPaginator(pageSize));
        this.sheet = sheet;
        this.containsHeaders = containsHeaders;
        getDataPaginator().setTotalSize(sheet.getLastRowNum() + 1);
        if (containsHeaders) {
            getDataPaginator().setTotalSize(getTotalSize() - 1);
        }
    }

    @Override
    public List<T> loadActivePageData() {
        List<T> pageData = new ArrayList<>();
        int from = getDataPaginator().getFirstResult();

        if (containsHeaders) {
            from = from + 1;
        }

        int to = from + getPageSize() - 1;

        logger.info("Parsing rows " + from + " - " + to);
        for (int i = from; i <= to; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                T data = parseRowData(row);
                pageData.add(data);
            }
        }

        return pageData;
    }

    public abstract T parseRowData(Row row);
}
