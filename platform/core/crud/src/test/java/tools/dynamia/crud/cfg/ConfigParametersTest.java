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
package tools.dynamia.crud.cfg;

import org.junit.jupiter.api.Test;
import tools.dynamia.domain.jpa.JpaParameter;
import tools.dynamia.domain.query.Parameter;
import tools.dynamia.domain.query.Parameters;
import tools.dynamia.viewers.ViewDescriptor;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static tools.dynamia.viewers.ViewDescriptorBuilder.field;
import static tools.dynamia.viewers.ViewDescriptorBuilder.viewDescriptor;

/**
 * Covers the mapping between the fields of a {@code config} descriptor and application parameters.
 */
class ConfigParametersTest {

    /** In-memory {@link Parameters}: only the two methods the config code uses. */
    private final Map<String, Parameter> stored = new HashMap<>();

    @SuppressWarnings({"rawtypes", "unchecked"})
    private final Parameters store = (Parameters) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Parameters.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "getParameter" -> stored.get((String) args[1]);
                case "save" -> {
                    ((Collection<Parameter>) args[0]).forEach(p -> stored.put(p.getName(), p));
                    yield null;
                }
                default -> throw new UnsupportedOperationException(method.getName());
            });

    private ViewDescriptor descriptor() {
        return viewDescriptor("config")
                .id("DemoCFG")
                .params("parameterClass", JpaParameter.class.getName())
                .fields(
                        field("location").params("parameterName", "DEMO_LOCATION", "defaultValue", "/data"),
                        field("region"),
                        field("useHttps", "Use HTTPS", "checkbox").params("parameterName", "DEMO_HTTPS", "cacheable", true)
                )
                .build();
    }

    @Test
    void readsDefaultsWhenNothingIsStored() {
        var values = ConfigParameters.read(descriptor(), store);

        assertEquals("/data", values.get("location"));
        assertEquals("", values.get("region"));
        assertEquals(false, values.get("useHttps"));
    }

    @Test
    void savesUnderTheParameterNameOrTheDescriptorIdPrefix() {
        ConfigParameters.save(descriptor(), Map.of("location", "/srv", "region", "eu", "useHttps", true), store);

        assertEquals("/srv", stored.get("DEMO_LOCATION").getValue());
        assertEquals("eu", stored.get("DemoCFG_region").getValue());
        assertEquals("true", stored.get("DEMO_HTTPS").getValue());
        assertEquals(true, stored.get("DEMO_HTTPS").isCacheable());
        assertEquals(true, ConfigParameters.read(descriptor(), store).get("useHttps"));
    }

    @Test
    void ignoresNamesThatAreNotFieldsOfTheDescriptor() {
        var values = new HashMap<String, Object>();
        values.put("location", "/srv");
        values.put("AWS_SECRET_KEY", "stolen");
        values.put("DEMO_LOCATION", "bypass");

        ConfigParameters.save(descriptor(), values, store);

        assertEquals("/srv", stored.get("DEMO_LOCATION").getValue());
        assertNull(stored.get("AWS_SECRET_KEY"));
        assertEquals(1, stored.size());
    }

    @Test
    void onlyTheSentFieldsChange() {
        ConfigParameters.save(descriptor(), Map.of("region", "eu"), store);
        ConfigParameters.save(descriptor(), Map.of("location", "/srv"), store);

        assertEquals("eu", stored.get("DemoCFG_region").getValue());
        assertEquals("/srv", stored.get("DEMO_LOCATION").getValue());
    }
}
