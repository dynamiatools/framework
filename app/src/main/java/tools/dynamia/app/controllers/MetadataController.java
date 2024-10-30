package tools.dynamia.app.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.dynamia.app.metadata.ApplicationMetadata;
import tools.dynamia.app.metadata.ApplicationMetadataLoader;
import tools.dynamia.app.metadata.EntityMetadata;

@RestController
@RequestMapping(value = MetadataController.PATH, produces = "application/json")
public class MetadataController {
    public static final String PATH = "/api/app/metadata";
    private final ApplicationMetadataLoader metadataLoader;
    private ApplicationMetadata cache;

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

    @GetMapping(value = "/entity/{className}", produces = "application/json")
    public EntityMetadata getEntityMetadata(@PathVariable String className) {
        var entityMetadata = getMetadata().getEntityMetadata(className);
        if (entityMetadata != null) {
            try {
                return metadataLoader.loadFullEntityMetadata(Class.forName(className));
            } catch (ClassNotFoundException e) {

            }
        }
        return null;
    }
}
