/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.app.metadata;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.dynamia.crud.actions.DeleteAction;
import tools.dynamia.crud.actions.FindAction;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.ViewDescriptorFactory;
import tools.dynamia.viewers.impl.DefaultViewDescriptor;

import java.lang.reflect.Proxy;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What the entity catalog publishes: headless actions with endpoint, frontend actions without it, and nothing for an
 * action nobody declared.
 */
class EntityActionsCatalogTest {

    public static class Sale {
    }

    /** A subclass nobody reviewed: it must not be published just because its parent is headless. */
    public static class VoidSaleAction extends DeleteAction {
    }

    @BeforeEach
    void setUp() {
        Containers.get().removeAllContainers();
        var container = new SimpleObjectContainer();
        container.addObject(new DeleteAction());
        container.addObject(new VoidSaleAction());
        container.addObject(new FindAction());
        Containers.get().installObjectContainer(container);
    }

    @AfterEach
    void tearDown() {
        Containers.get().removeAllContainers();
    }

    private static ApplicationMetadataLoader loader() {
        var factory = (ViewDescriptorFactory) Proxy.newProxyInstance(ViewDescriptorFactory.class.getClassLoader(),
                new Class<?>[]{ViewDescriptorFactory.class}, (proxy, method, args) -> {
                    if (ViewDescriptor.class.isAssignableFrom(method.getReturnType())) {
                        return new DefaultViewDescriptor();
                    }
                    return Set.class.isAssignableFrom(method.getReturnType()) ? Set.of() : null;
                });
        return new ApplicationMetadataLoader(null, factory);
    }

    @Test
    void headlessFrontendAndUndeclaredActionsAreTreatedByWhatTheirClassDeclares() {
        var entity = loader().loadEntityMetadata(Sale.class);

        var delete = entity.getActions().stream().filter(a -> "delete".equals(a.getId())).findFirst().orElseThrow();
        assertEquals("HEADLESS", delete.getRuntime());
        assertNotNull(delete.getEndpoint());
        assertTrue(delete.isExecutable());

        var find = entity.getActions().stream().filter(a -> "FRONTEND".equals(a.getRuntime())).findFirst().orElseThrow();
        assertNull(find.getEndpoint());
        assertFalse(find.isExecutable());
        assertEquals("FindAction", find.getClassName());

        assertFalse(entity.getActions().stream().anyMatch(a -> "VoidSaleAction".equals(a.getClassName())),
                "an undeclared subclass of a headless action is not published");
    }
}
