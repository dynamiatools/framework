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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import tools.dynamia.commons.StringPojoParser;
import tools.dynamia.crud.CrudPage;
import tools.dynamia.domain.query.DataPaginator;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.navigation.ModuleContainer;
import tools.dynamia.navigation.NavigationRestrictions;
import tools.dynamia.navigation.Page;
import tools.dynamia.navigation.PageNotFoundException;
import tools.dynamia.viewers.JsonView;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.util.Viewers;
import tools.jackson.core.JacksonException;

import java.util.List;

/**
 * Shared context for all REST navigation operation classes.
 *
 * <p>Holds the two required infrastructure dependencies ({@link ModuleContainer} and
 * {@link CrudService}) and exposes the common utility operations used by every CRUD
 * operation class:</p>
 * <ul>
 *   <li>Navigation path extraction from the raw request URI</li>
 *   <li>{@link CrudPage} resolution with access-restriction enforcement</li>
 *   <li>{@link ViewDescriptor} lookup (form / table variants)</li>
 *   <li>JSON {@link ResponseEntity} construction helpers</li>
 *   <li>{@code _metadata} short-circuit response</li>
 * </ul>
 *
 * <p>The inner DTOs ({@link SimpleResult} and {@link ListResult}) live here so they are
 * accessible to every operation in the same package without being part of the public API
 * of the controller itself.</p>
 *
 * @author Mario A. Serrano Leones
 */
public class RestNavigationContext {

    static final String JSON_FORM = "json-form";
    static final String JSON = "json";
    static final String FORM = "form";

    private final ModuleContainer moduleContainer;
    private final CrudService crudService;

    /**
     * Constructs a new {@code RestNavigationContext}.
     *
     * @param moduleContainer the container used to resolve navigation pages by path
     * @param crudService     the service used to perform CRUD persistence operations
     */
    public RestNavigationContext(ModuleContainer moduleContainer, CrudService crudService) {
        this.moduleContainer = moduleContainer;
        this.crudService = crudService;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /**
     * Returns the {@link ModuleContainer} used to look up navigation pages.
     *
     * @return the module container
     */
    public ModuleContainer getModuleContainer() {
        return moduleContainer;
    }

    /**
     * Returns the {@link CrudService} used to execute persistence operations.
     *
     * @return the crud service
     */
    public CrudService getCrudService() {
        return crudService;
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
    public String getPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.startsWith("/api/")) {
            path = path.replaceFirst("/api/", "");
        }
        return path;
    }

    // -------------------------------------------------------------------------
    // Page resolution
    // -------------------------------------------------------------------------

    /**
     * Resolves a {@link CrudPage} by navigation {@code path}, verifying access restrictions.
     * First attempts an exact path match, then falls back to a pretty/virtual-path lookup.
     *
     * @param path the navigation path to resolve
     * @return the matching {@link CrudPage}
     * @throws PageNotFoundException if the path does not resolve to a {@link CrudPage}
     *                               or the resolved page is of the wrong type
     */
    public CrudPage findCrudPage(String path) {
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

    // -------------------------------------------------------------------------
    // ViewDescriptor lookup
    // -------------------------------------------------------------------------

    /**
     * Resolves the JSON form {@link ViewDescriptor} for the given entity class,
     * without auto-creating one when none is found.
     *
     * <p>Lookup order: {@code json-form} → {@code json} → {@code form}.</p>
     *
     * @param entityClass the entity class to look up
     * @return the resolved {@link ViewDescriptor}, or {@code null} if not found
     */
    public static ViewDescriptor getJsonFormDescriptor(Class entityClass) {
        return getJsonFormDescriptor(entityClass, false);
    }

    /**
     * Resolves the JSON form {@link ViewDescriptor} for the given entity class.
     *
     * <p>Lookup order: {@code json-form} → {@code json} → {@code form}.
     * When {@code autocreate} is {@code true} and no descriptor is found, a default
     * {@code form} descriptor is generated.</p>
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
     *
     * <p>Lookup order: {@code json} → {@code tree} → {@code table}.</p>
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

    // -------------------------------------------------------------------------
    // JSON response builders
    // -------------------------------------------------------------------------

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
     * Builds a JSON response wrapping a single entity inside a {@link SimpleResult}.
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
     * @param message        the status message (reserved for future use)
     * @return a {@code 200 OK} JSON response
     */
    public static ResponseEntity<String> buildJsonResponse(ViewDescriptor readDescriptor, ListResult result, String message) {
        HttpHeaders headers = jsonHeaders();
        if (readDescriptor != null) {
            return new ResponseEntity<>(new JsonView<>(result, readDescriptor).renderJson(), headers, HttpStatus.OK);
        }
        return new ResponseEntity<>(StringPojoParser.convertPojoToJson(result), headers, HttpStatus.OK);
    }

    // -------------------------------------------------------------------------
    // Metadata helper
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Creates a pre-configured {@link HttpHeaders} instance with
     * {@code Content-Type: application/json}.
     *
     * @return JSON HTTP headers
     */
    static HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // -------------------------------------------------------------------------
    // Inner DTOs
    // -------------------------------------------------------------------------

    /**
     * Simple wrapper that pairs a single entity payload with a status message.
     */
    public static class SimpleResult {

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

        /** Returns the entity data payload. */
        public Object getData() { return data; }

        /** Sets the entity data payload. */
        public void setData(Object data) { this.data = data; }

        /** Returns the status message. */
        public String getResponse() { return response; }

        /** Sets the status message. */
        public void setResponse(String response) { this.response = response; }
    }

    /**
     * Wraps a paginated list of entities together with optional pagination metadata.
     */
    public static class ListResult {

        private List data;

        /** Pagination info; omitted from JSON when {@code null}. */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private DataPaginator pageable;

        private String response;

        /** Returns the status message. */
        public String getResponse() { return response; }

        /** Sets the status message. */
        public void setResponse(String response) { this.response = response; }

        /** Returns the list of entity records for the current page. */
        public List getData() { return data; }

        /** Sets the list of entity records. */
        public void setData(List data) { this.data = data; }

        /**
         * Returns the pagination metadata, or {@code null} if the result is not paginated.
         *
         * @return the {@link DataPaginator}, or {@code null}
         */
        public DataPaginator getPageable() { return pageable; }

        /** Sets the pagination metadata. */
        public void setPageable(DataPaginator pageable) { this.pageable = pageable; }
    }
}

