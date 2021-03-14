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
package tools.dynamia.zk.actions;

import tools.dynamia.commons.Messages;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Span;
import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.ActionEventBuilder;
import tools.dynamia.commons.DateRange;
import tools.dynamia.commons.MapBuilder;
import tools.dynamia.zk.ui.DateRangebox;

import java.util.Date;
import java.util.Locale;

/**
 * @author Mario A. Serrano Leones
 */
public class DateboxRangeActionRenderer extends ZKActionRenderer<Component> {

    private Date startDate;
    private Date endDate;
    private String format;
    private Locale locale;

    public DateboxRangeActionRenderer() {
    }

    public DateboxRangeActionRenderer(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public Component render(final Action action, final ActionEventBuilder actionEventBuilder) {
        final DateRangebox dateRangebox = new DateRangebox();
        if (startDate != null && endDate != null) {
            dateRangebox.setValue(new DateRange(startDate, endDate));
        }
        dateRangebox.setTooltiptext(action.getLocalizedDescription(Messages.getDefaultLocale()));
        dateRangebox.addEventListener(Events.ON_CHANGE, event -> {
            fireActionEvent(dateRangebox, action, actionEventBuilder);
        });

        super.configureProperties(dateRangebox, action);

        if (format != null) {
            dateRangebox.setFormat(format);
        }

        if (locale != null) {
            dateRangebox.setLocale(locale);
        }

        return dateRangebox;
    }

    private void fireActionEvent(DateRangebox dateRangebox, Action action, ActionEventBuilder
            actionEventBuilder) {
        DateRange dateRange = dateRangebox.getValue();
        ActionEvent evt = actionEventBuilder.buildActionEvent(this, MapBuilder.put("minDate", dateRange.getStartDate(),
                "maxDate", dateRange.getEndDate()));
        evt.setData(dateRange);
        action.actionPerformed(evt);
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
