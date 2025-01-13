package tools.dynamia.app.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * The type Application metadata entities.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationMetadataEntities {
    private List<EntityMetadata> entities;

    public ApplicationMetadataEntities() {
    }

    public ApplicationMetadataEntities(List<EntityMetadata> entities) {
        this.entities = entities;
    }

    public List<EntityMetadata> getEntities() {
        return entities;
    }

    public void setEntities(List<EntityMetadata> entities) {
        this.entities = entities;
    }

    public EntityMetadata getEntityMetadata(String className) {
        if (entities == null) {
            return null;
        }
        return entities.stream().filter(e -> e.getClassName().equals(className)).findFirst().orElse(null);
    }
}
