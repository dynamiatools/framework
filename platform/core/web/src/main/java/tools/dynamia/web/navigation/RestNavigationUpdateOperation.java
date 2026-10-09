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
package tools.dynamia.web.navigation;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.StringPojoParser;
import tools.dynamia.commons.reflect.PropertyInfo;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.commons.logger.AbstractLoggable;
import tools.dynamia.crud.CrudPage;
import tools.dynamia.navigation.PageNotFoundException;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.JsonViewDescriptorDeserializer;
import tools.dynamia.viewers.ViewDescriptor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Handles the <em>update</em> operation for the REST navigation API.
 *
 * <p>Applies a partial JSON patch to an existing entity: only the fields present in the
 * request body are updated; all other fields retain their current persisted values.
 * The entity is first loaded from the database, patched in memory, and then saved back
 * via {@link tools.dynamia.domain.services.CrudService#update}.</p>
 *
 * @author Mario A. Serrano Leones
 * @see RestNavigationContext
 */
public class RestNavigationUpdateOperation extends AbstractLoggable {

    private final RestNavigationContext ctx;

    /**
     * Constructs a new {@code RestNavigationUpdateOperation}.
     *
     * @param ctx the shared navigation context providing infrastructure dependencies
     */
    public RestNavigationUpdateOperation(RestNavigationContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Updates the entity identified by {@code id} by applying the fields present in {@code jsonData}.
     *
     * <p>Processing steps:</p>
     * <ol>
     *   <li>Load the entity from the database; throw {@link PageNotFoundException} if absent.</li>
     *   <li>Parse the JSON body into a {@link JsonNode} tree.</li>
     *   <li>For each field present in both the JSON and the entity's {@link ViewDescriptor},
     *       invoke the corresponding setter via reflection.</li>
     *   <li>Persist the patched entity and return it as JSON.</li>
     * </ol>
     *
     * @param path     the navigation path resolving to a {@link CrudPage}
     * @param id       the entity identifier
     * @param jsonData the JSON object containing the fields to update
     * @param request  the current HTTP request (reserved for future use)
     * @return a {@code 200 OK} JSON response containing the updated entity
     * @throws PageNotFoundException if no entity with the given {@code id} exists
     */
    public ResponseEntity<String> update(String path, Long id, String jsonData, HttpServletRequest request) {
        CrudPage page = ctx.findCrudPage(path);
        Class entityClass = page.getEntityClass();

        @SuppressWarnings("unchecked") final Object entity = ctx.getCrudService().find(entityClass, id);
        if (entity == null) {
            throw new PageNotFoundException(entityClass.getSimpleName() + " with id " + id + " not found");
        }

        ViewDescriptor descriptor = RestNavigationContext.getJsonFormDescriptor(entityClass, true);
        try {
            JsonNode node = StringPojoParser.createJsonMapper().readTree(jsonData);
            node.properties().forEach(entry -> {
                Field field = descriptor.getField(entry.getKey());
                if (field != null) {
                    var info = field.getPropertyInfo();
                    if (info != null && info.isCollection()) {
                        applyCollection(entity, info, entry.getValue());
                        return;
                    }
                    Object fieldValue = JsonViewDescriptorDeserializer.getNodeValue(info, entry.getValue());
                    ObjectOperations.invokeSetMethod(entity, info, fieldValue);
                }
            });
        } catch (JacksonException e) {
            log("Error updating entity", e);
        }

        return RestNavigationContext.buildJsonResponse(descriptor, ctx.getCrudService().update(entity), "Updated Successfully");
    }

    /**
     * A collection field of the patch. Only a many-to-many association of entities is replaced, with the entities whose
     * {@code id} the JSON array lists. Any other collection (owned children, element collections) is left untouched:
     * {@link JsonViewDescriptorDeserializer#getNodeValue} cannot build it, and setting {@code null} would wipe the
     * current children.
     */
    private void applyCollection(Object entity, PropertyInfo info, JsonNode node) {
        if (!node.isArray() || !isManyToMany(info) || !DomainUtils.isEntity(info.getGenericType())) {
            return;
        }
        Collection<Object> items = Set.class.isAssignableFrom(info.getType()) ? new LinkedHashSet<>() : new ArrayList<>();
        for (JsonNode item : node) {
            var id = item.get("id");
            if (id != null) {
                @SuppressWarnings("unchecked") Object related = ctx.getCrudService().find((Class) info.getGenericType(), id.asLong());
                if (related != null) {
                    items.add(related);
                }
            }
        }
        ObjectOperations.invokeSetMethod(entity, info, items);
    }

    private static boolean isManyToMany(PropertyInfo info) {
        var annotations = new ArrayList<Annotation>();
        if (info.getField() != null) {
            annotations.addAll(Arrays.asList(info.getField().getAnnotations()));
        }
        if (info.getReadMethod() != null) {
            annotations.addAll(Arrays.asList(info.getReadMethod().getAnnotations()));
        }
        return annotations.stream().anyMatch(a -> "ManyToMany".equals(a.annotationType().getSimpleName()));
    }
}
