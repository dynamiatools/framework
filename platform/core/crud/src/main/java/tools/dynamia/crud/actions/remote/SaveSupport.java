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
package tools.dynamia.crud.actions.remote;

import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.StringPojoParser;
import tools.dynamia.crud.CrudState;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.JsonViewDescriptorDeserializer;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.util.Viewers;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

/**
 * Shared persistence logic for {@link SaveRemoteAction} and {@link SaveFlowRemoteAction}.
 * <p>
 * Deliberately a small {@code static} helper rather than a common superclass — see
 * {@code docs/design/SERVER_DRIVEN_ACTION_FLOWS.md} §7.4 for why {@code SaveFlowRemoteAction extends
 * SaveRemoteAction} is a footgun here (a class method always wins over {@code FlowRemoteAction}'s default
 * {@code execute()}, silently skipping the flow dispatch).
 * <p>
 * Same field-resolution approach as {@code RestNavigationUpdateOperation} (plain REST CRUD writes): entities
 * are resolved through the entity's {@code json-form}/{@code json}/{@code form} {@link ViewDescriptor} so
 * only known, mapped fields are ever set from client input.
 *
 * @author Mario A. Serrano Leones
 */
final class SaveSupport {

    private SaveSupport() {
    }

    /**
     * Resolves {@code className} (a fully qualified Java class name) to its {@link Class}.
     *
     * @throws IllegalArgumentException if {@code className} is missing or not a known class
     */
    static Class<?> resolveEntityClass(String className) {
        if (className == null || className.isBlank()) {
            throw new IllegalArgumentException("Missing dataType/className for save action");
        }
        Class<?> entityClass = ObjectOperations.findClass(className);
        if (entityClass == null) {
            throw new IllegalArgumentException("Unknown entity class: " + className);
        }
        return entityClass;
    }

    /**
     * Creates or updates {@code entityClass} from {@code data} (the action request's payload, deserialized
     * as a {@code Map<String, Object>}): loads and patches the existing entity via {@link CrudService#update}
     * when {@code data} carries a non-null {@code id}, otherwise parses a new instance and
     * {@link CrudService#create creates} it.
     * <p>
     * Enforces {@code applicableStates} server-side (a hand-crafted request can't invoke, say, an update
     * through an action whose declared applicable states are {@code CREATE}-only) — see
     * {@code docs/design/SERVER_DRIVEN_ACTION_FLOWS.md} §7.6, point 2.
     *
     * @throws IllegalArgumentException if {@code data} isn't a map, the inferred state (CREATE when no
     *                                  {@code id}, UPDATE otherwise) isn't in {@code applicableStates}, or
     *                                  {@code id} is set but no such entity exists
     */
    @SuppressWarnings("unchecked")
    static Object persist(CrudService crudService, Class<?> entityClass, Object data, CrudState[] applicableStates) {
        if (!(data instanceof Map)) {
            throw new IllegalArgumentException("Save action data must be an object, got: "
                    + (data == null ? "null" : data.getClass()));
        }
        Map<String, Object> map = (Map<String, Object>) data;

        Object idValue = map.get("id");
        CrudState inferredState = idValue != null ? CrudState.UPDATE : CrudState.CREATE;
        if (!Arrays.asList(applicableStates).contains(inferredState)) {
            throw new IllegalArgumentException("This action does not allow " + inferredState + " (id=" + idValue + ")");
        }

        ViewDescriptor descriptor = jsonFormDescriptor(entityClass);

        if (idValue != null) {
            Object entity = crudService.find(entityClass, (Serializable) idValue);
            if (entity == null) {
                throw new IllegalArgumentException(entityClass.getSimpleName() + " with id " + idValue + " not found");
            }
            applyPatch(entity, descriptor, map);
            return crudService.update(entity);
        } else {
            Object entity = ObjectOperations.newInstance(entityClass);
            applyPatch(entity, descriptor, map);
            return crudService.create(entity);
        }
    }

    /** Same lookup order as {@code RestNavigationContext.getJsonFormDescriptor(entityClass, true)}. */
    private static ViewDescriptor jsonFormDescriptor(Class<?> entityClass) {
        ViewDescriptor descriptor = Viewers.findViewDescriptor(entityClass, "json-form");
        if (descriptor == null) {
            descriptor = Viewers.findViewDescriptor(entityClass, "json");
        }
        if (descriptor == null) {
            descriptor = Viewers.findViewDescriptor(entityClass, "form");
        }
        if (descriptor == null) {
            descriptor = Viewers.getViewDescriptor(entityClass, "form");
        }
        return descriptor;
    }

    /** Applies every field present in {@code data} and mapped in {@code descriptor} onto {@code entity}. */
    private static void applyPatch(Object entity, ViewDescriptor descriptor, Map<String, Object> data) {
        try {
            String json = StringPojoParser.convertMapToJson(data);
            JsonNode node = StringPojoParser.createJsonMapper().readTree(json);
            node.properties().forEach(entry -> {
                Field field = descriptor.getField(entry.getKey());
                if (field != null) {
                    Object fieldValue = JsonViewDescriptorDeserializer.getNodeValue(field.getPropertyInfo(), entry.getValue());
                    ObjectOperations.invokeSetMethod(entity, field.getPropertyInfo(), fieldValue);
                }
            });
        } catch (JacksonException e) {
            throw new IllegalStateException("Unable to build/patch " + entity.getClass() + " from action data", e);
        }
    }
}
