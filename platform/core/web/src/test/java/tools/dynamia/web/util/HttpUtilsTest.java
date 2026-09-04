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
package tools.dynamia.web.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class HttpUtilsTest {

    @Test
    public void shouldFormatHttpParams() {

        Map<String, Object> params = Map.of(
                "id", "123",
                "name", "mario",
                "page", "/index.html");

        String result = HttpUtils.formatRequestParams(params);

        Assertions.assertTrue(result.contains("id=123"));
        Assertions.assertTrue(result.contains("name=mario"));
    }
}
