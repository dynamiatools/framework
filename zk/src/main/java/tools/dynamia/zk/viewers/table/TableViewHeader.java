
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

import org.zkoss.zul.Listheader;
import tools.dynamia.viewers.Field;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class TableViewHeader extends Listheader {

    private TableView tableView;
    private Field field;

    public TableViewHeader() {
    }

    public TableViewHeader(TableView tableView) {
        this.tableView = tableView;
    }

    public TableViewHeader(TableView tableView, String label) {
        super(label);
        this.tableView = tableView;
    }

    public TableViewHeader(TableView tableView, String label, String src) {
        super(label, src);
        this.tableView = tableView;
    }

    public TableViewHeader(TableView tableView, String label, String src, String width) {
        super(label, src, width);
        this.tableView = tableView;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public TableView getTableView() {
        return tableView;
    }

}
