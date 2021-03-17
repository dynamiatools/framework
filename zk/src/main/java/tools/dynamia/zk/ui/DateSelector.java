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

package tools.dynamia.zk.ui;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;
import tools.dynamia.commons.ClassMessages;
import tools.dynamia.commons.DateTimeUtils;
import tools.dynamia.web.util.HttpUtils;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.util.ZKUtil;

import java.time.Month;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Date selector component show one combobox for day, other for months and a intbox for year
 */
public class DateSelector extends Div {

    static {
        ComponentAliasIndex.getInstance().add("dateselector", DateSelector.class);
        BindingComponentIndex.getInstance().put("selected", DateSelector.class);
    }

    private final ClassMessages messages = ClassMessages.get(DateSelector.class);
    private Combobox daycombo;
    private Combobox monthcombo;
    private Intbox yearbox;

    private Integer selectedDay;
    private Integer selectedMonth;
    private Integer selectedYear;
    private Date selected;


    public DateSelector() {
        this(null);
    }

    public DateSelector(Date selected) {
        initUI();
        setSelected(selected);
    }

    private void initUI() {
        Layout container = new Hlayout();
        if (HttpUtils.isSmartphone()) {
            container = new Vlayout();
        }

        container.setHflex("1");


        daycombo = new Combobox();
        daycombo.setPlaceholder(messages.get("day"));
        daycombo.setReadonly(true);
        daycombo.addEventListener(Events.ON_SELECT, this::fireListener);
        daycombo.setHflex("1");
        List<Integer> days = IntStream.range(1, 32).boxed().collect(Collectors.toList());
        ZKUtil.fillCombobox(daycombo, days);
        container.appendChild(daycombo);


        monthcombo = new Combobox();
        monthcombo.setPlaceholder(messages.get("month"));
        monthcombo.setReadonly(true);
        monthcombo.setItemRenderer(new MonthItemRenderer(true));
        monthcombo.addEventListener(Events.ON_SELECT, this::fireListener);
        monthcombo.setHflex("1");

        ZKUtil.fillCombobox(monthcombo, Month.values(), true);
        container.appendChild(monthcombo);

        yearbox = new Intbox();
        yearbox.setConstraint("no negative");
        yearbox.setPlaceholder(messages.get("year"));
        yearbox.addEventListener(Events.ON_CHANGE, this::fireListener);
        yearbox.setHflex("1");

        container.appendChild(yearbox);


        appendChild(container);
    }

    private void loadValues() {
        if (selectedDay != null && selectedDay > 0 && selectedDay <= 31) {
            ((AbstractListModel) daycombo.getModel()).addToSelection(selectedDay);
        } else {
            daycombo.setSelectedItem(null);
        }

        if (selectedMonth != null && selectedMonth > 0 && selectedMonth <= 12) {
            ((AbstractListModel) monthcombo.getModel()).addToSelection(Month.of(selectedMonth));
        } else {
            monthcombo.setSelectedItem(null);
        }

        yearbox.setValue(selectedYear);
    }

    private void saveValues() {
        if (daycombo.getSelectedItem() != null) {
            selectedDay = daycombo.getSelectedItem().getValue();
        }

        if (monthcombo.getSelectedItem() != null) {
            selectedMonth = monthcombo.getSelectedItem().getValue();
        }

        selectedYear = yearbox.getValue();


    }

    private void fireListener(Event evt) {
        saveValues();
        if (selectedDay != null && selectedMonth != null && selectedYear != null) {
            Date newSelected = DateTimeUtils.createDate(selectedYear, selectedMonth, selectedDay);
            if (!Objects.equals(selected, newSelected)) {
                this.selected = newSelected;
                Events.postEvent(new Event(Events.ON_SELECT, this, selected));
            }
        }
    }


    public Integer getSelectedDay() {
        return selectedDay;
    }

    public void setSelectedDay(Integer selectedDay) {
        this.selectedDay = selectedDay;
    }

    public Integer getSelectedMonth() {
        return selectedMonth;
    }

    public void setSelectedMonth(Integer selectedMonth) {
        this.selectedMonth = selectedMonth;
    }

    public Integer getSelectedYear() {
        return selectedYear;
    }

    public void setSelectedYear(Integer selectedYear) {
        this.selectedYear = selectedYear;
    }

    public Date getSelected() {
        saveValues();
        return selected;
    }

    public void setSelected(Date selected) {
        this.selected = selected;
        selectedDay = null;
        selectedMonth = null;
        selectedYear = null;
        if (selected != null) {
            selectedDay = DateTimeUtils.getDay(selected);
            selectedMonth = DateTimeUtils.getMonth(selected);
            selectedYear = DateTimeUtils.getYear(selected);
            loadValues();
        }
    }

    public Combobox getDaycombo() {
        return daycombo;
    }

    public Combobox getMonthcombo() {
        return monthcombo;
    }

    public Intbox getYearbox() {
        return yearbox;
    }
}
