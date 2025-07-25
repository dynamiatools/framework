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
import tools.dynamia.commons.logger.LoggingService;

import java.time.format.TextStyle;

public class Month implements Converter<Object, Object, Component> {


    @Override
    public Object coerceToUi(Object val, Component component, BindContext ctx) {

        try {
            if (val instanceof Number) {
                return java.time.Month.of(((Number) val).intValue()).getDisplayName(TextStyle.FULL, Messages.getDefaultLocale()).toUpperCase();
            } else if (val instanceof Month) {
                return ((java.time.Month) val).getValue();
            }
        } catch (Exception e) {
            LoggingService.get(Month.class).error("Error converting month", e);
        }

        return null;
    }

    @Override
    public Object coerceToBean(Object val, Component component, BindContext ctx) {
        if (val instanceof java.time.Month) {
            return ((java.time.Month) val).getValue();
        } else if (val instanceof String) {
            for (java.time.Month month : java.time.Month.values()) {
                if (val.equals(month.getDisplayName(TextStyle.FULL, Messages.getDefaultLocale()).toUpperCase())) {
                    return month.getValue();
                }
            }
        }

        return null;
    }

}
