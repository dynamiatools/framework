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
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Span;
import tools.dynamia.commons.LocalDateTimeRange;

import java.util.Locale;
import java.util.TimeZone;

public class LocalDateTimeRangebox extends Span {


    private LocalDateTimeRange value;
    private final Datebox start;
    private final Datebox end;


    public LocalDateTimeRangebox() {
        this(null);
    }

    public LocalDateTimeRangebox(LocalDateTimeRange value) {
        super.setStyle("display: inline");
        this.value = value;
        this.start = new Datebox();
        this.end = new Datebox();
        appendChild(start);
        appendChild(end);
        start.addEventListener(Events.ON_CHANGE, this::fireEvent);
        end.addEventListener(Events.ON_CHANGE, this::fireEvent);
        setShowTodayLink(true);
    }

    private void fireEvent(Event e) {
        if (start.getValue() != null && end.getValue() != null) {
            Events.postEvent(Events.ON_CHANGE, this, getValue());
        }
    }

    public LocalDateTimeRange getValue() {
        if (value == null) {
            value = new LocalDateTimeRange();
        }
        value.setStartDateTime(start.getValueInLocalDateTime());
        value.setEndDateTime(end.getValueInLocalDateTime());
        return value;
    }

    public void setValue(LocalDateTimeRange value) {
        this.value = value;
        if (value != null) {
            start.setValueInLocalDateTime(value.getStartDateTime());
            end.setValueInLocalDateTime(value.getEndDateTime());
        }
    }

    public boolean isButtonVisible() {
        return start.isButtonVisible();
    }

    public void setButtonVisible(boolean buttonVisible) {
        start.setButtonVisible(buttonVisible);
        end.setButtonVisible(buttonVisible);
    }

    public void setFormat(String format) throws WrongValueException {
        start.setFormat(format);
        end.setFormat(format);
    }

    public String getRealFormat() {
        return start.getRealFormat();
    }

    public TimeZone getTimeZone() {
        return start.getTimeZone();
    }

    public void setTimeZone(TimeZone tzone) {
        start.setTimeZone(tzone);
        end.setTimeZone(tzone);
    }

    public void setTimeZone(String id) {
        start.setTimeZone(id);
        end.setTimeZone(id);
    }

    public boolean isTimeZonesReadonly() {
        return start.isTimeZonesReadonly();
    }

    public void setTimeZonesReadonly(boolean readonly) {
        start.setTimeZonesReadonly(readonly);
        end.setTimeZonesReadonly(readonly);
    }

    public Locale getLocale() {
        return start.getLocale();
    }

    public void setLocale(Locale locale) {
        start.setLocale(locale);
        end.setLocale(locale);
    }

    public void setLocale(String locale) {
        start.setLocale(locale);
        end.setLocale(locale);
    }

    public void setOpen(boolean open) {
        start.setOpen(open);
        end.setOpen(open);
    }

    public void setConstraint(String constr) {
        start.setConstraint(constr);
        end.setConstraint(constr);
    }

    public boolean getShowTodayLink() {
        return start.getShowTodayLink();
    }

    public void setShowTodayLink(boolean showTodayLink) {
        start.setShowTodayLink(showTodayLink);
        end.setShowTodayLink(showTodayLink);
    }

    public String getTodayLinkLabel() {
        return start.getTodayLinkLabel();
    }

    public void setTodayLinkLabel(String todayLinkLabel) {
        start.setTodayLinkLabel(todayLinkLabel);
        end.setTodayLinkLabel(todayLinkLabel);
    }

    public String getPlaceholder() {
        return start.getPlaceholder();
    }

    public void setPlaceholder(String placeholder) {
        start.setPlaceholder(placeholder);
        end.setPlaceholder(placeholder);
    }

    public void setInplace(boolean inplace) {
        start.setInplace(inplace);
        end.setInplace(inplace);
    }

    public boolean isInplace() {
        return start.isInplace();
    }

    public boolean isDisabled() {
        return start.isDisabled();
    }

    public void setDisabled(boolean disabled) {
        start.setDisabled(disabled);
        end.setDisabled(disabled);
    }

    public Datebox getStart() {
        return start;
    }

    public Datebox getEnd() {
        return end;
    }
}
