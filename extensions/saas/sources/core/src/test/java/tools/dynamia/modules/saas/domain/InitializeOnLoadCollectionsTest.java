package tools.dynamia.modules.saas.domain;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import org.junit.jupiter.api.Test;
import tools.dynamia.domain.InitializeOnLoad;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Without Open Persistence In View a detached entity cannot walk its lazy collections, so every to-many
 * association of the SaaS entities must declare {@link InitializeOnLoad} (or be fetched eagerly on purpose).
 */
class InitializeOnLoadCollectionsTest {

    private static final List<Class<?>> ENTITIES = List.of(
            Account.class, AccountProfile.class, AccountRegion.class, AccountReseller.class, AccountType.class);

    @Test
    void everyCollectionDeclaresInitializeOnLoad() {
        List<String> missing = new ArrayList<>();
        int collections = 0;
        for (Class<?> entity : ENTITIES) {
            for (Field field : entity.getDeclaredFields()) {
                boolean toMany = field.isAnnotationPresent(OneToMany.class)
                        || field.isAnnotationPresent(ManyToMany.class)
                        || field.isAnnotationPresent(ElementCollection.class);
                if (toMany) {
                    collections++;
                    if (!field.isAnnotationPresent(InitializeOnLoad.class)) {
                        missing.add(entity.getSimpleName() + "." + field.getName());
                    }
                }
            }
        }
        assertTrue(collections > 0, "no collections found: the scan is broken");
        assertTrue(missing.isEmpty(), "collections without @InitializeOnLoad: " + missing);
    }
}
