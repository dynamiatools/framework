package tools.dynamia.app.metadata;

import tools.dynamia.actions.ActionLoader;
import tools.dynamia.actions.ApplicationGlobalAction;
import tools.dynamia.app.ApplicationInfo;
import tools.dynamia.app.controllers.ApplicationMetadataController;
import tools.dynamia.crud.CrudAction;
import tools.dynamia.integration.sterotypes.Service;
import tools.dynamia.viewers.ViewDescriptorFactory;

import java.util.ArrayList;

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
    public ApplicationMetadataLoader(ApplicationInfo applicationInfo, ViewDescriptorFactory viewDescriptorFactory) {
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
                    var entity = new EntityMetadata(entityClass);
                    metadata.getEntities().add(entity);
                });
        return metadata;
    }

    /**
     * Loads the metadata for all global actions in the application.
     *
     * @return the {@link ApplicationMetadataActions} object containing all global action metadata
     */
    public ApplicationMetadataActions loadGlobalActions() {
        ApplicationMetadataActions metadata = new ApplicationMetadataActions();
        metadata.setActions(new ArrayList<>());
        ActionLoader<ApplicationGlobalAction> actionLoader = new ActionLoader<>(ApplicationGlobalAction.class);
        actionLoader.load().forEach(action -> {
            var actionMetadata = new ActionMetadata(action);
            metadata.getActions().add(actionMetadata);
        });

        return metadata;

    }

    public EntityMetadata loadEntityMetadata(Class entityClass) {
        var entity = new EntityMetadata(entityClass);

        var descriptors = viewDescriptorFactory.findDescriptorByClass(entityClass);
        entity.setDescriptors(descriptors.stream().map(ViewDescriptorMetadata::new).toList());

        ActionLoader<CrudAction> loader = new ActionLoader<>(CrudAction.class);
        entity.setActions(loader
                .load(action -> isApplicable(entityClass, action))
                .stream().map(a -> {
                    var md = new ActionMetadata(a);
                    md.setEndpoint(ApplicationMetadataController.PATH + "/entities/" + entityClass.getName() + "/actions/" + a.getId());
                    return md;
                })
                .toList());

        loadEnpoint(entity);
        return entity;
    }

    private boolean isApplicable(final Class targetClass, CrudAction crudAction) {
        return ApplicableClass.isApplicable(targetClass, crudAction.getApplicableClasses(), true);
    }

    private void loadEnpoint(EntityMetadata entity) {
        entity.setEndpoint("/api/app/metadata/entity/" + entity.getClassName());
    }
}
