/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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

import tools.dynamia.commons.JsonParsingException;
import tools.dynamia.commons.StringPojoParser;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;

import javax.persistence.AttributeConverter;
import java.util.ArrayList;
import java.util.List;

/**
 * Converter List to String with JSON format. You need extend this class with generic <T> object type. Example:
 *
 * public class AddressToJsonConverter extends AbstractListToJsonConverter<Address> {
 *
 *     public AddressToJsonConverter() {
 *         super(Address.class);
 *     }
 * }
 *
 * And then annotate entity field with {@link javax.persistence.Convert}
 *
 */
public abstract class AbstractListToJsonConverter<T> implements AttributeConverter<List<T>, String> {

    private final LoggingService logger = new SLF4JLoggingService();
    private final Class<T> objectClass;

    public AbstractListToJsonConverter(Class<T> objectClass) {
        this.objectClass = objectClass;
    }

    @Override
    public String convertToDatabaseColumn(List<T> list) {

        String json = null;
        try {
            json = StringPojoParser.convertListToJson(list);
        } catch (final JsonParsingException e) {
            logger.error("JSON writing error", e);
        }

        return json;
    }

    @Override
    public List<T> convertToEntityAttribute(String data) {

        List<T> list = null;
        try {
            list = StringPojoParser.parseJsonToList(data, objectClass);
        } catch (final JsonParsingException e) {
            logger.error("JSON reading error", e);
        }

        return list;
    }

}
