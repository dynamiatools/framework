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

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.StringPojoParser;
import tools.dynamia.commons.collect.PagedList;
import tools.dynamia.commons.logger.AbstractLoggable;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.crud.CrudPage;
import tools.dynamia.domain.query.DataPaginator;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.QueryBuilder;
import tools.dynamia.navigation.ModuleContainer;
import tools.dynamia.navigation.NavigationRestrictions;
import tools.dynamia.navigation.Page;
import tools.dynamia.navigation.PageNotFoundException;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.JsonView;
import tools.dynamia.viewers.JsonViewDescriptorDeserializer;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.util.Viewers;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

/**
 * REST controller that handles CRUD operations on entities exposed through the application's
 * navigation structure. Each entity's path is derived from its corresponding {@link CrudPage}
 * definition, and this controller maps HTTP requests to the appropriate persistence operations
 * via {@link CrudService}.
 *
 * <p>Supported operations per entity path:</p>
 * <ul>
 *   <li>GET (list) — paginated collection with optional query conditions</li>
 *   <li>GET (single) — retrieve one entity by ID</li>
 *   <li>POST — create a new entity from a JSON payload</li>
 *   <li>PUT — update an existing entity by ID</li>
 *   <li>DELETE — remove an entity by ID</li>
 * </ul>
 *
 * <p>All responses are serialized as JSON using the entity's registered {@link ViewDescriptor}.
 * Access control is enforced via {@link NavigationRestrictions}.</p>
 *
 * @author Mario A. Serrano Leones
 */
@RestController("restNavigationController")
@Order(1000)
public class RestNavigationController extends AbstractLoggable {

    /** Default number of items returned per page when no {@code size} parameter is provided. */
    private static final int DEFAULT_PAGINATION_SIZE = 50;

    private static final String JSON_FORM = "json-form";
    private static final String JSON = "json";
    private static final String FORM = "form";

    private final ModuleContainer moduleContainer;
    private final CrudService crudService;

    /**
     * Constructs a new {@code RestNavigationController}.
     *
     * @param moduleContainer the container used to resolve navigation pages by path
     * @param crudService     the service used to perform CRUD operations on entities
     */
    public RestNavigationController(ModuleContainer moduleContainer, CrudService crudService) {
        this.moduleContainer = moduleContainer;
        this.crudService = crudService;
    }

    // -------------------------------------------------------------------------
    // Path extraction
    // -------------------------------------------------------------------------

    /**
     * Extracts the navigation path from the incoming request URI, stripping the
     * leading {@code /api/} prefix when present.
     *
     * @param request the current HTTP request
     * @return the normalized navigation path
     */
    private String getPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.startsWith("/api/")) {
            path = path.replaceFirst("/api/", "");
        }
        return path;
    }

    // -------------------------------------------------------------------------
    // Route entry points (delegators)
    // -------------------------------------------------------------------------

    /**
     * Entry point for reading all entities at the resolved path.
     *
     * @param request the current HTTP request
     * @return a JSON response containing the paginated entity list
     */
    public ResponseEntity<String> routeReadAll(HttpServletRequest request) {
        return readAll(getPath(request), request);
    }

    /**
     * Entry point for reading a single entity by its ID.
     *
     * @param id      the entity identifier
     * @param request the current HTTP request
     * @return a JSON response containing the requested entity, or 404 if not found
     */
    public ResponseEntity<String> routeReadOne(@PathVariable Long id, HttpServletRequest request) {
        return readOne(getPath(request).replace("/" + id, ""), id, request);
    }

    /**
     * Entry point for creating a new entity from the supplied JSON body.
     *
     * @param jsonData the JSON representation of the entity to create
     * @param request  the current HTTP request
     * @return a JSON response containing the persisted entity
     */
    public ResponseEntity<String> routeCreate(@RequestBody String jsonData, HttpServletRequest request) {
        return create(getPath(request), jsonData, request);
    }

    /**
     * Entry point for updating an existing entity identified by {@code id}.
     *
     * @param id       the entity identifier
     * @param jsonData the JSON patch data with updated field values
     * @param request  the current HTTP request
     * @return a JSON response containing the updated entity, or 404 if not found
     */
    public ResponseEntity<String> routeUpdate(@PathVariable Long id, @RequestBody String jsonData, HttpServletRequest request) {
        return update(getPath(request).replace("/" + id, ""), id, jsonData, request);
    }

    /**
     * Entry point for deleting an entity identified by {@code id}.
     *
     * @param id      the entity identifier
     * @param request the current HTTP request
     * @return a JSON response containing the deleted entity's data, or 404 if not found
     */
    public ResponseEntity<String> routeDelete(@PathVariable Long id, HttpServletRequest request) {
        return delete(getPath(request).replace("/" + id, ""), id, request);
    }

    // -------------------------------------------------------------------------
    // Internal CRUD operations
    // -------------------------------------------------------------------------

    /**
     * Reads all entities of the type associated with the given navigation {@code path},
     * applying optional query conditions and pagination.
     *
     * @param path    the navigation path resolving to a {@link CrudPage}
     * @param request the current HTTP request (used for pagination and metadata params)
     * @return a paginated JSON response or a metadata response when {@code _metadata} is requested
     */
    private ResponseEntity<String> readAll(String path, HttpServletRequest request) {
        CrudPage page = findCrudPage(path);
        Class entityClass = page.getEntityClass();

        ViewDescriptor readDescriptor = getJsonTableDescriptor(entityClass);
        ResponseEntity<String> metadata = getMetadata(request, readDescriptor);
        if (metadata != null) {
            return metadata;
        }

        QueryBuilder query = QueryBuilder.select().from(entityClass, "e");
        QueryParameters pageParams = (QueryParameters) page.getAttribute("queryParameters");
        if (pageParams != null) {
            query.where(pageParams);
        }
        parseConditions(query, readDescriptor);

        int pageSize = getParameterNumber(request, "size");
        int currentPage = getParameterNumber(request, "page");
        if (pageSize == 0) {
            pageSize = DEFAULT_PAGINATION_SIZE;
        }
        query.getQueryParameters().paginate(pageSize);

        List content = crudService.executeQuery(query);
        DataPaginator paginator = query.getQueryParameters().getPaginator();
        if (paginator != null) {
            paginator.setPage(currentPage);
        }

        return buildJsonResponse(readDescriptor, buildListResult(content, paginator, currentPage), "OK");
    }

    /**
     * Builds a {@link ListResult} from a raw content list, handling paged data sources
     * when the content is a {@link PagedList}.
     *
     * @param content     the raw list returned by the query
     * @param paginator   the {@link DataPaginator} associated with the query, may be {@code null}
     * @param currenPage  the requested page number (1-based); ignored when {@code <= 0}
     * @return a populated {@link ListResult} ready for serialization
     */
    public static ListResult buildListResult(List content, DataPaginator paginator, int currenPage) {
        ListResult result = new ListResult();
        if (content instanceof PagedList pagedList && paginator != null) {
            if (currenPage > 0) {
                pagedList.getDataSource().setActivePage(currenPage);
            }
            result.setData(pagedList.getDataSource().getPageData());
            result.setPageable(paginator);
        } else {
            result.setData(content);
        }
        result.setResponse("OK");
        return result;
    }

    /**
     * Reads a single entity by its {@code id} from the {@link CrudPage} resolved by {@code path}.
     *
     * @param path    the navigation path resolving to a {@link CrudPage}
     * @param id      the entity identifier
     * @param request the current HTTP request
     * @return a JSON response with the entity, or HTTP 404 if not found
     */
    private ResponseEntity<String> readOne(String path, Long id, HttpServletRequest request) {
        CrudPage page = findCrudPage(path);
        Class entityClass = page.getEntityClass();

        ViewDescriptor readDescriptor = getJsonFormDescriptor(entityClass);
        ResponseEntity<String> metadata = getMetadata(request, readDescriptor);
        if (metadata != null) {
            return metadata;
        }

        @SuppressWarnings("unchecked") Object result = crudService.find(entityClass, id);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Entity " + entityClass.getSimpleName() + " with id " + id + " not found\n");
        }

        return buildJsonResponse(readDescriptor, result, "OK");
    }

    /**
     * Builds a JSON response for a single entity, automatically resolving its
     * {@link ViewDescriptor} via {@link #getJsonFormDescriptor(Class)}.
     *
     * @param result the entity to serialize
     * @return a {@code 200 OK} JSON response
     */
    public static ResponseEntity<String> buildJsonResponse(Object result) {
        ViewDescriptor descriptor = getJsonFormDescriptor(result.getClass());
        return buildJsonResponse(descriptor, result, "ok");
    }

    /**
     * Builds a JSON response wrapping a single entity result inside a {@link SimpleResult}.
     * Falls back to generic JSON serialization when no {@link ViewDescriptor} is available.
     *
     * @param readDescriptor the view descriptor controlling field serialization; may be {@code null}
     * @param result         the entity to serialize
     * @param message        the status message included in the response body
     * @return a {@code 200 OK} JSON response
     */
    public static ResponseEntity<String> buildJsonResponse(ViewDescriptor readDescriptor, Object result, String message) {
        HttpHeaders headers = jsonHeaders();
        SimpleResult resultWrapper = new SimpleResult(result, message);
        if (readDescriptor != null) {
            return new ResponseEntity<>(new JsonView<>(resultWrapper, readDescriptor).renderJson(), headers, HttpStatus.OK);
        }
        return new ResponseEntity<>(StringPojoParser.convertPojoToJson(result), headers, HttpStatus.OK);
    }

    /**
     * Builds a JSON response for a paginated list result.
     * Falls back to generic JSON serialization when no {@link ViewDescriptor} is available.
     *
     * @param readDescriptor the view descriptor controlling field serialization; may be {@code null}
     * @param result         the {@link ListResult} to serialize
     * @param message        the status message (currently unused in the body for list results)
     * @return a {@code 200 OK} JSON response
     */
    public static ResponseEntity<String> buildJsonResponse(ViewDescriptor readDescriptor, ListResult result, String message) {
        HttpHeaders headers = jsonHeaders();
        if (readDescriptor != null) {
            return new ResponseEntity<>(new JsonView<>(result, readDescriptor).renderJson(), headers, HttpStatus.OK);
        }
        return new ResponseEntity<>(StringPojoParser.convertPojoToJson(result), headers, HttpStatus.OK);
    }

    /**
     * Resolves the JSON form {@link ViewDescriptor} for the given entity class,
     * without auto-creating one when none is found.
     *
     * @param entityClass the entity class to look up
     * @return the resolved {@link ViewDescriptor}, or {@code null} if not found
     */
    public static ViewDescriptor getJsonFormDescriptor(Class entityClass) {
        return getJsonFormDescriptor(entityClass, false);
    }

    /**
     * Resolves the JSON form {@link ViewDescriptor} for the given entity class.
     * The lookup order is: {@code json-form} → {@code json} → {@code form}.
     * When {@code autocreate} is {@code true} and no descriptor is found, a default
     * {@code form} descriptor is generated.
     *
     * @param entityClass the entity class to look up
     * @param autocreate  {@code true} to auto-generate a form descriptor when none exists
     * @return the resolved (or auto-generated) {@link ViewDescriptor}, or {@code null}
     */
    public static ViewDescriptor getJsonFormDescriptor(Class entityClass, boolean autocreate) {
        ViewDescriptor descriptor = Viewers.findViewDescriptor(entityClass, JSON_FORM);
        if (descriptor == null) {
            descriptor = Viewers.findViewDescriptor(entityClass, JSON);
        }
        if (descriptor == null) {
            descriptor = Viewers.findViewDescriptor(entityClass, FORM);
        }
        if (descriptor == null && autocreate) {
            descriptor = Viewers.getViewDescriptor(entityClass, FORM);
        }
        return descriptor;
    }

    /**
     * Resolves the JSON table {@link ViewDescriptor} for the given entity class.
     * The lookup order is: {@code json} → {@code tree} → {@code table}.
     *
     * @param entityClass the entity class to look up
     * @return the resolved {@link ViewDescriptor}, or {@code null} if none exists
     */
    public static ViewDescriptor getJsonTableDescriptor(Class entityClass) {
        ViewDescriptor descriptor = Viewers.findViewDescriptor(entityClass, JSON);
        if (descriptor == null) {
            descriptor = Viewers.findViewDescriptor(entityClass, "tree");
        }
        if (descriptor == null) {
            descriptor = Viewers.findViewDescriptor(entityClass, "table");
        }
        return descriptor;
    }

    /**
     * Creates a new entity by parsing the supplied JSON payload and persisting it
     * through {@link CrudService}.
     *
     * @param path     the navigation path resolving to a {@link CrudPage}
     * @param jsonData the JSON representation of the new entity
     * @param request  the current HTTP request (unused, reserved for future use)
     * @return a JSON response containing the newly created entity
     */
    private ResponseEntity<String> create(String path, String jsonData, HttpServletRequest request) {
        CrudPage page = findCrudPage(path);
        Class entityClass = page.getEntityClass();

        ViewDescriptor descriptor = getJsonFormDescriptor(entityClass, true);
        JsonView jsonView = new JsonView(descriptor);
        jsonView.parse(jsonData);
        Object newEntity = crudService.create(jsonView.getValue());

        return buildJsonResponse(descriptor, newEntity, "Created Successfully");
    }

    /**
     * Updates an existing entity by applying the JSON patch fields to the persistent
     * instance identified by {@code id}.
     *
     * @param path     the navigation path resolving to a {@link CrudPage}
     * @param id       the entity identifier
     * @param jsonData the JSON object containing the fields to update
     * @param request  the current HTTP request (unused, reserved for future use)
     * @return a JSON response with the updated entity, or HTTP 404 if not found
     */
    private ResponseEntity<String> update(String path, Long id, String jsonData, HttpServletRequest request) {
        CrudPage page = findCrudPage(path);
        Class entityClass = page.getEntityClass();

        @SuppressWarnings("unchecked") final Object entity = crudService.find(entityClass, id);
        if (entity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Entity " + entityClass.getSimpleName() + " with id " + id + " not found");
        }

        ViewDescriptor descriptor = getJsonFormDescriptor(entityClass, true);
        try {
            JsonNode node = StringPojoParser.createJsonMapper().readTree(jsonData);
            node.properties().forEach(entry -> {
                Field field = descriptor.getField(entry.getKey());
                if (field != null) {
                    Object fieldValue = JsonViewDescriptorDeserializer.getNodeValue(field.getPropertyInfo(), entry.getValue());
                    ObjectOperations.invokeSetMethod(entity, field.getPropertyInfo(), fieldValue);
                }
            });
        } catch (JacksonException e) {
            log("Error updating entity", e);
        }

        return buildJsonResponse(descriptor, crudService.update(entity), "Updated Successfully");
    }

    /**
     * Deletes the entity identified by {@code id} and returns its last known state.
     *
     * @param path    the navigation path resolving to a {@link CrudPage}
     * @param id      the entity identifier
     * @param request the current HTTP request (unused, reserved for future use)
     * @return a JSON response with the deleted entity's data, or HTTP 404 if not found
     */
    private ResponseEntity<String> delete(String path, Long id, HttpServletRequest request) {
        CrudPage page = findCrudPage(path);
        Class entityClass = page.getEntityClass();

        Object result = crudService.find(entityClass, id);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Entity " + entityClass.getSimpleName() + " with id " + id + " not found\n");
        }

        crudService.delete(entityClass, id);
        return buildJsonResponse(getJsonFormDescriptor(entityClass), result, "Deleted Successfully");
    }

    // -------------------------------------------------------------------------
    // Utility / helper methods
    // -------------------------------------------------------------------------

    /**
     * Applies any JPQL {@code conditions} declared in the {@link ViewDescriptor}'s parameters
     * to the given {@link QueryBuilder}. Each condition string is appended as an AND clause.
     *
     * @param query      the query builder to augment
     * @param descriptor the view descriptor that may contain a {@code conditions} parameter map
     */
    public static void parseConditions(QueryBuilder query, ViewDescriptor descriptor) {
        try {
            if (descriptor.getParams().containsKey("conditions")) {
                @SuppressWarnings("unchecked") Map<String, String> conditions =
                        (Map<String, String>) descriptor.getParams().get("conditions");
                conditions.forEach((k, v) -> query.and(v));
            }
        } catch (Exception e) {
            LoggingService.get(RestNavigationController.class).error("Error parsing conditions", e);
        }
    }

    /**
     * Reads an integer request parameter by name. Returns {@code 0} if the parameter
     * is absent or cannot be parsed as an integer.
     *
     * @param request the current HTTP request
     * @param name    the name of the request parameter
     * @return the parsed integer value, or {@code 0} if not present / invalid
     */
    public static int getParameterNumber(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
                // fall through and return 0
            }
        }
        return 0;
    }

    /**
     * Returns a JSON {@link ResponseEntity} containing the serialized {@link ViewDescriptor}
     * when the request includes the {@code _metadata} query parameter. Returns {@code null}
     * when the condition is not met, allowing normal processing to continue.
     *
     * @param request        the current HTTP request
     * @param viewDescriptor the descriptor to serialize; if {@code null} this method returns {@code null}
     * @return a metadata response, or {@code null} if not a metadata request
     */
    public static ResponseEntity<String> getMetadata(HttpServletRequest request, ViewDescriptor viewDescriptor) {
        if (viewDescriptor != null && request.getParameter("_metadata") != null) {
            try {
                return new ResponseEntity<>(StringPojoParser.createJsonMapper().writeValueAsString(viewDescriptor), HttpStatus.OK);
            } catch (JacksonException e) {
                return new ResponseEntity<>("ERROR: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return null;
    }

    /**
     * Resolves a {@link CrudPage} by navigation {@code path}, verifying access restrictions.
     * First attempts an exact path match, then falls back to a pretty/virtual-path lookup.
     *
     * @param path the navigation path to resolve
     * @return the matching {@link CrudPage}
     * @throws PageNotFoundException if the path does not resolve to a {@link CrudPage}
     *                               or the resolved page is of the wrong type
     */
    private CrudPage findCrudPage(String path) {
        Page page;
        try {
            page = moduleContainer.findPage(path);
        } catch (PageNotFoundException e) {
            page = moduleContainer.findPageByPrettyVirtualPath(path);
        }
        if (page instanceof CrudPage crudPage) {
            NavigationRestrictions.verifyAccess(page);
            return crudPage;
        }
        throw new PageNotFoundException("Invalid Path " + path);
    }

    /**
     * Creates a pre-configured {@link HttpHeaders} instance with
     * {@code Content-Type: application/json}.
     *
     * @return JSON HTTP headers
     */
    private static HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // -------------------------------------------------------------------------
    // Inner result types
    // -------------------------------------------------------------------------

    /**
     * Simple wrapper that pairs a single entity payload with a status message.
     */
    static class SimpleResult {

        private Object data;
        private String response;

        /**
         * Creates a new {@code SimpleResult}.
         *
         * @param content  the entity data to include in the response
         * @param response a human-readable status message
         */
        public SimpleResult(Object content, String response) {
            this.data = content;
            this.response = response;
        }

        /**
         * Returns the entity data payload.
         *
         * @return the data object
         */
        public Object getData() {
            return data;
        }

        /**
         * Sets the entity data payload.
         *
         * @param data the data object
         */
        public void setData(Object data) {
            this.data = data;
        }

        /**
         * Returns the status message.
         *
         * @return the response message
         */
        public String getResponse() {
            return response;
        }

        /**
         * Sets the status message.
         *
         * @param response the response message
         */
        public void setResponse(String response) {
            this.response = response;
        }
    }

    /**
     * Wraps a paginated list of entities together with optional pagination metadata.
     */
    static class ListResult {

        private List data;

        /** Pagination info; omitted from JSON when {@code null}. */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private DataPaginator pageable;

        private String response;

        /**
         * Returns the status message.
         *
         * @return the response message
         */
        public String getResponse() {
            return response;
        }

        /**
         * Sets the status message.
         *
         * @param response the response message
         */
        public void setResponse(String response) {
            this.response = response;
        }

        /**
         * Returns the list of entity records for the current page.
         *
         * @return the data list
         */
        public List getData() {
            return data;
        }

        /**
         * Sets the list of entity records.
         *
         * @param data the data list
         */
        public void setData(List data) {
            this.data = data;
        }

        /**
         * Returns the pagination metadata, or {@code null} if the result is not paginated.
         *
         * @return the {@link DataPaginator}, or {@code null}
         */
        public DataPaginator getPageable() {
            return pageable;
        }

        /**
         * Sets the pagination metadata.
         *
         * @param pageable the {@link DataPaginator} to attach
         */
        public void setPageable(DataPaginator pageable) {
            this.pageable = pageable;
        }
    }

}
