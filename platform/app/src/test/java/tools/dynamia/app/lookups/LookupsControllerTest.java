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
package tools.dynamia.app.lookups;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.ui.EntityLookup;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LookupsControllerTest {

    private final List<Integer> askedLimits = new ArrayList<>();
    private final LookupsController controller = new LookupsController();

    @BeforeEach
    void install() {
        var beans = new SimpleObjectContainer("lookups-test");
        beans.addObject(new EntityLookup<String>() {
            @Override
            public String name() {
                return "names";
            }

            @Override
            public List<String> search(String text, int limit) {
                askedLimits.add(limit);
                return java.util.stream.IntStream.range(0, 500).mapToObj(i -> text + i).toList();
            }

            @Override
            public Optional<String> find(String key) {
                return Optional.of(key);
            }

            @Override
            public String key(String value) {
                return "k-" + value;
            }

            @Override
            public String label(String value) {
                return value.toUpperCase();
            }
        });
        Containers.get().installObjectContainer(beans);
    }

    @AfterEach
    void uninstall() {
        Containers.get().removeAllContainers();
    }

    @Test
    void answersKeysAndLabels() {
        var response = controller.search("names", "an", 2);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(List.of(java.util.Map.of("key", "k-an0", "label", "AN0"), java.util.Map.of("key", "k-an1", "label", "AN1")),
                response.getBody());
    }

    @Test
    void thePageIsCappedEvenWhenTheLookupReturnsMore() {
        var response = controller.search("names", "x", 100000);

        assertEquals(EntityLookup.MAX_LIMIT, askedLimits.get(0));
        assertEquals(EntityLookup.MAX_LIMIT, response.getBody().size());
    }

    @Test
    void withoutALimitTheDefaultPageIsUsedAndANegativeOneBecomesOne() {
        controller.search("names", "x", null);
        controller.search("names", "x", -5);

        assertEquals(List.of(LookupsController.DEFAULT_LIMIT, 1), askedLimits);
    }

    @Test
    void anUnknownLookupIsNotFound() {
        assertEquals(404, controller.search("nope", "", null).getStatusCode().value());
        assertTrue(askedLimits.isEmpty());
    }
}
