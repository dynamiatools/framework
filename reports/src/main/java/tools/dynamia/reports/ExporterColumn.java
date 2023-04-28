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
package tools.dynamia.reports;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ExporterColumn<T> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -2236746949643481534L;
    private String name;
    private String title;
    private String formatPattern;
    private Class columnClass;
    private String entityAlias;
    private Object defaultValue;
    private ExporterFieldLoader<T> fieldLoader;
    private EnumValueType enumValueType;

    public ExporterColumn() {

    }

    public ExporterColumn(String name) {
        super();
        this.name = name;
    }

    public ExporterColumn(String name, String title) {
        super();
        this.name = name;
        this.title = title;
    }

    public ExporterColumn(String name, String title, ExporterFieldLoader<T> fieldLoader) {
        super();
        this.name = name;
        this.title = title;
        this.fieldLoader = fieldLoader;
    }

    public ExporterColumn(String name, String title, String formatPattern) {
        super();
        this.name = name;
        this.title = title;
        this.formatPattern = formatPattern;
    }

    public ExporterColumn(String name, String title, String formatPattern, Class columnClass) {
        this.name = name;
        this.title = title;
        this.formatPattern = formatPattern;
        this.columnClass = columnClass;
    }

    public void setFieldLoader(ExporterFieldLoader<T> fieldLoader) {
        this.fieldLoader = fieldLoader;
    }

    public ExporterFieldLoader<T> getFieldLoader() {
        return fieldLoader;
    }

    public Class getColumnClass() {
        return columnClass;
    }

    public void setColumnClass(Class columnClass) {
        this.columnClass = columnClass;
    }

    public String getEntityAlias() {
        return entityAlias;
    }

    public void setEntityAlias(String entityAlias) {
        this.entityAlias = entityAlias;
    }

    private final Map<String, Object> params = new HashMap<>();

    public void addParam(String name, Object value) {
        params.put(name, value);
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFormatPattern() {
        return formatPattern;
    }

    public void setFormatPattern(String formatPattern) {
        this.formatPattern = formatPattern;
    }

    public boolean isEntityAlias() {
        return entityAlias != null && !entityAlias.isEmpty();
    }

    public EnumValueType getEnumValueType() {
        return enumValueType;
    }

    public void setEnumValueType(EnumValueType enumValueType) {
        this.enumValueType = enumValueType;
    }

    @Override
    public String toString() {
        return name;
    }

    public ExporterColumn<T> value(Object value) {
        this.defaultValue = value;
        return this;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public ExporterColumn entityAlias(String entityAlias) {
        setEntityAlias(entityAlias);
        return this;
    }
}
