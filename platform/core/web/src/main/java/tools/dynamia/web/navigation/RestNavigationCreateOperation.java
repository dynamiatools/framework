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
import tools.dynamia.crud.CrudPage;
import tools.dynamia.viewers.JsonView;
import tools.dynamia.viewers.ViewDescriptor;

/**
 * Handles the <em>create</em> operation for the REST navigation API.
 *
 * <p>Accepts a JSON body, parses it into a new entity instance using the entity's
 * registered {@link ViewDescriptor}, persists it via {@link tools.dynamia.domain.services.CrudService},
 * and returns the persisted entity as a JSON response.</p>
 *
 * @author Mario A. Serrano Leones
 * @see RestNavigationContext
 */
public class RestNavigationCreateOperation {

    private final RestNavigationContext ctx;

    /**
     * Constructs a new {@code RestNavigationCreateOperation}.
     *
     * @param ctx the shared navigation context providing infrastructure dependencies
     */
    public RestNavigationCreateOperation(RestNavigationContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Creates a new entity by parsing the supplied JSON payload and persisting it.
     *
     * <p>The entity class and its {@link ViewDescriptor} are resolved from the {@link CrudPage}
     * associated with {@code path}. If no descriptor exists, one is auto-generated from the
     * entity's {@code form} view definition.</p>
     *
     * @param path     the navigation path resolving to a {@link CrudPage}
     * @param jsonData the JSON representation of the new entity
     * @param request  the current HTTP request (reserved for future use)
     * @return a {@code 200 OK} JSON response containing the newly persisted entity
     */
    public ResponseEntity<String> create(String path, String jsonData, HttpServletRequest request) {
        CrudPage page = ctx.findCrudPage(path);
        Class entityClass = page.getEntityClass();

        ViewDescriptor descriptor = RestNavigationContext.getJsonFormDescriptor(entityClass, true);
        JsonView jsonView = new JsonView(descriptor);
        jsonView.parse(jsonData);
        Object newEntity = ctx.getCrudService().create(jsonView.getValue());

        return RestNavigationContext.buildJsonResponse(descriptor, newEntity, "Created Successfully");
    }
}

