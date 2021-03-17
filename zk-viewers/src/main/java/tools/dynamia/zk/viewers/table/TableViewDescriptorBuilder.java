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
package tools.dynamia.zk.viewers.table;

import tools.dynamia.viewers.FieldBuilder;
import tools.dynamia.viewers.ViewCustomizer;
import tools.dynamia.viewers.ViewDescriptorBuilder;
import tools.dynamia.viewers.impl.DefaultViewDescriptor;
import tools.dynamia.viewers.util.Viewers;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Mario Serrano Leones
 */
public class TableViewDescriptorBuilder extends ViewDescriptorBuilder {

    public static TableViewDescriptorBuilder tableViewDescriptor(String id) {
        TableViewDescriptorBuilder builder = new TableViewDescriptorBuilder();
        builder.descriptor = new DefaultViewDescriptor();
        builder.descriptor.setId(id);
        builder.descriptor.setViewTypeName("table");
        return builder;
    }

    public static TableViewDescriptorBuilder tableViewDescriptor(Class beanClass) {
        return tableViewDescriptor(beanClass, true);
    }

    public static TableViewDescriptorBuilder tableViewDescriptor(Class beanClass, boolean autofields) {
        TableViewDescriptorBuilder builder = new TableViewDescriptorBuilder();
        builder.descriptor = new DefaultViewDescriptor(beanClass, "table", autofields);
        return builder;
    }

    public static ColumnBuilder column(String name) {
        return new ColumnBuilder(name);
    }

    public static HeaderBuilder h() {
        return new HeaderBuilder();
    }

    public static FooterBuilder f() {
        return new FooterBuilder();
    }

    public TableViewDescriptorBuilder orderBy(String... columnName) {
        if (columnName != null && columnName.length > 0) {
            params(Viewers.PARAM_ORDER_BY, String.join(",", columnName));
        }
        return this;
    }

    public TableViewDescriptorBuilder frozenColumns(String... columnName) {
        if (columnName != null && columnName.length > 0) {
            params(Viewers.PARAM_FROZEN_COLUMNS, String.join(",", columnName));
        }
        return this;
    }

    public TableViewDescriptorBuilder itemRenderer(String className) {
        params(Viewers.PARAM_ITEM_RENDERER, className);
        return this;
    }

    public TableViewDescriptorBuilder itemRenderer(Class<? extends TableViewRowRenderer> itemRendererClass) {
        params(Viewers.PARAM_ITEM_RENDERER, itemRendererClass.getName());
        return this;
    }

    @Override
    public TableViewDescriptorBuilder fields(FieldBuilder... fields) {
        return (TableViewDescriptorBuilder) super.fields(fields);
    }

    @Override
    public TableViewDescriptorBuilder layout(Object... keyValue) {
        return (TableViewDescriptorBuilder) super.layout(keyValue);
    }

    @Override
    public TableViewDescriptorBuilder params(Object... keyValue) {
        return (TableViewDescriptorBuilder) super.params(keyValue);
    }

    @Override
    public TableViewDescriptorBuilder hidden(String... fields) {
        return (TableViewDescriptorBuilder) super.hidden(fields);
    }

    @Override
    public TableViewDescriptorBuilder sortFields(String... fields) {
        return (TableViewDescriptorBuilder) super.sortFields(fields);
    }

    @Override
    public TableViewDescriptorBuilder customizer(Class<? extends ViewCustomizer> customizer) {
        return (TableViewDescriptorBuilder) super.customizer(customizer);
    }

    @Override
    public TableViewDescriptorBuilder id(String id) {
        return (TableViewDescriptorBuilder) super.id(id);
    }

    public static class ColumnBuilder extends FieldBuilder {

        public ColumnBuilder(String name) {
            super(name);
        }

        @Override
        public ColumnBuilder ignoreBindings() {
            return (ColumnBuilder) super.ignoreBindings();
        }

        @Override
        public ColumnBuilder params(Object... keyValue) {
            return (ColumnBuilder) super.params(keyValue);
        }

        @Override
        public ColumnBuilder fieldClass(Class fieldClass) {
            return (ColumnBuilder) super.fieldClass(fieldClass);
        }

        @Override
        public ColumnBuilder componentCustomizer(String customizer) {
            return (ColumnBuilder) super.componentCustomizer(customizer);
        }

        @Override
        public ColumnBuilder component(String component) {
            return (ColumnBuilder) super.component(component);
        }

        @Override
        public ColumnBuilder description(String description) {
            return (ColumnBuilder) super.description(description);
        }

        @Override
        public ColumnBuilder label(String label) {
            return (ColumnBuilder) super.label(label);
        }

        public ColumnBuilder header(HeaderBuilder header) {
            header.init(this);
            return this;
        }

        public ColumnBuilder footer(FooterBuilder footer) {
            footer.init(this);
            return this;
        }

        public ColumnBuilder style(String css) {
            params(Viewers.PARAM_STYLE, css);
            return this;
        }

        public ColumnBuilder styleClass(String cssClass) {
            params(Viewers.PARAM_STYLE_CLASS, cssClass);
            return this;
        }
    }

    public static class HeaderBuilder {

        private final Map<String, Object> params = new HashMap<>();

        private void init(ColumnBuilder column) {
            column.params(Viewers.PARAM_HEADER, params);
        }

        public HeaderBuilder align(String align) {
            params.put(Viewers.PARAM_ALIGN, align);
            return this;
        }

        public HeaderBuilder width(String width) {
            params.put(Viewers.PARAM_WIDTH, width);
            return this;
        }
    }

    public static class FooterBuilder {

        private final Map<String, Object> params = new HashMap<>();

        private void init(ColumnBuilder column) {
            column.params(Viewers.PARAM_FOOTER, params);
        }

        public FooterBuilder function(String name) {
            params.put(Viewers.PARAM_FUNCTION, name);
            return this;
        }

        public FooterBuilder functionConverter(String converter) {
            params.put(Viewers.PARAM_FUNCTION_CONVERTER, converter);
            return this;
        }

        public FooterBuilder functionConverter(Class converterClass) {
            params.put(Viewers.PARAM_FUNCTION_CONVERTER, converterClass.getName());
            return this;
        }
    }

}
