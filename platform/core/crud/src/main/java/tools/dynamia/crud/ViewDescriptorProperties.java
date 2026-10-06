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
package tools.dynamia.crud;

import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.util.Viewers;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves the properties a {@link ViewDescriptor} needs to render, so a list or table can be mapped to read-only
 * BeanMaps while the persistence context is still open (no Open Persistence In View). Unlike a query projection,
 * the entity is loaded normally, so transient properties and related entities are kept.
 */
public final class ViewDescriptorProperties {

    private static final String PATH = "[A-Za-z_]\\w*(?:\\.[A-Za-z_]\\w*)*";
    private static final Pattern PLAIN_PATH = Pattern.compile(PATH);
    private static final Pattern BEAN_PATH = Pattern.compile("\\bbean\\.(" + PATH + ")");

    private ViewDescriptorProperties() {
    }

    /**
     * Lists the property paths required by the descriptor: {@code id}, the name of every field bound to a property
     * and every property path used by the field {@code bindings} / {@code bind} params (for example
     * {@code foto1.thumbnailUrl}), even when the field itself is virtual. Field {@code path} expressions (query
     * aliases) are ignored. For a nested path the root object is listed too (before it), because bindings are
     * evaluated as {@code bean.foto1.thumbnailUrl} and so need the {@code foto1} object in the map.
     *
     * @param descriptor the view descriptor
     * @return distinct property paths in field order
     */
    public static String[] propertiesOf(ViewDescriptor descriptor) {
        Set<String> properties = new LinkedHashSet<>();
        properties.add("id");
        for (Field field : Viewers.getFields(descriptor)) {
            if (field.isProperty()) {
                add(properties, field.getName());
            }
            addBindings(properties, field.getParams().get(Viewers.PARAM_BINDINGS));
            addExpression(properties, field.getParams().get(Viewers.PARAM_BIND));
        }
        return properties.toArray(new String[0]);
    }

    private static void addBindings(Set<String> properties, Object bindings) {
        if (bindings instanceof Map<?, ?> map) {
            for (Object binding : map.values()) {
                if (binding instanceof Map<?, ?> detail) {
                    addExpression(properties, detail.get("value"));
                } else {
                    addExpression(properties, binding);
                }
            }
        }
    }

    /**
     * Adds the property paths found in a binding expression: either a plain path ({@code tipo.nombre}) or the
     * {@code bean.xxx} paths inside a longer expression. Anything else cannot be resolved and is ignored.
     */
    private static void addExpression(Set<String> properties, Object expression) {
        if (!(expression instanceof String text)) {
            return;
        }
        String trimmed = text.trim();
        if (PLAIN_PATH.matcher(trimmed).matches()) {
            add(properties, trimmed.startsWith("bean.") ? trimmed.substring("bean.".length()) : trimmed);
        } else {
            Matcher matcher = BEAN_PATH.matcher(trimmed);
            while (matcher.find()) {
                add(properties, matcher.group(1));
            }
        }
    }

    private static void add(Set<String> properties, String path) {
        int dot = path.indexOf('.');
        if (dot > 0) {
            properties.add(path.substring(0, dot));
        }
        properties.add(path);
    }
}
