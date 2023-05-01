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
package tools.dynamia.app.controllers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.collect.PagedList;
import tools.dynamia.crud.CrudPage;
import tools.dynamia.domain.query.DataPaginator;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.QueryBuilder;
import tools.dynamia.navigation.NavigationManager;
import tools.dynamia.navigation.NavigationRestrictions;
import tools.dynamia.navigation.Page;
import tools.dynamia.navigation.PageNotFoundException;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.JsonView;
import tools.dynamia.viewers.JsonViewDescriptorDeserializer;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.util.Viewers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController("restNavigationController")
@Order(1000)
public class RestNavigationController {


    private final static int DEFAULT_PAGINATION_SIZE = 50;

    @Autowired
    private CrudService crudService;


    private String getPath(HttpServletRequest request) {
        var path = request.getRequestURI();
        if (path.startsWith("/api/")) {
            path = path.replaceFirst("/api/", "");
        }
        return path;
    }


    public ResponseEntity<String> routeReadAll(HttpServletRequest request) {
        String path = getPath(request);
        return readAll(path, request);
    }

    public ResponseEntity<String> routeReadOne(@PathVariable Long id, HttpServletRequest request) {
        return readOne(getPath(request).replace("/" + id, ""), id, request);
    }


    public ResponseEntity<String> routeCreate(@RequestBody String jsonData, HttpServletRequest request) {
        String path = getPath(request);
        return create(path, jsonData, request);

    }

    public ResponseEntity<String> routeUpdate(@PathVariable Long id, @RequestBody String jsonData, HttpServletRequest request) {
        String path = getPath(request).replace("/" + id, "");
        return update(path, id, jsonData, request);
    }

    public ResponseEntity<String> routeDelete(@PathVariable Long id, HttpServletRequest request) {
        String path = getPath(request).replace("/" + id, "");
        return delete(path, id, request);

    }


    //INTERNAL OPERATIONS

    private ResponseEntity<String> readAll(String path, HttpServletRequest request) {

        CrudPage page = findCrudPage(path);
        Class entityClass = page.getEntityClass();

        ViewDescriptor readDescriptor = getJsonTableDescriptor(entityClass);
        ResponseEntity<String> metadata = getMetadata(request, readDescriptor);
        if (metadata != null) {
            return metadata;
        }

        QueryBuilder query = QueryBuilder.select().from(entityClass, "e");
        if (page.getAttribute("queryParameters") != null) {
            QueryParameters pageParams = (QueryParameters) page.getAttribute("queryParameters");
            query.where(pageParams);
        }
        parseConditions(query, readDescriptor);

        int pageSize = getParameterNumber(request, "size");
        int currentPage = getParameterNumber(request, "page");
        if (pageSize == 0) {
            pageSize = DEFAULT_PAGINATION_SIZE;
        }
        if (pageSize > 0) {
            query.getQueryParameters().paginate(pageSize);
        }


        List content = crudService.executeQuery(query);
        var paginator = query.getQueryParameters().getPaginator();
        if (paginator != null) {
            paginator.setPage(currentPage);
        }

        ListResult result = buildListResult(content, paginator, currentPage);


        return buildJsonResponse(readDescriptor, result, "OK");
    }

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
            return new ResponseEntity<>("Entity " + entityClass.getSimpleName() + " with id " + id + " not found\n", HttpStatus.NOT_FOUND);
        }

        return buildJsonResponse(readDescriptor, result, "OK");
    }

    public static ResponseEntity<String> buildJsonResponse(Object result) {
        var descriptor = getJsonFormDescriptor(result.getClass());
        return buildJsonResponse(descriptor, result, "ok");
    }

    public static ResponseEntity<String> buildJsonResponse(ViewDescriptor readDescriptor, Object result, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(new JsonView<>(new SimpleResult(result, message), readDescriptor).renderJson(), headers, HttpStatus.OK);
    }

    public static ResponseEntity<String> buildJsonResponse(ViewDescriptor readDescriptor, ListResult result, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(new JsonView<>(result, readDescriptor).renderJson(), headers, HttpStatus.OK);
    }

    public static ViewDescriptor getJsonFormDescriptor(Class entityClass) {
        ViewDescriptor readDescriptor = Viewers.findViewDescriptor(entityClass, "json-form");

        if (readDescriptor == null) {
            readDescriptor = Viewers.getViewDescriptor(entityClass, "json");
        }

        if (readDescriptor == null) {
            readDescriptor = Viewers.getViewDescriptor(entityClass, "form");
        }
        return readDescriptor;
    }

    public static ViewDescriptor getJsonTableDescriptor(Class entityClass) {
        ViewDescriptor readDescriptor = Viewers.findViewDescriptor(entityClass, "json");
        if (readDescriptor == null) {
            readDescriptor = Viewers.findViewDescriptor(entityClass, "tree");
        }

        if (readDescriptor == null) {
            readDescriptor = Viewers.getViewDescriptor(entityClass, "table");

        }
        return readDescriptor;
    }


    private ResponseEntity<String> create(String path, String jsonData, HttpServletRequest request) {

        CrudPage page = findCrudPage(path);
        Class entityClass = page.getEntityClass();


        ViewDescriptor descriptor = getJsonFormDescriptor(entityClass);

        JsonView jsonView = new JsonView(descriptor);
        jsonView.parse(jsonData);
        Object newEntity = jsonView.getValue();

        newEntity = crudService.create(newEntity);

        return buildJsonResponse(descriptor, newEntity, "Created Successfully");
    }

    private ResponseEntity<String> update(String path, Long id, String jsonData, HttpServletRequest request) {

        CrudPage page = findCrudPage(path);
        Class entityClass = page.getEntityClass();

        @SuppressWarnings("unchecked") final Object entity = crudService.find(entityClass, id);
        if (entity == null) {
            return new ResponseEntity<>("Entity " + entityClass.getSimpleName() + " with id " + id + " not found", HttpStatus.NOT_FOUND);
        }


        ViewDescriptor descriptor = Viewers.findViewDescriptor(entityClass, "json");
        if (descriptor == null) {
            descriptor = Viewers.getViewDescriptor(entityClass, "form");
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            final ViewDescriptor desc = descriptor;
            JsonNode node = mapper.readTree(jsonData);
            System.out.println("TYPE: " + node.getNodeType());
            node.fields().forEachRemaining(entry -> {
                Field field = desc.getField(entry.getKey());
                if (field != null) {
                    Object fieldValue = JsonViewDescriptorDeserializer.getNodeValue(field.getPropertyInfo(), entry.getValue());
                    BeanUtils.invokeSetMethod(entity, field.getPropertyInfo(), fieldValue);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }


        Object updatedEntity = crudService.update(entity);
        return buildJsonResponse(descriptor, updatedEntity, "Updated Successfully");
    }

    private ResponseEntity<String> delete(String path, Long id, HttpServletRequest request) {

        CrudPage page = findCrudPage(path);
        Class entityClass = page.getEntityClass();

        ViewDescriptor readDescriptor = Viewers.getViewDescriptor(entityClass, "form");

        @SuppressWarnings("unchecked") Object result = crudService.find(entityClass, id);
        if (result == null) {
            return new ResponseEntity<>("Entity " + entityClass.getSimpleName() + " with id " + id + " not found\n", HttpStatus.NOT_FOUND);
        }
        crudService.delete(entityClass, id);

        return buildJsonResponse(readDescriptor, result, "Deleted Successfully");
    }


    public static void parseConditions(QueryBuilder query, ViewDescriptor descriptor) {
        try {
            if (descriptor.getParams().containsKey("conditions")) {
                @SuppressWarnings("unchecked") Map<String, String> conditions = (Map<String, String>) descriptor.getParams().get("conditions");
                conditions.forEach((k, v) -> query.and(v));
            }
        } catch (Exception ignored) {

        }
    }

    public static int getParameterNumber(HttpServletRequest request, String name) {
        int param = 0;
        if (request.getParameter(name) != null) {
            try {
                param = Integer.parseInt(request.getParameter(name));
            } catch (NumberFormatException ignored) {

            }
        }
        return param;
    }

    public static ResponseEntity<String> getMetadata(HttpServletRequest request, ViewDescriptor viewDescriptor) {
        if (request.getParameter("_metadata") != null) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);


            try {
                return new ResponseEntity<>(mapper.writeValueAsString(viewDescriptor), HttpStatus.OK);
            } catch (JsonProcessingException e) {
                return new ResponseEntity<>("ERROR: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return null;
    }


    private CrudPage findCrudPage(String path) {

        Page page = null;
        try {
            page = NavigationManager.getCurrent().findPage(path);
        } catch (PageNotFoundException e) {
            page = NavigationManager.getCurrent().findPageByPrettyVirtualPath(path);
        }
        if (page instanceof CrudPage) {
            NavigationRestrictions.verifyAccess(page);
            return (CrudPage) page;
        }
        throw new PageNotFoundException("Invalid Path " + path);
    }

    static class SimpleResult {
        private Object data;
        private String response;

        public SimpleResult(Object content, String response) {
            this.data = content;
            this.response = response;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }
    }

    static class ListResult {
        private List data;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private DataPaginator pageable;
        private String response;

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }

        public List getData() {
            return data;
        }

        public void setData(List data) {
            this.data = data;
        }

        public DataPaginator getPageable() {
            return pageable;
        }

        public void setPageable(DataPaginator pageable) {
            this.pageable = pageable;
        }
    }

}
