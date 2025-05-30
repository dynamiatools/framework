package tools.dynamia.app.metadata;

import tools.dynamia.actions.ActionLoader;
import tools.dynamia.actions.ApplicationGlobalAction;
import tools.dynamia.app.ApplicationInfo;
import tools.dynamia.app.controllers.ApplicationMetadataController;
import tools.dynamia.commons.ApplicableClass;
import tools.dynamia.crud.CrudAction;
import tools.dynamia.integration.sterotypes.Service;
import tools.dynamia.viewers.ViewDescriptorFactory;

import java.util.ArrayList;

@Service
public class ApplicationMetadataLoader {

    private final ApplicationInfo applicationInfo;
    private final ViewDescriptorFactory viewDescriptorFactory;


    public ApplicationMetadataLoader(ApplicationInfo applicationInfo, ViewDescriptorFactory viewDescriptorFactory) {
        this.applicationInfo = applicationInfo;
        this.viewDescriptorFactory = viewDescriptorFactory;
    }


    public ApplicationMetadata load() {
        return new ApplicationMetadata(applicationInfo);
    }

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
