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
package tools.dynamia.zk.converters;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;
import tools.dynamia.commons.DateTimeUtils;
import tools.dynamia.commons.Messages;
import tools.dynamia.commons.SimpleCache;
import tools.dynamia.commons.logger.LoggingService;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Locale;

/**
 * @author Mario A. Serrano Leones
 */

public abstract class AbstractTemporalConverter implements Converter<Object, Object, Component> {

    private static final SimpleCache<String, DateTimeFormatter> FORMATTER_SIMPLE_CACHE = new SimpleCache<>();
    private final LoggingService logger = LoggingService.get(getClass());

    @Override
    public Object coerceToUi(Object val, Component comp, BindContext ctx) {

        if (val instanceof TemporalAccessor temporalAccessor) {
            return format(temporalAccessor);
        } else if (val instanceof Date date) {
            var instant = DateTimeUtils.toInstant(date);
            if (instant != null) {
                return format(instant.atZone(Messages.getDefaultTimeZone()));
            }
        }
        return null;
    }

    @Override
    public Object coerceToBean(Object val, Component comp, BindContext ctx) {
        var formatter = loadFormatter();
        return Util.coerceToBean(val, formatter);
    }

    private DateTimeFormatter loadFormatter() {
        Locale locale = Messages.getDefaultLocale();
        String pattern = getPattern();
        FormatStyle dateStyle = getDateStyle();
        FormatStyle timeStyle = getTimeStyle();
        String key = pattern + "#" + locale.toString() + "#" + dateStyle + "#" + timeStyle;
        return FORMATTER_SIMPLE_CACHE.getOrLoad(key, s -> buildFormatter(pattern, locale, dateStyle, timeStyle));
    }

    protected DateTimeFormatter buildFormatter(String pattern, Locale locale, FormatStyle dateStyle, FormatStyle timeStyle) {
        if (pattern != null && !pattern.isBlank()) {
            return DateTimeFormatter.ofPattern(pattern, locale);
        } else if (dateStyle != null && timeStyle != null) {
            return DateTimeFormatter.ofLocalizedDateTime(dateStyle, timeStyle).withLocale(locale);
        } else if (dateStyle != null && timeStyle == null) {
            return DateTimeFormatter.ofLocalizedDate(dateStyle).withLocale(locale);
        } else if (dateStyle == null && timeStyle != null) {
            return DateTimeFormatter.ofLocalizedTime(timeStyle).withLocale(locale);
        } else {
            return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withLocale(locale);
        }
    }

    public String format(TemporalAccessor temporalAccessor) {
        logger.debug("Formatting:" + temporalAccessor.getClass() + " " + temporalAccessor + " with pattern: " + getPattern());
        var formatter = loadFormatter();
        var result = formatter.format(temporalAccessor);
        return result;
    }


    protected String getPattern() {
        return null;
    }

    protected FormatStyle getDateStyle() {
        return null;
    }

    protected FormatStyle getTimeStyle() {
        return null;
    }

}
