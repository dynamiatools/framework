package tools.dynamia.app.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tools.dynamia.actions.ActionExecutionRequest;
import tools.dynamia.actions.ActionExecutionResponse;
import tools.dynamia.actions.ActionRestrictions;
import tools.dynamia.actions.Actions;
import tools.dynamia.app.metadata.*;
import tools.dynamia.commons.SimpleCache;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.navigation.NavigationTree;
import tools.dynamia.viewers.ViewDescriptor;

import java.util.List;

/**
 * REST controller for exposing application metadata, navigation, entities, and actions.
 * <p>
 * Provides endpoints to retrieve metadata about the application, navigation tree, global actions, entities, and to execute actions.
 * Caches metadata for performance and uses {@link ApplicationMetadataLoader} to load data.
 * <p>
 * Endpoints:
 * <ul>
 *   <li>GET /api/app/metadata - Application metadata</li>
 *   <li>GET /api/app/metadata/navigation - Navigation tree</li>
 *   <li>GET /api/app/metadata/actions - Global actions metadata</li>
 *   <li>POST /api/app/metadata/actions/{action} - Execute global action</li>
 *   <li>GET /api/app/metadata/entities - Entities metadata</li>
 *   <li>GET /api/app/metadata/entities/{className} - Metadata for a specific entity</li>
 * </ul>
 *
 * @author Mario A. Serrano Leones
 * @since 2023
 */
@RestController
@RequestMapping(value = ApplicationMetadataController.PATH, produces = "application/json")
@Tag(name = "DynamiaApplicationMetadata")
public class ApplicationMetadataController {
    /**
     * Base path for all metadata endpoints.
     */
    public static final String PATH = "/api/app/metadata";
    /**
     * Loader for application metadata.
     */
    private final ApplicationMetadataLoader metadataLoader;
    /**
     * Cached application metadata.
     */
    private ApplicationMetadata cache;
    /**
     * Cached entities metadata.
     */
    private ApplicationMetadataEntities entities;
    /**
     * Cached global actions metadata.
     */
    private ApplicationMetadataActions globalActions;
    /**
     * Cache for individual entity metadata.
     */
    private SimpleCache<String, EntityMetadata> entitiesCache = new SimpleCache<>();

    /**
     * Constructs a new {@code ApplicationMetadataController} with the given metadata loader.
     *
     * @param metadataLoader the loader for application metadata
     */
    public ApplicationMetadataController(ApplicationMetadataLoader metadataLoader) {
        this.metadataLoader = metadataLoader;
    }

    /**
     * Returns the application metadata.
     *
     * @return the {@link ApplicationMetadata} object
     */
    @GetMapping(value = "", produces = "application/json")
    public ApplicationMetadata getMetadata() {
        if (cache == null) {
            cache = metadataLoader.load();
        }
        return cache;
    }

    /**
     * Returns the default navigation tree for the application.
     *
     * @return the {@link NavigationTree} object
     */
    @GetMapping(value = "/navigation", produces = "application/json")
    public NavigationTree getNavigation() {
        return NavigationTree.buildDefault();
    }

    /**
     * Returns metadata for all global actions in the application.
     *
     * @return the {@link ApplicationMetadataActions} object
     */
    @GetMapping(value = "/actions", produces = "application/json")
    public ApplicationMetadataActions getGlobalActions() {
        if (globalActions == null) {
            globalActions = metadataLoader.loadGlobalActions();
        }
        return globalActions;
    }

    /**
     * Executes a global action by its ID.
     *
     * @param action  the action ID
     * @param request the execution request containing parameters and data
     * @return the {@link ActionExecutionResponse} object
     */
    @PostMapping(value = "/actions/{action}", produces = "application/json", consumes = "application/json")
    public ActionExecutionResponse executeGlobalAction(@PathVariable("action") String action, ActionExecutionRequest request) {
        var actionMetadata = getGlobalActions().getAction(action);
        return executeAction(action, request, actionMetadata);
    }

    /**
     * Executes an action using its metadata and request.
     *
     * @param action         the action ID
     * @param request        the execution request
     * @param actionMetadata the action metadata
     * @return the {@link ActionExecutionResponse} object
     */
    private static ActionExecutionResponse executeAction(String action, ActionExecutionRequest request, ActionMetadata actionMetadata) {
        if (actionMetadata != null) {
            try {
                var actionInstance = actionMetadata.getAction();
                if (ActionRestrictions.allowAccess(actionInstance)) {
                    return Actions.execute(actionInstance, request);
                } else {
                    return new ActionExecutionResponse("Action " + action + " not allowed", HttpStatus.FORBIDDEN.getReasonPhrase(), 403);
                }
            } catch (ValidationError e) {
                return new ActionExecutionResponse(e.getMessage(), HttpStatus.NOT_ACCEPTABLE.getReasonPhrase(), HttpStatus.NOT_ACCEPTABLE.value());
            } catch (Exception e) {
                return new ActionExecutionResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), 500);
            }

        } else {
            return new ActionExecutionResponse("Action " + action + " not found", HttpStatus.NOT_FOUND.getReasonPhrase(), 404);
        }
    }


    /**
     * Returns metadata for all entities in the application.
     *
     * @return the {@link ApplicationMetadataEntities} object
     */
    @GetMapping(value = "/entities", produces = "application/json")
    public ApplicationMetadataEntities getEntities() {
        if (entities == null) {
            entities = metadataLoader.loadEntities();
        }
        return entities;
    }

    /**
     * Returns metadata for a specific entity by its class name.
     *
     * @param className the fully qualified class name of the entity
     * @return the {@link EntityMetadata} object
     */
    @GetMapping(value = "/entities/{className}", produces = "application/json")
    public EntityMetadata getEntityMetadata(@PathVariable String className) {
        if (entities == null) {
            entities = metadataLoader.loadEntities();
        }

        return entitiesCache.getOrLoad(className, c -> {
            var metadata = entities.getEntityMetadata(className);
            if (metadata != null) {
                return metadataLoader.loadEntityMetadata(metadata.getEntityClass()); //force reload cache
            }
            return null;
        });
    }

    /**
     * Returns all view descriptors for a specific entity by its class name.
     *
     * @param className the entity class name
     * @return the list of {@link ViewDescriptor} objects
     */
    @GetMapping(value = "/entities/{className}/views", produces = "application/json")
    public List<ViewDescriptor> executeEntityAction(@PathVariable String className) {
        var entityMetadata = getEntities().getEntityMetadata(className);
        if (entityMetadata != null) {
            return entityMetadata.getDescriptors().stream().map(ViewDescriptorMetadata::getDescriptor).toList();
        }
        return null;
    }

    /**
     * Returns a specific view descriptor for an entity by its class name and view ID.
     *
     * @param className the entity class name
     * @param view      the view ID
     * @return the {@link ViewDescriptor} object
     */
    @GetMapping(value = "/entities/{className}/views/{view}", produces = "application/json")
    public ViewDescriptor getEntityViewDescriptor(@PathVariable String className, @PathVariable String view) {
        var entityMetadata = getEntityMetadata(className);
        if (entityMetadata != null && entityMetadata.getDescriptors() != null) {
            return entityMetadata.getDescriptors().stream()
                    .filter(d -> d.getView().equals(view))
                    .findFirst().map(ViewDescriptorMetadata::getDescriptor)
                    .orElse(null);
        }
        return null;
    }

    /**
     * Executes an action on an entity by its class name and action ID.
     *
     * @param className the entity class name
     * @param action    the action ID
     * @param request   the execution request containing parameters and data
     * @return the {@link ActionExecutionResponse} object
     */
    @PostMapping(value = "/entities/{className}/action/{action}", produces = "application/json", consumes = "application/json")
    public ActionExecutionResponse executeEntityAction(@PathVariable String className, @PathVariable String action, @RequestBody ActionExecutionRequest request) {
        var entityMetadata = getEntities().getEntityMetadata(className);
        if (entityMetadata != null) {
            var actionMetadata = entityMetadata.getActions().stream().filter(a -> a.getId().equals(action)).findFirst().orElse(null);
            return executeAction(action, request, actionMetadata);
        }
        return new ActionExecutionResponse("Entity " + className + " not found", HttpStatus.NOT_FOUND.getReasonPhrase(), 404);
    }

}
