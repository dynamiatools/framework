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
        viewDescriptorFactory.findDescriptorsByType("crud")
                .forEach(d -> {
                    var entity = new EntityMetadata(d.getKey());

                    loadEnpoint(entity);
                   metadata.getEntities().add(entity);
                });
    }

    private void loadEnpoint(EntityMetadata entity) {
    }
}
