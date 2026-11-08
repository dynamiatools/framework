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
package tools.dynamia.web.query;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** The wire format of a QuerySpec, as documented in its javadoc. */
class QuerySpecJsonTest {

    @Test
    void theDocumentedMessageReadsAsASpec() throws Exception {
        var json = """
                {"version":1,"filters":[{"field":"estado","op":"in","value":["OPEN","PAID"]},
                 {"field":"total","op":"gte","value":100}],
                 "search":"dune","sort":[{"field":"creationDate","dir":"desc"}],"page":2,"size":20}
                """;

        var spec = JsonMapper.builder().build().readValue(json, QuerySpec.class);

        assertEquals(2, spec.filters().size());
        assertEquals("in", spec.filters().get(0).op());
        assertEquals(java.util.List.of("OPEN", "PAID"), spec.filters().get(0).value());
        assertEquals(100, spec.filters().get(1).value());
        assertEquals("dune", spec.search());
        assertEquals("desc", spec.sort().get(0).dir());
        assertEquals(2, spec.page());
        assertEquals(20, spec.size());
    }

    @Test
    void anEmptyObjectIsAnEmptyQuery() throws Exception {
        var spec = JsonMapper.builder().build().readValue("{}", QuerySpec.class);

        assertEquals(0, spec.filters().size());
        assertNull(spec.search());
    }
}
