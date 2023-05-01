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

package tools.dynamia.domain.jpa;

import jakarta.persistence.AttributeConverter;
import tools.dynamia.commons.JsonParsingException;
import tools.dynamia.commons.StringPojoParser;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;

import java.util.Map;

/**
 * Convert Map to String with JSON format
 */
public class MapToJsonConverter implements AttributeConverter<Map<String, Object>, String> {

    private final LoggingService logger = new SLF4JLoggingService();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> map) {

        String customerInfoJson = null;
        try {
            customerInfoJson = StringPojoParser.convertMapToJson(map);
        } catch (final JsonParsingException e) {
            logger.error("JSON writing error", e);
        }

        return customerInfoJson;
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String data) {

        Map<String, Object> map = null;
        try {
            map = StringPojoParser.parseJsonToMap(data);
        } catch (final JsonParsingException e) {
            logger.error("JSON reading error", e);
        }

        return map;
    }

}
