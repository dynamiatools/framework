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
import tools.dynamia.commons.collect.PagedList;
import tools.dynamia.crud.CrudPage;
import tools.dynamia.domain.query.DataPaginator;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.util.QueryBuilder;
import tools.dynamia.navigation.PageNotFoundException;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.web.navigation.RestNavigationContext.ListResult;

import java.util.List;

/**
 * Handles the <em>read</em> operations for the REST navigation API.
 *
 * <p>Covers two HTTP verbs:</p>
 * <ul>
 *   <li><b>GET (collection)</b> — paginated list with static conditions, dynamic filters, and sorting.</li>
 *   <li><b>GET (single)</b> — individual entity retrieval by numeric ID.</li>
 * </ul>
 *
 * <p>Both operations honour the {@code _metadata} query parameter, which short-circuits the normal
 * response and returns the entity's {@link ViewDescriptor} as JSON instead.</p>
 *
 * @author Mario A. Serrano Leones
 * @see RestNavigationQuerySupport
 * @see RestNavigationContext
 */
public class RestNavigationReadOperation {

    /** Default page size when the client does not supply a {@code size} parameter. */
    private static final int DEFAULT_PAGINATION_SIZE = 50;

    private final RestNavigationContext ctx;

    /**
     * Constructs a new {@code RestNavigationReadOperation}.
     *
     * @param ctx the shared navigation context providing infrastructure dependencies
     */
    public RestNavigationReadOperation(RestNavigationContext ctx) {
        this.ctx = ctx;
    }

    // -------------------------------------------------------------------------
    // Read all (paginated collection)
    // -------------------------------------------------------------------------

    /**
     * Returns a paginated collection of entities for the {@link CrudPage} resolved from {@code path}.
     *
     * <p>Processing pipeline:</p>
     * <ol>
     *   <li>Resolve and access-check the {@link CrudPage}.</li>
     *   <li>Short-circuit with metadata when {@code _metadata} is present.</li>
     *   <li>Apply static conditions from the {@link ViewDescriptor}.</li>
     *   <li>Apply dynamic field filters from request parameters.</li>
     *   <li>Apply dynamic ordering from {@code _sort} / {@code _order}.</li>
     *   <li>Paginate using {@code page} / {@code size}.</li>
     * </ol>
     *
     * @param path    the navigation path resolving to a {@link CrudPage}
     * @param request the current HTTP request
     * @return a paginated JSON response, or a metadata JSON response when {@code _metadata} is requested
     */
    public ResponseEntity<String> readAll(String path, HttpServletRequest request) {
        CrudPage page = ctx.findCrudPage(path);
        Class entityClass = page.getEntityClass();

        ViewDescriptor descriptor = RestNavigationContext.getJsonTableDescriptor(entityClass);
        ResponseEntity<String> metadata = RestNavigationContext.getMetadata(request, descriptor);
        if (metadata != null) {
            return metadata;
        }

        QueryBuilder query = QueryBuilder.select().from(entityClass, "e");
        QueryParameters pageParams = (QueryParameters) page.getAttribute("queryParameters");
        if (pageParams != null) {
            query.where(pageParams);
        }

        RestNavigationQuerySupport.parseConditions(query, descriptor);
        RestNavigationQuerySupport.applyRequestFilters(request, query, descriptor);
        RestNavigationQuerySupport.applyRequestSorting(request, query, descriptor);

        int pageSize = RestNavigationQuerySupport.getParameterNumber(request, "size");
        int currentPage = RestNavigationQuerySupport.getParameterNumber(request, "page");
        if (pageSize == 0) {
            pageSize = DEFAULT_PAGINATION_SIZE;
        }
        query.getQueryParameters().paginate(pageSize);

        List content = ctx.getCrudService().executeQuery(query);
        DataPaginator paginator = query.getQueryParameters().getPaginator();
        if (paginator != null) {
            paginator.setPage(currentPage);
        }

        return RestNavigationContext.buildJsonResponse(descriptor, buildListResult(content, paginator, currentPage), "OK");
    }

    // -------------------------------------------------------------------------
    // Read one (single entity)
    // -------------------------------------------------------------------------

    /**
     * Returns a single entity identified by {@code id} from the {@link CrudPage} resolved by {@code path}.
     *
     * @param path    the navigation path resolving to a {@link CrudPage}
     * @param id      the entity identifier
     * @param request the current HTTP request
     * @return a JSON response with the entity data
     * @throws PageNotFoundException if no entity with the given {@code id} exists
     */
    public ResponseEntity<String> readOne(String path, Long id, HttpServletRequest request) {
        CrudPage page = ctx.findCrudPage(path);
        Class entityClass = page.getEntityClass();

        ViewDescriptor descriptor = RestNavigationContext.getJsonFormDescriptor(entityClass);
        ResponseEntity<String> metadata = RestNavigationContext.getMetadata(request, descriptor);
        if (metadata != null) {
            return metadata;
        }

        @SuppressWarnings("unchecked") Object result = ctx.getCrudService().find(entityClass, id);
        if (result == null) {
            throw new PageNotFoundException(entityClass.getSimpleName() + " with id " + id + " not found");
        }

        return RestNavigationContext.buildJsonResponse(descriptor, result, "OK");
    }

    // -------------------------------------------------------------------------
    // ListResult builder (also used by other callers)
    // -------------------------------------------------------------------------

    /**
     * Builds a {@link ListResult} from a raw content list, handling paged data sources
     * when the content is a {@link PagedList}.
     *
     * @param content    the raw list returned by the query
     * @param paginator  the {@link DataPaginator} associated with the query; may be {@code null}
     * @param currentPage the requested page number (1-based); ignored when {@code <= 0}
     * @return a populated {@link ListResult} ready for serialization
     */
    public static ListResult buildListResult(List content, DataPaginator paginator, int currentPage) {
        ListResult result = new ListResult();
        if (content instanceof PagedList pagedList && paginator != null) {
            if (currentPage > 0) {
                pagedList.getDataSource().setActivePage(currentPage);
            }
            result.setData(pagedList.getDataSource().getPageData());
            result.setPageable(paginator);
        } else {
            result.setData(content);
        }
        result.setResponse("OK");
        return result;
    }
}

