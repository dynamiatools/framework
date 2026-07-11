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
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.dynamia.crud.CrudPage;
import tools.dynamia.domain.query.DataPaginator;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.QueryBuilder;
import tools.dynamia.navigation.ModuleContainer;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.web.navigation.RestNavigationContext.ListResult;
import tools.dynamia.web.navigation.RestNavigationContext.SimpleResult;

import java.util.List;

/**
 * REST controller that handles CRUD operations on entities exposed through the application's
 * navigation structure. Each entity's path is derived from its corresponding {@link CrudPage}
 * definition, and this controller maps HTTP requests to the appropriate persistence operations
 * via {@link CrudService}.
 *
 * <p>This class acts as a <em>thin delegator</em>: all logic is implemented in focused
 * operation classes and shared utilities:</p>
 * <ul>
 *   <li>{@link RestNavigationContext} — shared state, descriptor lookup, response builders</li>
 *   <li>{@link RestNavigationQuerySupport} — filters, sorting, pagination helpers</li>
 *   <li>{@link RestNavigationReadOperation} — GET (list) and GET (single)</li>
 *   <li>{@link RestNavigationCreateOperation} — POST</li>
 *   <li>{@link RestNavigationUpdateOperation} — PUT</li>
 *   <li>{@link RestNavigationDeleteOperation} — DELETE</li>
 * </ul>
 *
 * <p>All public {@code static} methods are preserved for backward compatibility with external
 * callers (e.g., {@link RestApiNavigationConfiguration}, custom REST customizers).</p>
 *
 * @author Mario A. Serrano Leones
 */
@RestController("restNavigationController")
@Order(1000)
public class RestNavigationController {

    private final RestNavigationContext ctx;
    private final RestNavigationReadOperation readOp;
    private final RestNavigationCreateOperation createOp;
    private final RestNavigationUpdateOperation updateOp;
    private final RestNavigationDeleteOperation deleteOp;

    /**
     * Constructs a new {@code RestNavigationController} and initialises all operation delegates.
     *
     * @param moduleContainer the container used to resolve navigation pages by path
     * @param crudService     the service used to perform CRUD operations on entities
     */
    public RestNavigationController(ModuleContainer moduleContainer, CrudService crudService) {
        this.ctx = new RestNavigationContext(moduleContainer, crudService);
        this.readOp = new RestNavigationReadOperation(ctx);
        this.createOp = new RestNavigationCreateOperation(ctx);
        this.updateOp = new RestNavigationUpdateOperation(ctx);
        this.deleteOp = new RestNavigationDeleteOperation(ctx);
    }

    // -------------------------------------------------------------------------
    // Route entry points
    // -------------------------------------------------------------------------

    /**
     * Entry point for reading all entities at the resolved path.
     *
     * @param request the current HTTP request
     * @return a JSON response containing the paginated entity list
     */
    public ResponseEntity<String> routeReadAll(HttpServletRequest request) {
        return readOp.readAll(ctx.getPath(request), request);
    }

    /**
     * Entry point for reading a single entity by its ID.
     *
     * @param id      the entity identifier
     * @param request the current HTTP request
     * @return a JSON response containing the requested entity
     */
    public ResponseEntity<String> routeReadOne(@PathVariable Long id, HttpServletRequest request) {
        return readOp.readOne(ctx.getPath(request).replace("/" + id, ""), id, request);
    }

    /**
     * Entry point for creating a new entity from the supplied JSON body.
     *
     * @param jsonData the JSON representation of the entity to create
     * @param request  the current HTTP request
     * @return a JSON response containing the persisted entity
     */
    public ResponseEntity<String> routeCreate(@RequestBody String jsonData, HttpServletRequest request) {
        return createOp.create(ctx.getPath(request), jsonData, request);
    }

    /**
     * Entry point for updating an existing entity identified by {@code id}.
     *
     * @param id       the entity identifier
     * @param jsonData the JSON patch data with updated field values
     * @param request  the current HTTP request
     * @return a JSON response containing the updated entity
     */
    public ResponseEntity<String> routeUpdate(@PathVariable Long id, @RequestBody String jsonData, HttpServletRequest request) {
        return updateOp.update(ctx.getPath(request).replace("/" + id, ""), id, jsonData, request);
    }

    /**
     * Entry point for deleting an entity identified by {@code id}.
     *
     * @param id      the entity identifier
     * @param request the current HTTP request
     * @return a JSON response containing the deleted entity's last known state
     */
    public ResponseEntity<String> routeDelete(@PathVariable Long id, HttpServletRequest request) {
        return deleteOp.delete(ctx.getPath(request).replace("/" + id, ""), id, request);
    }

    // -------------------------------------------------------------------------
    // Public static API — preserved for backward compatibility
    // -------------------------------------------------------------------------

    /**
     * @see RestNavigationContext#buildJsonResponse(Object)
     */
    public static ResponseEntity<String> buildJsonResponse(Object result) {
        return RestNavigationContext.buildJsonResponse(result);
    }

    /**
     * @see RestNavigationContext#buildJsonResponse(ViewDescriptor, Object, String)
     */
    public static ResponseEntity<String> buildJsonResponse(ViewDescriptor descriptor, Object result, String message) {
        return RestNavigationContext.buildJsonResponse(descriptor, result, message);
    }

    /**
     * @see RestNavigationContext#buildJsonResponse(ViewDescriptor, ListResult, String)
     */
    public static ResponseEntity<String> buildJsonResponse(ViewDescriptor descriptor, ListResult result, String message) {
        return RestNavigationContext.buildJsonResponse(descriptor, result, message);
    }

    /**
     * @see RestNavigationContext#getJsonFormDescriptor(Class)
     */
    public static ViewDescriptor getJsonFormDescriptor(Class entityClass) {
        return RestNavigationContext.getJsonFormDescriptor(entityClass);
    }

    /**
     * @see RestNavigationContext#getJsonFormDescriptor(Class, boolean)
     */
    public static ViewDescriptor getJsonFormDescriptor(Class entityClass, boolean autocreate) {
        return RestNavigationContext.getJsonFormDescriptor(entityClass, autocreate);
    }

    /**
     * @see RestNavigationContext#getJsonTableDescriptor(Class)
     */
    public static ViewDescriptor getJsonTableDescriptor(Class entityClass) {
        return RestNavigationContext.getJsonTableDescriptor(entityClass);
    }

    /**
     * @see RestNavigationReadOperation#buildListResult(List, DataPaginator, int)
     */
    public static ListResult buildListResult(List content, DataPaginator paginator, int currentPage) {
        return RestNavigationReadOperation.buildListResult(content, paginator, currentPage);
    }

    /**
     * @see RestNavigationQuerySupport#parseConditions(QueryBuilder, ViewDescriptor)
     */
    public static void parseConditions(QueryBuilder query, ViewDescriptor descriptor) {
        RestNavigationQuerySupport.parseConditions(query, descriptor);
    }

    /**
     * @see RestNavigationQuerySupport#applyRequestFilters(HttpServletRequest, QueryBuilder, ViewDescriptor)
     */
    public static void applyRequestFilters(HttpServletRequest request, QueryBuilder query, ViewDescriptor descriptor) {
        RestNavigationQuerySupport.applyRequestFilters(request, query, descriptor);
    }

    /**
     * @see RestNavigationQuerySupport#applyRequestSorting(HttpServletRequest, QueryBuilder, ViewDescriptor)
     */
    public static void applyRequestSorting(HttpServletRequest request, QueryBuilder query, ViewDescriptor descriptor) {
        RestNavigationQuerySupport.applyRequestSorting(request, query, descriptor);
    }

    /**
     * @see RestNavigationQuerySupport#getParameterNumber(HttpServletRequest, String)
     */
    public static int getParameterNumber(HttpServletRequest request, String name) {
        return RestNavigationQuerySupport.getParameterNumber(request, name);
    }

    /**
     * @see RestNavigationContext#getMetadata(HttpServletRequest, ViewDescriptor)
     */
    public static ResponseEntity<String> getMetadata(HttpServletRequest request, ViewDescriptor viewDescriptor) {
        return RestNavigationContext.getMetadata(request, viewDescriptor);
    }
}
