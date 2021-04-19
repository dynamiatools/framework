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
package converters;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;
import tools.dynamia.commons.Messages;

import java.text.NumberFormat;

/**
 * @author Mario A. Serrano Leones
 */

public class CurrencySimple implements Converter<Object, Object, Component> {

    @Override
    public Object coerceToUi(Object val, Component comp, BindContext ctx) {

        if (val instanceof Number) {
            NumberFormat formatter = buildFormatter();
            Util.applyStylesClass((Number) val, comp);
            return formatter.format(val);
        }
        return null;
    }

    @Override
    public Object coerceToBean(Object val, Component comp, BindContext ctx) {
        NumberFormat formatter = buildFormatter();
        return Util.coerceToBean(val, formatter);
    }

    private NumberFormat buildFormatter() {
        var locale = Messages.getDefaultLocale();
        var formatter = NumberFormat.getCurrencyInstance(locale);
        formatter.setMaximumFractionDigits(0);
        return formatter;
    }

    public String format(Number number) {
        return (String) coerceToUi(number, null, null);
    }

}
