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
package tools.dynamia.crud.cfg;

import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.domain.query.ApplicationParameters;
import tools.dynamia.domain.query.Parameter;
import tools.dynamia.domain.query.Parameters;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.ViewDescriptor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads and saves the values of a {@code view: config} descriptor as application {@link Parameter}s, without any UI
 * dependency. Every front end of a {@link ConfigPage} (the REST API used by Vue, tests) goes through here, so the mapping
 * from a field to its parameter is defined once:
 *
 * <ul>
 *   <li>the parameter name is {@code params.parameterName} of the field, or {@code <descriptorId>_<fieldName>};</li>
 *   <li>the parameter class is {@code parameterClass} of the descriptor, or {@link DomainUtils#getDefaultParameterClass()};</li>
 *   <li>a parameter that does not exist yet is created with the {@code defaultValue} of the field (or an empty value).</li>
 * </ul>
 *
 * <p>Only the visible fields of the descriptor can be read or written: a caller can never reach a parameter that the
 * descriptor does not declare, whatever names it sends.</p>
 *
 * <pre>{@code
 * Map<String, Object> values = ConfigParameters.read(descriptor);
 * ConfigParameters.save(descriptor, Map.of("useHttps", true));
 * }</pre>
 *
 * @author Mario A. Serrano Leones
 */
public final class ConfigParameters {

    /** Descriptor param with the class name of a custom {@link Parameter} implementation. */
    public static final String PARAM_PARAMETER_CLASS = "parameterClass";
    /** Field param with an explicit parameter name. */
    public static final String PARAM_PARAMETER_NAME = "parameterName";
    /** Field param that marks the parameter as cacheable. */
    public static final String PARAM_CACHEABLE = "cacheable";
    /** Field param with the value used while the parameter does not exist. */
    public static final String PARAM_DEFAULT_VALUE = "defaultValue";

    private ConfigParameters() {
    }

    /**
     * Reads the current value of every visible field of a config descriptor.
     *
     * @param descriptor the {@code config} view descriptor
     * @return field name to value, in descriptor order; checkbox fields come as {@link Boolean}, the rest as {@link String}
     */
    public static Map<String, Object> read(ViewDescriptor descriptor) {
        return read(descriptor, ApplicationParameters.get());
    }

    /**
     * Same as {@link #read(ViewDescriptor)} against an explicit {@link Parameters} store.
     *
     * @param descriptor the {@code config} view descriptor
     * @param store      where the parameters live
     * @return field name to value
     */
    public static Map<String, Object> read(ViewDescriptor descriptor, Parameters store) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (Field field : visibleFields(descriptor)) {
            String value = loadParameter(descriptor, field, store).getValue();
            values.put(field.getName(), isBoolean(field) ? Boolean.parseBoolean(value) : value == null ? "" : value);
        }
        return values;
    }

    /**
     * Saves the given values. Names that are not visible fields of the descriptor are ignored (and logged).
     *
     * @param descriptor the {@code config} view descriptor
     * @param values     field name to new value; {@code null} saves an empty value
     */
    public static void save(ViewDescriptor descriptor, Map<String, ?> values) {
        save(descriptor, values, ApplicationParameters.get());
    }

    /**
     * Same as {@link #save(ViewDescriptor, Map)} against an explicit {@link Parameters} store.
     *
     * @param descriptor the {@code config} view descriptor
     * @param values     field name to new value
     * @param store      where the parameters live
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void save(ViewDescriptor descriptor, Map<String, ?> values, Parameters store) {
        var allowed = visibleFields(descriptor);
        values.keySet().stream()
                .filter(name -> allowed.stream().noneMatch(f -> f.getName().equals(name)))
                .forEach(name -> LoggingService.get(ConfigParameters.class)
                        .warn("Ignoring value for '" + name + "': not a field of config " + descriptor.getId()));

        List<Parameter> changed = new ArrayList<>();
        for (Field field : allowed) {
            if (values.containsKey(field.getName())) {
                Parameter parameter = loadParameter(descriptor, field, store);
                Object value = values.get(field.getName());
                parameter.setValue(value == null ? "" : value.toString());
                changed.add(parameter);
            }
        }
        store.save(changed);
    }

    /**
     * @param field a field of a config descriptor
     * @return the name of the parameter that stores it
     */
    public static String parameterName(Field field) {
        Object name = field.getParams().get(PARAM_PARAMETER_NAME);
        return name != null ? name.toString() : field.getViewDescriptor().getId() + "_" + field.getName();
    }

    /**
     * @param descriptor a config descriptor
     * @return the {@link Parameter} implementation its values are stored in
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends Parameter> parameterClass(ViewDescriptor descriptor) {
        Object className = descriptor.getParams().get(PARAM_PARAMETER_CLASS);
        if (className == null) {
            return DomainUtils.getDefaultParameterClass();
        }
        try {
            return (Class<? extends Parameter>) Class.forName(className.toString());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Parameter class not found " + className + " in config " + descriptor.getId(), e);
        }
    }

    @SuppressWarnings("rawtypes")
    private static Parameter loadParameter(ViewDescriptor descriptor, Field field, Parameters store) {
        Class<? extends Parameter> parameterClass = parameterClass(descriptor);
        String name = parameterName(field);

        Parameter parameter = store.getParameter(parameterClass, name);
        if (parameter == null) {
            parameter = ObjectOperations.newInstance(parameterClass);
            parameter.setName(name);
            Object defaultValue = field.getParams().get(PARAM_DEFAULT_VALUE);
            parameter.setValue(defaultValue != null ? defaultValue.toString() : "");
        }
        parameter.setDescription(field.getDescription());
        parameter.setLabel(field.getLabel());
        parameter.setCacheable(Boolean.TRUE.equals(field.getParams().get(PARAM_CACHEABLE)));
        return parameter;
    }

    private static List<Field> visibleFields(ViewDescriptor descriptor) {
        return descriptor.getFields().stream().filter(Field::isVisible).toList();
    }

    private static boolean isBoolean(Field field) {
        return "checkbox".equalsIgnoreCase(field.getComponent())
                || field.getFieldClass() == Boolean.class || field.getFieldClass() == boolean.class;
    }
}
