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
package tools.dynamia.zk.actions;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionEventBuilder;
import tools.dynamia.actions.Actions;
import tools.dynamia.commons.LocalDateTimeRange;
import tools.dynamia.commons.Messages;
import tools.dynamia.zk.ui.LocalDateRangebox;
import tools.dynamia.zk.ui.LocalDateTimeRangebox;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

/**
 * @author Mario A. Serrano Leones
 */
public class LocalDateTimeboxRangeActionRenderer extends ZKActionRenderer<Component> {

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String format;
    private Locale locale;

    public LocalDateTimeboxRangeActionRenderer() {
    }

    public LocalDateTimeboxRangeActionRenderer(LocalDateTime startDate, LocalDateTime endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public Component render(final Action action, final ActionEventBuilder actionEventBuilder) {
        final LocalDateTimeRangebox dateRangebox = new LocalDateTimeRangebox();
        if (startDate != null && endDate != null) {
            dateRangebox.setValue(new LocalDateTimeRange(startDate, endDate));
        }
        dateRangebox.setTooltiptext(action.getLocalizedDescription(Messages.getDefaultLocale()));
        dateRangebox.addEventListener(Events.ON_CHANGE, event -> fireActionEvent(dateRangebox, action, actionEventBuilder));

        super.configureProperties(dateRangebox, action);

        if (format != null) {
            dateRangebox.setFormat(format);
        }

        if (locale != null) {
            dateRangebox.setLocale(locale);
        }

        return dateRangebox;
    }

    private void fireActionEvent(LocalDateTimeRangebox dateRangebox, Action action, ActionEventBuilder
            actionEventBuilder) {
        var dateRange = dateRangebox.getValue();

        if (!dateRange.isNull()) {
            Actions.run(action, actionEventBuilder, this, dateRange,
                    Map.of("minDate", dateRange.getStartDateTime(),
                            "maxDate", dateRange.getEndDateTime(),
                            "startDateTime", dateRange.getStartDateTime(),
                            "endDateTime", dateRange.getEndDateTime(),
                            "action", action));
        }
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
