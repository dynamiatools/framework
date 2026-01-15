package tools.dynamia.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.dynamia.commons.StringPojoParser;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;

import java.io.Serializable;
import java.util.List;

/**
 * REST controller for generic CRUD operations on entities.
 * <p>
 * Provides endpoints for creating, updating, deleting, retrieving, and searching entities by class name.
 * Uses {@link CrudService} for persistence and supports dynamic entity types via class name resolution.
 * <p>
 * Endpoints:
 * <ul>
 *   <li>POST/PUT /crud-service/{className} - Create or update entity</li>
 *   <li>DELETE /crud-service/{className}/{id} - Delete entity by ID</li>
 *   <li>GET /crud-service/{className}/{id} - Get entity by ID</li>
 *   <li>POST /crud-service/{className}/find - Find entities by query parameters</li>
 *   <li>POST /crud-service/{className}/id - Get entity ID by query parameters</li>
 * </ul>
 *
 * @author Mario A. Serrano Leones
 * @since 2023
 */
@RestController
@RequestMapping(value = "/crud-service/{className}", consumes = "application/json", produces = "application/json")
@Tag(name = "DynamiaCrudService")
public class CrudServiceRestController {

    /**
     * Service for CRUD operations.
     */
    private final CrudService crudService;
    /**
     * JSON object mapper for entity serialization/deserialization.
     */
    private final JsonMapper mapper = StringPojoParser.createJsonMapper();

    /**
     * Constructs a new {@code CrudServiceRestController} with the given CRUD service.
     * @param crudService the CRUD service to use
     */
    public CrudServiceRestController(CrudService crudService) {
        this.crudService = crudService;
    }

    /**
     * Creates or updates an entity of the specified class.
     * @param className the fully qualified class name of the entity
     * @param json the JSON representation of the entity
     * @return the persisted entity
     */
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<Object> createOrSave(@PathVariable String className, @RequestBody String json) {
        Object entity = parseJson(className, json);
        Object result = crudService.save(entity);
        return ResponseEntity.ok(result);
    }

    /**
     * Deletes an entity by its class name and ID.
     * @param className the fully qualified class name of the entity
     * @param id the entity ID
     * @return the deleted entity ID if successful, or 404 if not found
     */
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

    /**
     * Retrieves an entity by its class name and ID.
     * @param className the fully qualified class name of the entity
     * @param id the entity ID
     * @return the entity if found, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable String className, @PathVariable Serializable id) {
        Class entityClass = loadClass(className);
        Object result = crudService.find(entityClass, id);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Finds entities by query parameters.
     * @param className the fully qualified class name of the entity
     * @param parameters the query parameters
     * @return the list of matching entities
     */
    @PostMapping("/find")
    public ResponseEntity<List<Object>> find(@PathVariable String className, @RequestBody QueryParameters parameters) {
        Class entityClass = loadClass(className);
        List<Object> result = crudService.find(entityClass, parameters);
        return ResponseEntity.ok(result);
    }

    /**
     * Gets the ID of an entity by query parameters.
     * @param className the fully qualified class name of the entity
     * @param parameters the query parameters
     * @return the entity ID
     */
    @PostMapping("/id")
    public ResponseEntity<Object> getId(@PathVariable String className, @RequestBody QueryParameters parameters) {
        Class entityClass = loadClass(className);
        Object result = crudService.getId(entityClass, parameters);
        return ResponseEntity.ok(result);
    }

    /**
     * Parses a JSON string into an entity object of the specified class.
     * @param className the fully qualified class name
     * @param json the JSON string
     * @return the entity object
     * @throws ValidationError if parsing fails
     */
    private Object parseJson(String className, String json) {
        Class entityClass = loadClass(className);
        try {
            return mapper.readValue(json, entityClass);
        } catch (JsonProcessingException e) {
            throw new ValidationError("Error parsing json for entity class " + className, e);
        }
    }

    /**
     * Loads a class by its fully qualified name.
     * @param className the class name
     * @return the {@link Class} object
     * @throws ValidationError if the class is not found
     */
    private Class loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ValidationError("Class not found " + className + " - " + e.getMessage(), e);
        }
    }
}
