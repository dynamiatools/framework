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


package tools.dynamia.zk.viewers;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.*;
import tools.dynamia.commons.DateRange;
import tools.dynamia.commons.DayOfWeek;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.commons.collect.ArrayListMultiMap;
import tools.dynamia.commons.collect.ListMultiMap;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.FieldCustomizer;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.ui.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.Locale;

/**
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@Provider
public class DefaultFieldCustomizer implements FieldCustomizer {

    private final static ListMultiMap<Class<? extends Component>, Class> INDEX = new ArrayListMultiMap<>();

    static {
        INDEX.put(Intbox.class, int.class, Integer.class);
        INDEX.put(Longbox.class, long.class, Long.class);
        INDEX.put(Doublebox.class, double.class, Double.class);
        INDEX.put(Checkbox.class, boolean.class, Boolean.class);
        INDEX.put(Textbox.class, String.class);
        INDEX.put(Datebox.class, Date.class, LocalDate.class, Instant.class);
        INDEX.put(DecimalboxCalculator.class, BigDecimal.class);
        INDEX.put(Combobox.class, Enum.class);
        INDEX.put(DayWeekbox.class, DayOfWeek.class);
        INDEX.put(DateRangebox.class, DateRange.class);
        INDEX.put(Timebox.class, LocalTime.class);
        INDEX.put(LocaleCombobox.class, Locale.class);
    }

    @Override
    public void customize(String viewTypeName, Field field) {

        if (field.getComponent() != null && !field.getComponent().isEmpty()) {
            field.setComponentClass(ComponentAliasIndex.getInstance().get(field.getComponent()));
        }

        if (field.getLabel() == null || field.getLabel().isEmpty()) {
            field.setLabel(StringUtils.capitalize(StringUtils.addSpaceBetweenWords(field.getName())));
        }

        if (field.getComponentClass() == null && field.isVisible()) {

            if (viewTypeName.equals("form") || field.getParams().containsKey(Viewers.PARAM_WRITABLE)) {
                configureForm(field);
            } else if (field.getFieldClass() == Boolean.class || field.getFieldClass() == boolean.class) {
                field.setComponentClass(Checkbox.class);
                field.set("disabled", true);
            } else {
                field.setComponentClass(Label.class);
            }

            if (field.getComponentClass() != null && field.getComponent() == null) {
                field.setComponent(ComponentAliasIndex.getInstance().getAlias(field.getComponentClass()));
            }
        }

        if (field.getComponentClass() == Combobox.class && field.getParams().get("readonly") == null) {
            field.addParam("readonly", true);
        }

    }

    private Class<? extends Component> getComponentForSuperClass(Class superClass) {
        if (superClass == null) {
            return null;
        }

        Class component = INDEX.getKey(superClass);
        if (component == null) {
            component = getComponentForSuperClass(superClass.getSuperclass());
        }

        return component;
    }

    private Class<? extends Component> getComponentClass(Class fieldClass) {
        return INDEX.getKey(fieldClass);
    }

    private void configureForm(Field field) {
        if (field.getFieldClass() != null) {
            Class componentClass = null;

            if (field.getFieldClass().equals(String.class) && (field.getName().equalsIgnoreCase("color")
                    || field.getName().toLowerCase().endsWith(".color"))) {
                componentClass = Colorbox.class;
            }

            if (componentClass == null) {
                componentClass = getComponentClass(field.getFieldClass());
            }

            if (componentClass == null) {
                componentClass = getComponentForSuperClass(field.getFieldClass().getSuperclass());
            }
            field.setComponentClass(componentClass);

            if ((field.getFieldClass() != null && field.getFieldClass().isEnum())
                    || (field.getPropertyInfo() != null && field.getPropertyInfo().isEnum())) {
                field.setComponentCustomizer(EnumComponentCustomizer.class.getName());
            }

        }
    }
}
