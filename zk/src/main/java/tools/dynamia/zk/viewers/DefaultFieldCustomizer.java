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
import tools.dynamia.commons.Messages;
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
import java.time.*;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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
        INDEX.put(Datebox.class, Date.class, LocalDate.class, Instant.class, LocalDateTime.class, ZonedDateTime.class);
        INDEX.put(DecimalboxCalculator.class, BigDecimal.class);
        INDEX.put(Combobox.class, Enum.class);
        INDEX.put(DayWeekbox.class, DayOfWeek.class);
        INDEX.put(DateRangebox.class, DateRange.class);
        INDEX.put(Timebox.class, LocalTime.class);
        INDEX.put(LocaleCombobox.class, Locale.class);
        INDEX.put(DurationSelector.class, Duration.class);
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

            if (isForm(viewTypeName) || field.getParams().containsKey(Viewers.PARAM_WRITABLE)) {
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

        if (isForm(viewTypeName) && field.getFieldClass() != null && field.getComponentClass() != null) {
            customizeCombobox(field);
            customizeDateboxBindings(field);
            customizeTimeboxBindings(field);
            customizeDateSelectorBinding(field);
        }

    }

    private static boolean isForm(String viewTypeName) {
        return viewTypeName.equals("form");
    }

    public static void customizeCombobox(Field field) {
        if (field.getComponentClass() == Combobox.class && field.getParams().get("readonly") == null) {
            field.addParam("readonly", true);
        }
    }

    private static Class<? extends Component> getComponentForSuperClass(Class superClass) {
        if (superClass == null) {
            return null;
        }

        Class component = INDEX.getKey(superClass);
        if (component == null) {
            component = getComponentForSuperClass(superClass.getSuperclass());
        }

        return component;
    }

    private static Class<? extends Component> getComponentClass(Class fieldClass) {
        return INDEX.getKey(fieldClass);
    }

    public static void configureForm(Field field) {
        if (field.getFieldClass() == null) {
            return;
        }

        Class componentClass = null;

        if (field.getFieldClass().equals(String.class) && (
                "color".equalsIgnoreCase(field.getName()) ||
                        field.getName().toLowerCase().endsWith(".color"))) {
            componentClass = Colorbox.class;
        } else {
            componentClass = getComponentClass(field.getFieldClass());
            if (componentClass == null) {
                componentClass = getComponentForSuperClass(field.getFieldClass().getSuperclass());
            }
        }

        field.setComponentClass(componentClass);

        if ((field.getFieldClass().isEnum()) ||
                (field.getPropertyInfo() != null && field.getPropertyInfo().isEnum())) {
            field.setComponentCustomizer(EnumComponentCustomizer.class.getName());
        }

        customizeCombobox(field);
        customizeDateboxBindings(field);
        customizeTimeboxBindings(field);
        customizeDateSelectorBinding(field);

    }

    public static void customizeDateSelectorBinding(Field field) {
        if (field.getComponentClass() == DateSelector.class && !field.containsParam(Viewers.PARAM_BINDINGS) && !field.containsParam(Viewers.PARAM_BINDING_ATTRIBUTE) && field.getFieldClass() == LocalDate.class) {
            String attribute = "selectedLocalDate";
            field.addParam(Viewers.PARAM_BINDING_ATTRIBUTE, attribute);
        }
    }

    public static void customizeDateboxBindings(Field field) {
        if (field.getComponentClass() == Datebox.class && !field.containsParam(Viewers.PARAM_BINDINGS) && !field.containsParam(Viewers.PARAM_BINDING_ATTRIBUTE)) {
            String attribute = getDateboxBindingAttribute(field);
            field.addParam(Viewers.PARAM_BINDING_ATTRIBUTE, attribute);
            field.addParam("timeZone", TimeZone.getTimeZone(Messages.getDefaultTimeZone()));
            field.addParam("locale", Messages.getDefaultLocale());
            if (field.getFieldClass() == LocalDateTime.class) {
                field.addParam("format", "medium+medium");
            }
        }
    }

    public static void customizeTimeboxBindings(Field field) {
        if (field.getComponentClass() == Timebox.class && !field.containsParam(Viewers.PARAM_BINDINGS) && !field.containsParam(Viewers.PARAM_BINDING_ATTRIBUTE)) {
            String attribute = getDateboxBindingAttribute(field);
            field.addParam(Viewers.PARAM_BINDING_ATTRIBUTE, attribute);
        }
    }


    public static String getDateboxBindingAttribute(Field field) {
        String bindingProperty = "value";
        if (field.getFieldClass() == LocalDate.class) {
            bindingProperty = "valueInLocalDate";
        } else if (field.getFieldClass() == LocalDateTime.class) {
            bindingProperty = "valueInLocalDateTime";
        } else if (field.getFieldClass() == LocalTime.class) {
            bindingProperty = "valueInLocalTime";
        } else if (field.getFieldClass() == ZonedDateTime.class) {
            bindingProperty = "valueInZonedDateTime";
        }
        return bindingProperty;
    }
}
