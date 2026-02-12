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
package tools.dynamia.zk.ui;

import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.Combobox;
import tools.dynamia.commons.BeanSorter;
import tools.dynamia.navigation.NavigationManager;
import tools.dynamia.navigation.Page;
import tools.dynamia.zk.util.ZKUtil;

import java.util.List;

/**
 *
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class PageFinder extends Combobox {

    /**
     *
     */
    private static final long serialVersionUID = 6551599841117590635L;

    public PageFinder() {
        configure();
    }

    public PageFinder(String value) {
        super(value); // it invokes setValue(), which inits the child comboitems
        configure();
    }

    private void configure() {
        setButtonVisible(false);
        refresh("");
    }

    public void onChanging(InputEvent event) {
        refresh(event.getValue());
    }

    public void onSelect(SelectEvent event) {
        Page page = getSelectedItem().getValue();
        if (page != null) {
            NavigationManager.getCurrent().setCurrentPage(page);
        }
    }

    @Override
    public void setValue(String value) throws WrongValueException {
        super.setValue(value);
        refresh(value);
    }

    private void refresh(String val) {

        if (val != null && !val.isEmpty() && val.length() > 2) {
            List<Page> pages = NavigationManager.getCurrent().findPagesByName(val);
            BeanSorter sorter = new BeanSorter("name");
            sorter.setAscending(true);
            sorter.sort(pages);
            ZKUtil.fillCombobox(this, pages, false);
            open();
        } else {
            getChildren().clear();
            close();
        }
    }
}
