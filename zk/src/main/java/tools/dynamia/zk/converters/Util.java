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

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zul.Listcell;
import tools.dynamia.commons.DateTimeUtils;

import java.text.Format;
import java.text.ParseException;

/**
 * @author Mario A. Serrano Leones
 */
public class Util {


    public static void applyStylesClass(Number num, Component comp) {

        if (comp == null || num == null) {
            return;
        }

        if (comp.getParent() != null && comp.getParent() instanceof Listcell) {
            return;
        }

        Object ignoreStyles = comp.getAttribute("ignoreStyles");
        if (ignoreStyles == Boolean.TRUE) {
            return;
        }

        if (comp instanceof HtmlBasedComponent hcomp) {
            if (num.longValue() > 0) {
                hcomp.setSclass("conv-positive");
            } else if (num.longValue() < 0) {
                hcomp.setSclass("conv-negative");
            } else {
                hcomp.setSclass("conv-zero");
            }
        }
    }

    public static void applyStylesClass(java.util.Date date, Component comp) {
        if (comp != null && comp.getAttribute("ignoreStyles") == Boolean.TRUE) {
            return;
        }


        if (comp instanceof HtmlBasedComponent hcomp) {
            if (DateTimeUtils.isFuture(date)) {
                hcomp.setSclass("conv-future");
            } else if (DateTimeUtils.isPast(date)) {
                hcomp.setSclass("conv-past");
            } else {
                hcomp.setSclass("conv-present");
            }
        }
    }

    public static Object coerceToBean(Object val, Format format) {
        if (val != null) {
            try {
                return format.parseObject(val.toString());
            } catch (ParseException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public static String checkConverterClass(String converter) {
        if (converter != null && converter.startsWith("converters.")) {
            converter = "tools.dynamia.zk." + converter;
        }

        return converter;
    }

    private Util() {
    }
}
