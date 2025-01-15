package tools.dynamia.app.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tools.dynamia.actions.ActionExecutionRequest;
import tools.dynamia.actions.ActionExecutionResponse;
import tools.dynamia.actions.ActionRestrictions;
import tools.dynamia.app.metadata.*;
import tools.dynamia.commons.SimpleCache;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.navigation.NavigationTree;
import tools.dynamia.viewers.ViewDescriptor;

import java.util.List;

@RestController
@RequestMapping(value = ApplicationMetadataController.PATH, produces = "application/json")
public class ApplicationMetadataController {
    public static final String PATH = "/api/app/metadata";
    private final ApplicationMetadataLoader metadataLoader;
    private ApplicationMetadata cache;
    private ApplicationMetadataEntities entities;

    private ApplicationMetadataActions globalActions;

    private SimpleCache<String, EntityMetadata> entitiesCache = new SimpleCache<>();

    public ApplicationMetadataController(ApplicationMetadataLoader metadataLoader) {
        this.metadataLoader = metadataLoader;
    }

    @GetMapping(value = "", produces = "application/json")
    public ApplicationMetadata getMetadata() {
        if (cache == null) {
            cache = metadataLoader.load();
        }
        return cache;
    }

    @GetMapping(value = "/navigation", produces = "application/json")
    public NavigationTree getNavigation() {
        return NavigationTree.buildDefault();
    }

    @GetMapping(value = "/actions", produces = "application/json")
    public ApplicationMetadataActions getGlobalActions() {
        if (globalActions == null) {
            globalActions = metadataLoader.loadGlobalActions();
        }
        return globalActions;
    }

    @PostMapping(value = "/actions/{action}", produces = "application/json", consumes = "application/json")
    public ActionExecutionResponse executeGlobalAction(@PathVariable("action") String action, ActionExecutionRequest request) {
        var actionMetadata = getGlobalActions().getAction(action);
        return executeAction(action, request, actionMetadata);
    }

    private static ActionExecutionResponse executeAction(String action, ActionExecutionRequest request, ActionMetadata actionMetadata) {
        if (actionMetadata != null) {
            try {
                var actionInstance = actionMetadata.getAction();
                if (ActionRestrictions.allowAccess(actionInstance)) {
                    return actionInstance.execute(request);
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


    @GetMapping(value = "/entities", produces = "application/json")
    public ApplicationMetadataEntities getEntities() {
        if (entities == null) {
            entities = metadataLoader.loadEntities();
        }
        return entities;
    }

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

    @GetMapping(value = "/entities/{className}/views", produces = "application/json")
    public List<ViewDescriptor> getEntityViewDescriptors(@PathVariable String className) {
        var entityMetadata = getEntities().getEntityMetadata(className);
        if (entityMetadata != null) {
            return entityMetadata.getDescriptors().stream().map(ViewDescriptorMetadata::getDescriptor).toList();
        }
        return null;
    }

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

    @PostMapping(value = "/entities/{className}/action/{action}", produces = "application/json")
    public ActionExecutionResponse getEntityViewDescriptors(@PathVariable String className, @PathVariable String action, ActionExecutionRequest request) {
        var entityMetadata = getEntities().getEntityMetadata(className);
        if (entityMetadata != null) {
            var actionMetadata = entityMetadata.getActions().stream().filter(a -> a.getId().equals(action)).findFirst().orElse(null);
            return executeAction(action, request, actionMetadata);
        }
        return new ActionExecutionResponse("Entity " + className + " not found", HttpStatus.NOT_FOUND.getReasonPhrase(), 404);
    }

}
