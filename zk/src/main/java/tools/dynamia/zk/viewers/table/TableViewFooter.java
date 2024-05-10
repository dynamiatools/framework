
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

package tools.dynamia.zk.viewers.table;

import org.zkoss.bind.Converter;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listfooter;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.domain.fx.FunctionProvider;
import tools.dynamia.io.converters.Converters;
import tools.dynamia.viewers.Field;

/**
 * @author Mario A. Serrano Leones
 */
public class TableViewFooter extends Listfooter implements FunctionProvider {

    private TableView tableView;
    private Field field;
    private String functionConverter;
    private String function;
    private Object value;

    public TableViewFooter() {
    }

    public TableViewFooter(TableView tableView) {
        this.tableView = tableView;
    }

    public TableViewFooter(TableView tableView, Field field) {
        this.tableView = tableView;
        this.field = field;
    }

    public TableViewFooter(TableView tableView, String label) {
        super(label);
        this.tableView = tableView;
    }

    public TableViewFooter(TableView tableView, String label, String src) {
        super(label, src);
        this.tableView = tableView;
    }

    public void setFunction(final String function) {
        this.function = function;
    }

    public void setValue(Object value) {
        clear();
        this.value = value;
        if (value != null) {
            Label footerValue = new Label();
            String resultText = Converters.convert(value);
            if (functionConverter != null) {
                Converter converter = BeanUtils.newInstance(functionConverter);
                //noinspection unchecked
                resultText = (String) converter.coerceToUi(value, footerValue, null);
            }
            footerValue.setValue(resultText);
            appendChild(footerValue);
        }
    }

    public Object getValue() {
        return value;
    }

    public void setFunctionConverter(String functionConverter) {
        this.functionConverter = functionConverter;
    }

    public String getFunctionConverter() {
        return functionConverter;
    }

    public Field getField() {
        return field;
    }

    @Override
    public String getFunction() {
        return function;
    }

    public void clear() {
        this.value = null;
        try {
            getChildren().clear();
        }catch (Exception e){
            //ignore
        }
    }

    @Override
    public String getName() {
        if (field != null) {
            return field.getName();
        } else {
            return null;
        }
    }
}
