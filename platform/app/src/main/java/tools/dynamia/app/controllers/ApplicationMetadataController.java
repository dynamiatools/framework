package tools.dynamia.app.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.dynamia.actions.*;
import tools.dynamia.app.metadata.*;
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.crud.CrudRemoteAction;
import tools.dynamia.crud.CrudState;
import tools.dynamia.domain.EntityReference;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.Containers;
import tools.dynamia.navigation.NavigationNode;
import tools.dynamia.navigation.NavigationTree;
import tools.dynamia.navigation.Page;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.web.navigation.ErrorResult;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

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
 *   <li>GET /api/app/metadata/entities/{id} - Metadata for a specific entity, by id (simple class name)</li>
 *   <li>GET /api/app/metadata/entities/by-path?path={virtualPath} - Metadata for the CrudPage registered at that navigation virtual path</li>
 * </ul>
 *
 * @author Mario A. Serrano Leones
 * @since 2023
 */
@RestController
@RequestMapping(value = ApplicationMetadataController.PATH, produces = "application/json")
@Tag(name = "DynamiaApplicationMetadata")
@DependsOn({"applicationInfo"})
public class ApplicationMetadataController {

    private final static LoggingService logger = LoggingService.get(ApplicationMetadataController.class);

    /**
     * Base path for all metadata endpoints.
     */
    public static final String PATH = "/api/app/metadata";
    /**
     * Loader for application metadata.
     */
    private final ApplicationMetadataLoader metadataLoader;
    private final EntityMetadata unknowEntity;
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


    /**
     * Constructs a new {@code ApplicationMetadataController} with the given metadata loader.
     *
     * @param metadataLoader the loader for application metadata
     */
    public ApplicationMetadataController(ApplicationMetadataLoader metadataLoader) {
        this.metadataLoader = metadataLoader;
        this.unknowEntity = new EntityMetadata();
        unknowEntity.setClassName("unknown");
        unknowEntity.setName("Unknown Entity");
        unknowEntity.setActions(List.of());
        unknowEntity.setDescriptors(List.of());
        unknowEntity.setActionsEndpoint("");
        unknowEntity.setEndpoint("");
    }


    private void initMetadata() {
        if (entities == null) {
            entities = metadataLoader.loadEntities();
        }
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
     * @param httpRequest the current HTTP request, used only to populate a validation-error body's path
     * @return a {@code 200} {@link ActionExecutionResponse}, or a {@code 422}
     * {@link tools.dynamia.web.navigation.ErrorResult} on {@link ValidationError} — see
     * {@link #executeAction} and {@code docs/design/SERVER_DRIVEN_ACTION_FLOWS.md} §7.6 point 3
     */
    @PostMapping(value = "/actions/{action}", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Object> executeGlobalAction(@PathVariable("action") String action, ActionExecutionRequest request,
                                                        HttpServletRequest httpRequest) {
        var actionMetadata = getGlobalActions().getAction(action);
        return executeAction(action, request, actionMetadata, httpRequest);
    }

    /**
     * Executes an action using its metadata and request.
     * <p>
     * Every outcome except a failed validation is reported exactly as before — a real HTTP {@code 200}
     * carrying an {@link ActionExecutionResponse} whose own {@code status}/{@code statusCode} fields
     * describe success or a non-validation failure (403/404/409/500). A {@link ValidationError} is the one
     * exception: it now surfaces as a genuine HTTP {@code 422} with an
     * {@link tools.dynamia.web.navigation.ErrorResult} body, matching plain REST CRUD writes
     * ({@code RestApiExceptionHandler.handleValidationError}) instead of the {@code ActionExecutionResponse}
     * -wrapped {@code 406} this endpoint used to embed — see
     * {@code docs/design/SERVER_DRIVEN_ACTION_FLOWS.md} §7.6 point 3 for the full reasoning.
     *
     * @param action         the action ID
     * @param request        the execution request
     * @param actionMetadata the action metadata
     * @param httpRequest    the current HTTP request, used only to populate a validation-error body's path
     * @return the response entity described above
     */
    static ResponseEntity<Object> executeAction(String action, ActionExecutionRequest request,
                                                          ActionMetadata actionMetadata, HttpServletRequest httpRequest) {
        if (actionMetadata != null) {
            try {
                RemoteAction actionInstance = null;
                if (actionMetadata.getAction() != null) {
                    actionInstance = Containers.get().findObject(actionMetadata.getAction().getClass());
                }
                if (!ActionRestrictions.allowAccess(actionInstance)) {
                    return okBody(new ActionExecutionResponse("Action " + action + " not allowed", HttpStatus.FORBIDDEN.getReasonPhrase(), 403));
                }
                if (!isApplicableState(actionInstance, request)) {
                    return okBody(new ActionExecutionResponse("Action " + action + " is not applicable to the current state",
                            HttpStatus.CONFLICT.getReasonPhrase(), HttpStatus.CONFLICT.value()));
                }
                logger.info("Executing action " + action);
                return okBody(Actions.execute(actionInstance, request));
            } catch (ValidationError e) {
                return validationErrorResponse(e, httpRequest);
            } catch (Exception e) {
                return okBody(new ActionExecutionResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), 500));
            }

        } else {
            return okBody(new ActionExecutionResponse("Action " + action + " not found", HttpStatus.NOT_FOUND.getReasonPhrase(), 404));
        }
    }

    private static ResponseEntity<Object> okBody(ActionExecutionResponse body) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    /**
     * Builds the {@code 422} {@link tools.dynamia.web.navigation.ErrorResult} response for a failed
     * {@link ValidationError}, mirroring {@code RestApiExceptionHandler.handleValidationError}'s shape
     * exactly (same {@code invalidProperty}/{@code invalidValue} detail keys) so both entry points give
     * frontend code one error shape to handle.
     */
    private static ResponseEntity<Object> validationErrorResponse(ValidationError e, HttpServletRequest httpRequest) {
        logger.warn("Validation error on: " + httpRequest.getRequestURI() + " - " + e.getMessage());
        var error = new ErrorResult(422, "VALIDATION_ERROR", e.getMessage(), httpRequest.getRequestURI());
        if (e.getInvalidProperty() != null) {
            error.addDetail("invalidProperty", e.getInvalidProperty());
        }
        if (e.getInvalidValue() != null) {
            error.addDetail("invalidValue", String.valueOf(e.getInvalidValue()));
        }
        return ResponseEntity.status(422).contentType(MediaType.APPLICATION_JSON).body(error);
    }

    /**
     * Generic, cheap server-side guard for {@link CrudRemoteAction#getApplicableStates()}: rejects a
     * request whose inferred {@link CrudState} isn't one the action declared itself applicable to, before
     * {@code execute()}/{@code start()} ever runs — so a hand-crafted request can't invoke, say, a
     * delete-only action ({@code applicableStates = {READ}}) while pretending to be in a {@code CREATE}
     * context, or vice versa.
     * <p>
     * The inference deliberately mirrors {@code SaveSupport}'s own heuristic rather than hitting the
     * database: an entity id present anywhere the client is expected to carry one ({@code dataId}, a
     * {@code data.id}, a non-empty {@code data.ids}, or a raw list body) means "this request targets an
     * existing entity" — allowed when the action declares {@code READ}, {@code UPDATE}, or {@code DELETE}
     * applicable; no id means "this request targets a new entity" — allowed only when {@code CREATE} is
     * declared applicable. This only distinguishes "existing" from "new", not the finer READ/UPDATE/DELETE
     * distinction a specific action may still need to enforce itself (as {@code SaveSupport} already does
     * for CREATE vs UPDATE) — see {@code docs/design/SERVER_DRIVEN_ACTION_FLOWS.md} §7.6 point 2.
     * <p>
     * Non-{@link CrudRemoteAction} actions, or ones declaring no {@code applicableStates} at all, are
     * unaffected.
     */
    static boolean isApplicableState(RemoteAction actionInstance, ActionExecutionRequest request) {
        if (!(actionInstance instanceof CrudRemoteAction crudRemoteAction)) {
            return true;
        }
        CrudState[] applicableStates = crudRemoteAction.getApplicableStates();
        if (applicableStates == null || applicableStates.length == 0) {
            return true;
        }
        if (hasEntityId(request)) {
            return CrudState.isApplicable(CrudState.READ, applicableStates)
                    || CrudState.isApplicable(CrudState.UPDATE, applicableStates)
                    || CrudState.isApplicable(CrudState.DELETE, applicableStates);
        }
        return CrudState.isApplicable(CrudState.CREATE, applicableStates);
    }

    static boolean hasEntityId(ActionExecutionRequest request) {
        if (request.getDataId() != null && !request.getDataId().isBlank()) {
            return true;
        }
        Object data = request.getData();
        if (data instanceof Map<?, ?> map) {
            if (map.get("id") != null) {
                return true;
            }
            Object ids = map.get("ids");
            return ids instanceof Collection<?> collection && !collection.isEmpty();
        }
        return data instanceof Collection<?> collection && !collection.isEmpty();
    }


    /**
     * Returns metadata for all entities in the application.
     *
     * @return the {@link ApplicationMetadataEntities} object
     */
    @GetMapping(value = "/entities", produces = "application/json")
    public ApplicationMetadataEntities getEntities() {
        initMetadata();
        return entities;
    }


    /**
     * Returns metadata for a specific entity by its id (the entity class's simple name, e.g.
     * {@code "Invoice"} — see {@link EntityMetadata#getId()}). The fully qualified class name is
     * never accepted nor returned here; it stays server-side.
     *
     * @param id the entity id
     * @return the {@link EntityMetadata} object
     */
    @GetMapping(value = "/entities/{id}", produces = "application/json")
    public EntityMetadata getEntityMetadata(@PathVariable String id) {
        initMetadata();
        var result = entities.getEntityMetadata(id);
        if (result == null) {
            result = tryToFindEntityClassById(id);
        }

        if (result == null) {
            result = unknowEntity;
        }

        return result;

    }

    /**
     * Resolves entity metadata for a {@code CrudPage} directly from its navigation virtual path
     * (e.g. {@code "store/catalog/books"}, the same value used to build {@link NavigationNode#getInternalPath()}
     * and to call {@code client.crud(virtualPath)}), instead of requiring the client to already know
     * the entity's id.
     * <p>
     * This is what lets frontend code (see {@code CrudPageResolver} in {@code @dynamia-tools/ui-core})
     * resolve a CrudPage without any entity class information ever appearing in the navigation JSON.
     *
     * @param path the CrudPage's virtual path
     * @return the {@link EntityMetadata} object, or {@link #unknowEntity} if no CrudPage is registered at that path
     */
    @GetMapping(value = "/entities/by-path", produces = "application/json")
    public EntityMetadata getEntityMetadataByPath(@RequestParam String path) {
        initMetadata();
        var result = tryToFindEntityClass(node -> path.equals(node.getInternalPath()));
        return result != null ? result : unknowEntity;
    }

    /**
     * Server-side resolution of a {@code CrudPage}'s backing entity class, deriving it from the
     * live {@link Page#getPath()} of the navigation element itself (never from client input) — see
     * {@code docs/design} note on why {@code NavigationNode} no longer carries a {@code file} field.
     *
     * @param id the entity id to look for (its simple class name)
     * @return the resolved {@link EntityMetadata}, or {@code null} if no matching CrudPage exists
     */
    private EntityMetadata tryToFindEntityClassById(String id) {
        return tryToFindEntityClass(node -> {
            var clazz = crudPageEntityClass(node);
            return clazz != null && clazz.getSimpleName().equals(id);
        });
    }

    private EntityMetadata tryToFindEntityClass(Predicate<NavigationNode> nodeMatcher) {
        AtomicReference<EntityMetadata> found = new AtomicReference<>();
        getNavigation().forEachNode(node -> {
            if (found.get() == null && "CrudPage".equals(node.getType()) && nodeMatcher.test(node)) {
                var clazz = crudPageEntityClass(node);
                if (clazz != null) {
                    found.set(registerEntityMetadata(clazz));
                }
            }
        });
        return found.get();
    }

    /**
     * Extracts the real entity class of a {@code CrudPage} node from its live {@link NavigationNode#getElement()}
     * (a {@link Page}), never from serialized client data.
     */
    private Class<?> crudPageEntityClass(NavigationNode node) {
        if (node.getElement() instanceof Page page) {
            return ObjectOperations.findClass(page.getPath());
        }
        return null;
    }

    private EntityMetadata registerEntityMetadata(Class<?> clazz) {
        var existing = entities.getEntities().stream()
                .filter(e -> e.getClassName().equals(clazz.getName()))
                .findFirst().orElse(null);
        if (existing != null) {
            return existing;
        }
        var entityMetadata = metadataLoader.loadEntityMetadata(clazz);
        entities.getEntities().add(entityMetadata);
        return entityMetadata;
    }

    /**
     * Returns all view descriptors for a specific entity by its id.
     *
     * @param id the entity id
     * @return the list of {@link ViewDescriptor} objects
     */
    @GetMapping(value = "/entities/{id}/views", produces = "application/json")
    public List<ViewDescriptor> getEntityViewDescriptors(@PathVariable String id) {
        var entityMetadata = getEntityMetadata(id);
        if (entityMetadata != null) {
            return entityMetadata.getDescriptors().stream().map(ViewDescriptorMetadata::getDescriptor).toList();
        }
        return null;
    }

    /**
     * Returns a specific view descriptor for an entity by its id and view ID.
     *
     * @param id   the entity id
     * @param view the view ID
     * @return the {@link ViewDescriptor} object
     */
    @GetMapping(value = "/entities/{id}/views/{view}", produces = "application/json")
    public ViewDescriptor getEntityViewDescriptor(@PathVariable String id, @PathVariable String view) {
        var entityMetadata = getEntityMetadata(id);
        if (entityMetadata != null && entityMetadata.getDescriptors() != null) {
            return entityMetadata.getDescriptors().stream()
                    .filter(d -> d.getView().equals(view))
                    .findFirst().map(ViewDescriptorMetadata::getDescriptor)
                    .orElse(null);
        }
        return null;
    }

    /**
     * Executes an action on an entity by its id and action ID.
     *
     * @param id          the entity id
     * @param action      the action ID
     * @param request     the execution request containing parameters and data
     * @param httpRequest the current HTTP request, used only to populate a validation-error body's path
     * @return see {@link #executeAction}
     */
    @PostMapping(value = "/entities/{id}/action/{action}", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Object> executeEntityAction(@PathVariable String id, @PathVariable String action,
                                                        @RequestBody ActionExecutionRequest request, HttpServletRequest httpRequest) {
        var entityMetadata = getEntityMetadata(id);
        if (entityMetadata != null) {
            var actionMetadata = entityMetadata.getActions().stream().filter(a -> a.getId().equals(action)).findFirst().orElse(null);
            return executeAction(action, request, actionMetadata, httpRequest);
        }
        return okBody(new ActionExecutionResponse("Entity " + id + " not found", HttpStatus.NOT_FOUND.getReasonPhrase(), 404));
    }

    @GetMapping(value = "/entities/ref/{alias}/{id}", produces = "application/json")
    public EntityReference getEntityReference(@PathVariable String alias, @PathVariable String id) {
        return DomainUtils.getEntityReference(alias, id);
    }

    @GetMapping(value = "/entities/ref/{alias}/search", produces = "application/json")
    public List<EntityReference> findEntityReferences(@PathVariable String alias, @RequestParam("q") String query) {
        var repo = DomainUtils.getEntityReferenceRepositoryByAlias(alias);
        var params = new HashMap<String, Object>();


        if (repo != null) {
            return repo.find(query, params);
        }
        return List.of();
    }

}
