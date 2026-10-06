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
package tools.dynamia.domain;

import org.junit.jupiter.api.Test;
import tools.dynamia.domain.query.EntityMapper;
import tools.dynamia.domain.query.QueryParameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class QueryParametersMapperTest {

    @Test
    public void mapperIsNullByDefault() {
        assertNull(new QueryParameters().getMapper());
    }

    @Test
    public void mapperTravelsWithParametersAndClone() {
        EntityMapper<String> mapper = Object::toString;
        QueryParameters params = new QueryParameters().mapWith(mapper);

        assertSame(mapper, params.getMapper());
        assertSame(mapper, params.clone().getMapper());
    }

    @Test
    public void mapperIsNotAQueryParameter() {
        QueryParameters params = QueryParameters.with("name", "x").mapWith(Object::toString);
        assertEquals(1, params.size());
    }
}
