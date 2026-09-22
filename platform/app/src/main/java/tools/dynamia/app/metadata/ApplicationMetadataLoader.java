package tools.dynamia.app.metadata;

import org.springframework.context.annotation.DependsOn;
import tools.dynamia.actions.ActionLoader;
import tools.dynamia.actions.ApplicationGlobalAction;
import tools.dynamia.actions.ApplicationGlobalRemoteAction;
import tools.dynamia.app.ApplicationInfo;
import tools.dynamia.app.controllers.ApplicationMetadataController;
import tools.dynamia.commons.ApplicableClass;
import tools.dynamia.crud.CrudAction;
import tools.dynamia.crud.CrudRemoteAction;
import tools.dynamia.integration.sterotypes.Service;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.ViewDescriptorFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Loader and factory for application metadata objects.
 * <p>
 * This service provides methods to load metadata for the application, entities, and global actions.
 * It uses {@link ApplicationInfo} and {@link ViewDescriptorFactory} to gather and construct metadata objects for API and UI clients.
 * <p>
 * Typical usage is in controllers or service layers that need to expose metadata endpoints.
 *
 * @author Mario A. Serrano Leones
 * @since 2023
 */
@Service
@DependsOn({"applicationInfo"})
public class ApplicationMetadataLoader {

    /**
     * Application information source.
     */
    private final ApplicationInfo applicationInfo;
    /**
     * Factory for view descriptors, used to discover entity views.
     */
    private final ViewDescriptorFactory viewDescriptorFactory;


    /**
     * Constructs a new {@code ApplicationMetadataLoader} with the given application info and view descriptor factory.
     *
     * @param applicationInfo the application information source
     * @param viewDescriptorFactory the factory for view descriptors
     */
    public ApplicationMetadataLoader( ApplicationInfo applicationInfo, ViewDescriptorFactory viewDescriptorFactory) {
        this.applicationInfo = applicationInfo;
        this.viewDescriptorFactory = viewDescriptorFactory;
    }


    /**
     * Loads the metadata for the application.
     *
     * @return the {@link ApplicationMetadata} object
     */
    public ApplicationMetadata load() {
        return new ApplicationMetadata(applicationInfo);
    }

    /**
     * Loads the metadata for all entities in the application.
     *
     * @return the {@link ApplicationMetadataEntities} object containing all entity metadata
     */
    public ApplicationMetadataEntities loadEntities() {
        ApplicationMetadataEntities metadata = new ApplicationMetadataEntities();
        metadata.setEntities(new ArrayList<>());
        viewDescriptorFactory.findDescriptorsByType("form")
                .forEach(d -> {
                    var entityClass = d.getKey();
                    var entity = loadEntityMetadata(entityClass);
                    metadata.getEntities().add(entity);
                });
        validateUniqueEntityIds(metadata.getEntities());
        return metadata;
    }

    /**
     * Fails fast if two entities resolve to the same {@link EntityMetadata#getId()} (the class's
     * simple name). The {@code /api/app/metadata/entities/{id}} contract requires that id to be
     * unique across the application — see {@code docs/design} note on entity metadata identifiers
     * for why the fully qualified class name is no longer used as the wire identifier.
     *
     * @param entities the loaded entity metadata list
     * @throws IllegalStateException if a duplicate id is found, naming the colliding classes
     */
    private void validateUniqueEntityIds(List<EntityMetadata> entities) {
        var duplicates = entities.stream()
                .collect(Collectors.groupingBy(EntityMetadata::getId))
                .entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(e -> e.getKey() + " -> " + e.getValue().stream().map(EntityMetadata::getClassName).toList())
                .collect(Collectors.joining("; "));
        if (!duplicates.isEmpty()) {
            throw new IllegalStateException("Duplicate entity simple class names found while building " +
                    "application metadata: " + duplicates + ". /api/app/metadata/entities/{id} requires each " +
                    "entity's simple class name to be unique; rename one of the colliding classes.");
        }
    }

    /**
     * Loads the metadata for all global actions in the application.
     *
     * @return the {@link ApplicationMetadataActions} object containing all global action metadata
     */
    public ApplicationMetadataActions loadGlobalActions() {
        ApplicationMetadataActions metadata = new ApplicationMetadataActions();
        metadata.setActions(new ArrayList<>());
        ActionLoader<ApplicationGlobalRemoteAction> actionLoader = new ActionLoader<>(ApplicationGlobalRemoteAction.class);
        actionLoader.load().forEach(action -> {
            var actionMetadata = new ActionMetadata(action);
            metadata.getActions().add(actionMetadata);
        });

        return metadata;

    }

    public EntityMetadata loadEntityMetadata(Class entityClass) {
        var entity = new EntityMetadata(entityClass);

        Set<ViewDescriptor> descriptors = viewDescriptorFactory.findDescriptorByClass(entityClass);
        if(descriptors==null || descriptors.isEmpty()) {
            descriptors = new HashSet<>();
            descriptors.add(viewDescriptorFactory.getDescriptor(entityClass, "form"));
            descriptors.add(viewDescriptorFactory.getDescriptor(entityClass, "table"));
            descriptors.add(viewDescriptorFactory.getDescriptor(entityClass, "crud"));
        }
        entity.setDescriptors(descriptors.stream().map(ViewDescriptorMetadata::new).toList());

        ActionLoader<CrudRemoteAction> loader = new ActionLoader<>(CrudRemoteAction.class);
        entity.setActions(loader
                .load(action -> isApplicable(entityClass, action))
                .stream().map(a -> {
                    var md = new ActionMetadata(a);
                    md.setEndpoint(ApplicationMetadataController.PATH + "/entities/" + entity.getId() + "/actions/" + a.getId());
                    return md;
                })
                .toList());

        loadEnpoint(entity);
        return entity;
    }

    private boolean isApplicable(final Class targetClass, CrudRemoteAction crudAction) {
        return ApplicableClass.isApplicable(targetClass, crudAction.getApplicableClasses(), true);
    }

    private void loadEnpoint(EntityMetadata entity) {
        // Was "/entity/" + className (singular path segment, wrong mapping, and leaked the FQCN) —
        // aligned with the constructor's own "/entities/{id}" endpoint and the id-based contract.
        entity.setEndpoint(ApplicationMetadataController.PATH + "/entities/" + entity.getId());
    }
}
