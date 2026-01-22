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

import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.commons.reflect.ReflectionException;
import tools.dynamia.integration.ProgressMonitor;
import tools.dynamia.reports.EnumValueType;
import tools.dynamia.reports.ExporterColumn;
import tools.dynamia.reports.ExporterUtils;

import java.util.Collection;
import java.util.Map;

/**
 * @author Mario A. Serrano Leones
 */
public class ExcelCollectionExporter<T> extends AbstractExcelExporter<T, Collection<T>> {

    private final LoggingService logger = new SLF4JLoggingService(ExcelCollectionExporter.class);
    private String trueValue = "true";
    private String falseValue = "false";

    @Override
    protected void writeRows(ExcelFileWriter writer, Collection<T> data, ProgressMonitor monitor) {
        int row = 0;

        if (monitor != null) {
            monitor.setMax(data.size());
        }

        for (T object : data) {
            writer.newRow();
            for (ExporterColumn<T> col : getColumns()) {
                Object value = "";

                try {
                    if (col.getFieldLoader() != null) {
                        value = col.getFieldLoader().load(col.getName(), object);
                    } else if (object instanceof Map) {
                        value = ((Map) object).get(col.getName());
                    } else {
                        if (col.getColumnClass() != null && col.getColumnClass().equals(boolean.class)) {
                            value = ObjectOperations.invokeBooleanGetMethod(object, col.getName());
                        } else {
                            try {
                                value = ObjectOperations.invokeGetMethod(object, col.getName());
                                value = ExporterUtils.checkAndLoadEntityReferenceValue(col, value);
                            } catch (ReflectionException e) {
                                if (col.getDefaultValue() != null) {
                                    value = col.getDefaultValue();
                                } else {
                                    throw e;
                                }
                            }
                        }
                    }

                    if (value instanceof Enum enumValue) {
                        EnumValueType enumValueType = col.getEnumValueType() != null ? col.getEnumValueType() : getDefaultEnumValueType();
                        if (enumValueType != null) {
                            value = enumValueType == EnumValueType.NAME ? enumValue.name() : enumValue.ordinal();
                        }
                    }

                    if (value instanceof Boolean) {
                        value = (Boolean) value ? trueValue : falseValue;
                    }

                } catch (Exception e) {
                    logger.error("Error exporting to excel. Column: " + col, e);
                }
                writer.addCell(value, col.getFormatPattern());
            }
            row++;
            if (monitor != null) {
                monitor.setCurrent(row);
                if (monitor.isStopped()) {
                    return;
                }
            }
        }

    }

    public void setBooleanValues(String trueValue, String falseValue) {
        this.trueValue = trueValue;
        this.falseValue = falseValue;
    }
}
