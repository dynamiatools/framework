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
import tools.dynamia.navigation.PageNotFoundException;

/**
 * Handles the <em>delete</em> operation for the REST navigation API.
 *
 * <p>Loads the entity to verify it exists (returning its last known state in the response),
 * then removes it via {@link tools.dynamia.domain.services.CrudService#delete}.</p>
 *
 * @author Mario A. Serrano Leones
 * @see RestNavigationContext
 */
public class RestNavigationDeleteOperation {

    private final RestNavigationContext ctx;

    /**
     * Constructs a new {@code RestNavigationDeleteOperation}.
     *
     * @param ctx the shared navigation context providing infrastructure dependencies
     */
    public RestNavigationDeleteOperation(RestNavigationContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Deletes the entity identified by {@code id} and returns its last known state.
     *
     * @param path    the navigation path resolving to a {@link CrudPage}
     * @param id      the entity identifier
     * @param request the current HTTP request (reserved for future use)
     * @return a {@code 200 OK} JSON response containing the deleted entity's data
     * @throws PageNotFoundException if no entity with the given {@code id} exists
     */
    public ResponseEntity<String> delete(String path, Long id, HttpServletRequest request) {
        CrudPage page = ctx.findCrudPage(path);
        Class entityClass = page.getEntityClass();

        Object result = ctx.getCrudService().find(entityClass, id);
        if (result == null) {
            throw new PageNotFoundException(entityClass.getSimpleName() + " with id " + id + " not found");
        }

        ctx.getCrudService().delete(entityClass, id);
        return RestNavigationContext.buildJsonResponse(RestNavigationContext.getJsonFormDescriptor(entityClass), result, "Deleted Successfully");
    }
}

