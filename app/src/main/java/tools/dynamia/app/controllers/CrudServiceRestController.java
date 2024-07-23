package tools.dynamia.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.dynamia.commons.StringPojoParser;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;

import java.io.Serializable;
import java.util.List;

@RestController
@RequestMapping(value = "/crud-service/{className}", consumes = "application/json", produces = "application/json")
public class CrudServiceRestController {

    private final CrudService crudService;

    private final ObjectMapper mapper = StringPojoParser.createJsonMapper();

    public CrudServiceRestController(CrudService crudService) {
        this.crudService = crudService;
    }


    @PostMapping
    @PutMapping
    public ResponseEntity<Object> createOrSave(@PathVariable String className, @RequestBody String json) {
        Object entity = parseJson(className, json);
        Object result = crudService.save(entity);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable String className, @PathVariable Serializable id) {
        Class entityClass = loadClass(className);
        Object entity = crudService.find(entityClass, id);
        if (entity != null) {
            crudService.delete(entityClass);
            return ResponseEntity.ok(id);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable String className, @PathVariable Serializable id) {
        Class entityClass = loadClass(className);
        Object result = crudService.find(entityClass, id);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/find")
    public ResponseEntity<List<Object>> find(@PathVariable String className, @RequestBody QueryParameters parameters) {
        Class entityClass = loadClass(className);
        List<Object> result = crudService.find(entityClass, parameters);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/id")
    public ResponseEntity<Object> getId(@PathVariable String className, @RequestBody QueryParameters parameters) {
        Class entityClass = loadClass(className);
        Object result = crudService.getId(entityClass, parameters);
        return ResponseEntity.ok(result);
    }


    private Object parseJson(String className, String json) {
        Class entityClass = loadClass(className);
        try {
            return mapper.readValue(json, entityClass);
        } catch (JsonProcessingException e) {
            throw new ValidationError("Error parsing json for entity class " + className, e);
        }
    }


    private Class loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ValidationError("Class not found " + className + " - " + e.getMessage(), e);
        }
    }
}
