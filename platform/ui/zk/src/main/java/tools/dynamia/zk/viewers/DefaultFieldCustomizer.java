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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.*;
import tools.dynamia.commons.*;
import tools.dynamia.commons.DayOfWeek;
import tools.dynamia.commons.collect.ArrayListMultiMap;
import tools.dynamia.commons.collect.ListMultiMap;
import tools.dynamia.commons.logger.LoggingService;
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
 * DefaultFieldCustomizer is responsible for automatically configuring and customizing fields for ZK UI components
 * based on their type, name, and context within a view. It maps Java types to ZK components, sets labels, handles
 * special cases (such as enums, dates, colors), and applies appropriate bindings for form views. This class enables
 * dynamic and context-aware UI generation, reducing manual configuration and ensuring consistency across views.
 * <p>
 * Main features:
 * <ul>
 *   <li>Maps Java field types to ZK UI components using a predefined index.</li>
 *   <li>Automatically sets field labels and component classes.</li>
 *   <li>Handles special cases for booleans, enums, dates, times, and colors.</li>
 *   <li>Configures data bindings for form views, including date and time fields.</li>
 *   <li>Supports extensibility via customizers for specific field types.</li>
 * </ul>
 * <p>
 * Usage: This class is typically used by the DynamiaTools framework to prepare fields for rendering in ZK-based views.
 * It is registered as a provider and invoked automatically during view generation.
 *
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@Provider
public class DefaultFieldCustomizer implements FieldCustomizer {

    private final static ListMultiMap<Class<? extends Component>, Class> INDEX = new ArrayListMultiMap<>();
    private static final Logger log = LoggerFactory.getLogger(DefaultFieldCustomizer.class);
    private LoggingService logger = LoggingService.get(getClass());

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
        INDEX.put(LocalDateRangebox.class, LocalDateRange.class);
        INDEX.put(LocalDateTimeRangebox.class, LocalDateTimeRange.class);
        INDEX.put(Timebox.class, LocalTime.class);
        INDEX.put(LocaleCombobox.class, Locale.class);
        INDEX.put(DurationSelector.class, Duration.class);
    }

    /**
     * Customizes the given field based on its properties and the view type.
     * <p>
     * This method determines the appropriate ZK component for the field, sets its label if missing,
     * and configures bindings and customizations for form views. It also handles special cases for booleans,
     * enums, and date/time fields, ensuring the field is properly set up for display or editing.
     * </p>
     *
     * @param viewTypeName The name of the view type (e.g., "form").
     * @param field        The field to customize.     *
     */
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


        if (field.getComponentClass() != null) {
            customizeCombobox(field);
            customizeDateboxBindings(field);
            customizeTimeboxBindings(field);
            customizeDateSelectorBinding(field);
        }
        logger.debug("Customized field: " + field + " params: " + field.getParams());
    }

    /**
     * Checks if the given view type name corresponds to a form view.
     *
     * @param viewTypeName The name of the view type.
     * @return true if the view type is "form", false otherwise.
     */
    private static boolean isForm(String viewTypeName) {
        return viewTypeName != null && viewTypeName.equalsIgnoreCase("form");
    }

    public static boolean isForm(Field field) {
        return field != null && field.getViewDescriptor() != null && isForm(field.getViewDescriptor().getViewTypeName());
    }

    /**
     * Customizes a Combobox field to be readonly if not already specified.
     * <p>
     * Ensures that Combobox components are set to readonly by default unless overridden.
     * </p>
     *
     * @param field The field to customize.
     */
    public static void customizeCombobox(Field field) {
        if (field.getComponentClass() == Combobox.class && field.getParams().get(Viewers.PARAM_READ_ONLY) == null) {
            field.addParam(Viewers.PARAM_READ_ONLY, true);
        }
    }

    /**
     * Recursively searches for a ZK component class that matches the given superclass.
     * <p>
     * This method traverses the class hierarchy to find a suitable component mapping.
     * </p>
     *
     * @param superClass The superclass to search for.
     * @return The matching ZK component class, or null if not found.
     */
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

    /**
     * Retrieves the ZK component class associated with the given field class.
     *
     * @param fieldClass The field class to look up.
     * @return The corresponding ZK component class, or null if not mapped.
     */
    private static Class<? extends Component> getComponentClass(Class fieldClass) {
        return INDEX.getKey(fieldClass);
    }

    /**
     * Configures the field for form views by assigning the appropriate ZK component and customizer.
     * <p>
     * Handles special cases for color fields, enums, and date/time fields, ensuring correct component assignment
     * and binding setup for form editing.
     * </p>
     *
     * @param field The field to configure.
     */
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
    }

    /**
     * Sets up binding attributes for DateSelector components when used with LocalDate fields.
     * <p>
     * Ensures that the correct binding attribute is set for proper data synchronization.
     * </p>
     *
     * @param field The field to customize.
     */
    public static void customizeDateSelectorBinding(Field field) {
        if (field.getComponentClass() == DateSelector.class && !field.containsParam(Viewers.PARAM_BINDINGS) && !field.containsParam(Viewers.PARAM_BINDING_ATTRIBUTE) && field.getFieldClass() == LocalDate.class) {
            String attribute = "selectedLocalDate";
            field.addParam(Viewers.PARAM_BINDING_ATTRIBUTE, attribute);
        }
    }

    /**
     * Configures binding attributes and formatting for Datebox components.
     * <p>
     * Sets the binding attribute, time zone, locale, and format for date fields, especially for LocalDateTime.
     * </p>
     *
     * @param field The field to customize.
     */
    public static void customizeDateboxBindings(Field field) {
        if (field.getComponentClass() == Datebox.class && !field.containsParam(Viewers.PARAM_BINDINGS) && !field.containsParam(Viewers.PARAM_BINDING_ATTRIBUTE)) {
            String attribute = getDateboxBindingAttribute(field);
            field.addParam(Viewers.PARAM_BINDING_ATTRIBUTE, attribute);
            field.addParam("timeZone", TimeZone.getTimeZone(Messages.getDefaultTimeZone()));
            field.addParam("locale", Messages.getDefaultLocale());
        }
    }

    /**
     * Sets up binding attributes for Timebox components.
     * <p>
     * Ensures that the correct binding attribute is set for time fields.
     * </p>
     *
     * @param field The field to customize.
     */
    public static void customizeTimeboxBindings(Field field) {
        if (field.getComponentClass() == Timebox.class && !field.containsParam(Viewers.PARAM_BINDINGS) && !field.containsParam(Viewers.PARAM_BINDING_ATTRIBUTE)) {
            String attribute = getDateboxBindingAttribute(field);
            field.addParam(Viewers.PARAM_BINDING_ATTRIBUTE, attribute);
        }
    }

    /**
     * Determines the appropriate binding property name for date/time fields based on their type.
     * <p>
     * Returns the correct property name to use for data binding in ZK components.
     * </p>
     *
     * @param field The field to analyze.
     * @return The binding property name for the field's type.
     */
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
