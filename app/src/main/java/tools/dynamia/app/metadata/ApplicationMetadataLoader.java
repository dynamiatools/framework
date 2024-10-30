package tools.dynamia.app.metadata;

import tools.dynamia.app.ApplicationInfo;
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
        var metadata = new ApplicationMetadata(applicationInfo);
        loadEntities(metadata);

        return metadata;
    }

    public void loadEntities(ApplicationMetadata metadata) {
        metadata.setEntities(new ArrayList<>());
        viewDescriptorFactory.findDescriptorsByType("form")
                .forEach(d -> {
                    var entityClass = d.getKey();
                    var entity = new EntityMetadata(entityClass);
                    loadEnpoint(entity);
                    metadata.getEntities().add(entity);
                });
    }

    public EntityMetadata loadFullEntityMetadata(Class entityClass) {
        var entity = new EntityMetadata(entityClass);
        var descriptors = viewDescriptorFactory.findDescriptorByClass(entityClass);
        entity.setDescriptors(descriptors.stream().toList());
        loadEnpoint(entity);
        return entity;
    }

    private void loadEnpoint(EntityMetadata entity) {
        entity.setEndpoint("/api/app/metadata/entity/" + entity.getClassName());
    }
}
