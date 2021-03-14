/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
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

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
@Order(1000)
public class RestNavigationController {

    private final static String PAGE = "{page:[a-z\\-]+}";
    private final static String ID = "{id:[0-9]+}";
    private final static int DEFAULT_PAGINATION_SIZE = 100;

    @Autowired
    private CrudService crudService;

    //READ ALL

    @GetMapping("/{module}/" + PAGE)
    public ResponseEntity<String> readAll(@PathVariable String module, @PathVariable String page, HttpServletRequest request) {

        String path = module + "/" + page;
        return readAll(path, request);

    }

    @GetMapping("/{module}/{group}/" + PAGE)
    public ResponseEntity<String> readAll(@PathVariable("module") String module, @PathVariable("group") String group,
                                          @PathVariable("page") String page, HttpServletRequest request) {
        String path = module + "/" + group + "/" + page;
        return readAll(path, request);
    }

    @GetMapping("/{module}/{group}/{subgroup}/" + PAGE)
    public ResponseEntity<String> readAll(@PathVariable("module") String module, @PathVariable("group") String group,
                                          @PathVariable("subgroup") String subgroup, @PathVariable("page") String page,
                                          HttpServletRequest request) {

        String path = module + "/" + group + "/" + subgroup + "/" + page;
        return readAll(path, request);

    }

    //READ ONE

    @GetMapping("/{module}/{page}/" + ID)
    public ResponseEntity<String> readOne(@PathVariable String module, @PathVariable String page, @PathVariable Long id, HttpServletRequest request) {

        String path = module + "/" + page;
        return readOne(path, id, request);

    }

    @GetMapping("/{module}/{group}/{page}/" + ID)
    public ResponseEntity<String> readOne(@PathVariable String module, @PathVariable String group, @PathVariable String page,
                                          @PathVariable Long id, HttpServletRequest request) {
        String path = module + "/" + group + "/" + page;
        return readOne(path, id, request);
    }

    @GetMapping("/{module}/{group}/{subgroup}/{page}/" + ID)
    public ResponseEntity<String> readOne(@PathVariable String module, @PathVariable String group,
                                          @PathVariable String subgroup, @PathVariable String page, @PathVariable Long id, HttpServletRequest request) {

        String path = module + "/" + group + "/" + subgroup + "/" + page;
        return readOne(path, id, request);

    }

    //CREATE

    @PostMapping("/{module}/" + PAGE)
    public ResponseEntity<String> create(@PathVariable String module, @PathVariable String page, @RequestBody String jsonData, HttpServletRequest request) {

        String path = module + "/" + page;
        return create(path, jsonData, request);

    }

    @PostMapping("/{module}/{group}/" + PAGE)
    public ResponseEntity<String> create(@PathVariable String module, @PathVariable String group, @PathVariable String page, @RequestBody String jsonData,
                                         HttpServletRequest request) {
        String path = module + "/" + group + "/" + page;
        return create(path, jsonData, request);
    }

    @PostMapping("/{module}/{group}/{subgroup}/" + PAGE)
    public ResponseEntity<String> create(@PathVariable String module, @PathVariable String group, @PathVariable String subgroup,
                                         @PathVariable String page, @RequestBody String jsonData, HttpServletRequest request) {

        String path = module + "/" + group + "/" + subgroup + "/" + page;
        return create(path, jsonData, request);

    }


    //UPDATE

    @PutMapping("/{module}/{page}/" + ID)
    public ResponseEntity<String> update(@PathVariable String module, @PathVariable String page, @PathVariable Long id,
                                         @RequestBody String jsonData, HttpServletRequest request) {

        String path = module + "/" + page;
        return update(path, id, jsonData, request);

    }

    @PutMapping("/{module}/{group}/{page}/" + ID)
    public ResponseEntity<String> update(@PathVariable String module, @PathVariable String group, @PathVariable String page,
                                         @PathVariable Long id, @RequestBody String jsonData, HttpServletRequest request) {
        String path = module + "/" + group + "/" + page;
        return update(path, id, jsonData, request);
    }

    @PutMapping("/{module}/{group}/{subgroup}/{page}/" + ID)
    public ResponseEntity<String> update(@PathVariable String module, @PathVariable String group,
                                         @PathVariable String subgroup, @PathVariable String page, @PathVariable Long id,
                                         @RequestBody String jsonData, HttpServletRequest request) {

        String path = module + "/" + group + "/" + subgroup + "/" + page;
        return update(path, id, jsonData, request);

    }


    //DELETE

    @DeleteMapping("/{module}/{page}/{id}")
    public ResponseEntity<String> delete(@PathVariable String module, @PathVariable String page, @PathVariable Long id, HttpServletRequest request) {

        String path = module + "/" + page;
        return delete(path, id, request);

    }

    @DeleteMapping("/{module}/{group}/{page}/{id}")
    public ResponseEntity<String> delete(@PathVariable String module, @PathVariable String group, @PathVariable String page,
                                         @PathVariable Long id, HttpServletRequest request) {
        String path = module + "/" + group + "/" + page;
        return delete(path, id, request);
    }

    @DeleteMapping("/{module}/{group}/{subgroup}/{page}/{id}")
    public ResponseEntity<String> delete(@PathVariable String module, @PathVariable String group,
                                         @PathVariable String subgroup, @PathVariable String page, @PathVariable Long id, HttpServletRequest request) {

        String path = module + "/" + group + "/" + subgroup + "/" + page;
        return delete(path, id, request);

    }

    //INTERNAL OPERATIONS

    private ResponseEntity<String> readAll(String path, HttpServletRequest request) {

        CrudPage page = findCrudPage(path);
        Class entityClass = page.getEntityClass();

        ViewDescriptor readDescriptor = Viewers.findViewDescriptor(entityClass, "json");
        if (readDescriptor == null) {
            readDescriptor = Viewers.findViewDescriptor(entityClass, "tree");
        }

        if (readDescriptor == null) {
            readDescriptor = Viewers.getViewDescriptor(entityClass, "table");

        }
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


        ListResult result = new ListResult();

        List content = crudService.executeQuery(query);
        if (content instanceof PagedList) {
            PagedList pagedList = (PagedList) content;
            if (currentPage > 0) {
                pagedList.getDataSource().setActivePage(currentPage);
            }
            content = pagedList.getDataSource().getPageData();
            result.setPageable(query.getQueryParameters().getPaginator());
        }
        result.setResponse("OK");
        result.setData(content);


        return new ResponseEntity<>(new JsonView<>(result, readDescriptor).renderJson(), HttpStatus.OK);
    }

    private ResponseEntity<String> readOne(String path, Long id, HttpServletRequest request) {

        CrudPage page = findCrudPage(path);
        Class entityClass = page.getEntityClass();

        ViewDescriptor readDescriptor = Viewers.findViewDescriptor(entityClass, "json-form");

        if (readDescriptor == null) {
            readDescriptor = Viewers.getViewDescriptor(entityClass, "json");
        }

        if (readDescriptor == null) {
            readDescriptor = Viewers.getViewDescriptor(entityClass, "form");
        }
        ResponseEntity<String> metadata = getMetadata(request, readDescriptor);
        if (metadata != null) {
            return metadata;
        }

        Object result = crudService.find(entityClass, id);
        if (result == null) {
            return new ResponseEntity<>("Entity " + entityClass.getSimpleName() + " with id " + id + " not found\n", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new JsonView<>(new SimpleResult(result, "OK"), readDescriptor).renderJson(), HttpStatus.OK);
    }

    private ResponseEntity<String> create(String path, String jsonData, HttpServletRequest request) {

        CrudPage page = findCrudPage(path);
        Class entityClass = page.getEntityClass();


        ViewDescriptor descriptor = Viewers.findViewDescriptor(entityClass, "json-form");
        if (descriptor == null) {
            descriptor = Viewers.getViewDescriptor(entityClass, "form");
        }
        JsonView jsonView = new JsonView(descriptor);
        jsonView.parse(jsonData);
        Object newEntity = jsonView.getValue();

        newEntity = crudService.create(newEntity);

        return new ResponseEntity<>(new JsonView<>(new SimpleResult(newEntity, "Created Successfully"), descriptor).renderJson(), HttpStatus.OK);
    }

    private ResponseEntity<String> update(String path, Long id, String jsonData, HttpServletRequest request) {

        CrudPage page = findCrudPage(path);
        Class entityClass = page.getEntityClass();

        final Object entity = crudService.find(entityClass, id);
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
        return new ResponseEntity<>(new JsonView<>(new SimpleResult(updatedEntity, "Updated Successfully"), descriptor).renderJson(), HttpStatus.OK);
    }

    private ResponseEntity<String> delete(String path, Long id, HttpServletRequest request) {

        CrudPage page = findCrudPage(path);
        Class entityClass = page.getEntityClass();

        ViewDescriptor readDescriptor = Viewers.getViewDescriptor(entityClass, "form");

        Object result = crudService.find(entityClass, id);
        if (result == null) {
            return new ResponseEntity<>("Entity " + entityClass.getSimpleName() + " with id " + id + " not found\n", HttpStatus.NOT_FOUND);
        }
        crudService.delete(entityClass, id);

        return new ResponseEntity<>(new JsonView<>(new SimpleResult(result, "Deleted Successfully"), readDescriptor).renderJson(), HttpStatus.OK);
    }


    private void parseConditions(QueryBuilder query, ViewDescriptor descriptor) {
        try {
            if (descriptor.getParams().containsKey("conditions")) {
                Map<String, String> conditions = (Map<String, String>) descriptor.getParams().get("conditions");
                conditions.forEach((k, v) -> query.and(v));
            }
        } catch (Exception e) {

        }
    }

    private int getParameterNumber(HttpServletRequest request, String name) {
        int param = 0;
        if (request.getParameter(name) != null) {
            try {
                param = Integer.parseInt(request.getParameter(name));
            } catch (NumberFormatException e) {

            }
        }
        return param;
    }

    public ResponseEntity<String> getMetadata(HttpServletRequest request, ViewDescriptor viewDescriptor) {
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
        Page page = NavigationManager.getCurrent().findPage(path);
        if (page == null) {
            page = NavigationManager.getCurrent().findPageByPrettyVirtualPath(path);
        }
        if (page instanceof CrudPage) {
            NavigationRestrictions.verifyAccess(page);
            return (CrudPage) page;
        }
        throw new PageNotFoundException("Invalid Path " + path);
    }

    class SimpleResult {
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

    class ListResult {
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
