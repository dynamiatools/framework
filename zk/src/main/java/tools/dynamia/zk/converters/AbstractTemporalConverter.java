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
import tools.dynamia.commons.Messages;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * @author Mario A. Serrano Leones
 */

public abstract class AbstractTemporalConverter implements Converter<Object, Object, Component> {

    @Override
    public Object coerceToUi(Object val, Component comp, BindContext ctx) {

        if (val instanceof TemporalAccessor date) {
            return buildFormatter().format(date);
        }
        return null;
    }

    @Override
    public Object coerceToBean(Object val, Component comp, BindContext ctx) {
        var formatter = buildFormatter();
        return Util.coerceToBean(val, formatter);
    }

    private DateTimeFormatter buildFormatter() {
        return DateTimeFormatter.ofPattern(getPattern(), Messages.getDefaultLocale());
    }

    public String format(TemporalAccessor temporalAccessor) {
        return buildFormatter().format(temporalAccessor);
    }


    public abstract String getPattern();

}
