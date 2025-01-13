package tools.dynamia.app.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.dynamia.app.metadata.*;
import tools.dynamia.navigation.NavigationTree;
import tools.dynamia.viewers.ViewDescriptor;

import java.util.List;

@RestController
@RequestMapping(value = MetadataController.PATH, produces = "application/json")
public class MetadataController {
    public static final String PATH = "/api/app/metadata";
    private final ApplicationMetadataLoader metadataLoader;
    private ApplicationMetadata cache;
    private ApplicationMetadataEntities entitiesCache;

    public MetadataController(ApplicationMetadataLoader metadataLoader) {
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
        return metadataLoader.loadGlobalActions();
    }


    @GetMapping(value = "/entities", produces = "application/json")
    public ApplicationMetadataEntities getEntities() {
        if (entitiesCache == null) {
            entitiesCache = metadataLoader.loadEntities();
        }
        return entitiesCache;
    }

    @GetMapping(value = "/entity/{className}", produces = "application/json")
    public EntityMetadata getEntityMetadata(@PathVariable String className) {
        var entityMetadata = getEntities().getEntityMetadata(className);
        if (entityMetadata != null) {
            try {
                return metadataLoader.loadEntityMetadata(Class.forName(className));
            } catch (ClassNotFoundException e) {

            }
        }
        return null;
    }

    @GetMapping(value = "/entity/{className}/view-descriptors", produces = "application/json")
    public List<ViewDescriptor> getEntityViewDescriptors(@PathVariable String className) {
        var entityMetadata = getEntities().getEntityMetadata(className);
        if (entityMetadata != null) {
            return entityMetadata.getDescriptors().stream().map(ApplicationMetadataViewDescriptor::getDescriptor).toList();
        }
        return null;
    }

    @GetMapping(value = "/entity/{className}/view-descriptors/{view}", produces = "application/json")
    public ViewDescriptor getEntityViewDescriptor(@PathVariable String className, @PathVariable String view) {
        var entityMetadata = getEntities().getEntityMetadata(className);
        if (entityMetadata != null) {
            return entityMetadata.getDescriptors().stream()
                    .filter(d -> d.getView().equals(view))
                    .findFirst().map(ApplicationMetadataViewDescriptor::getDescriptor)
                    .orElse(null);
        }
        return null;
    }
}
