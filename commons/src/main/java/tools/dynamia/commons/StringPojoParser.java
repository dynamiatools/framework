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

package tools.dynamia.commons;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Helper class to parse and convert json strings to objects and objects to json
 */
public class StringPojoParser {

    /**
     * Convert Map to JSON string using Jackson
     *
     * @return json string or an empty string if map is null or empty
     */
    public static String convertMapToJson(Map map) {
        try {
            if (map == null || map.isEmpty()) {
                return "";
            }
            ObjectMapper jsonMapper = createJsonMapper();
            return jsonMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new JsonParsingException(e);
        }
    }

    /**
     * Create a json {@link ObjectMapper} with enable IDENT_OUTPUT and disabled FAIL_ON_EMPTY_BEANS. Also add support
     * to {@link JavaTimeModule} from JSR310 dependency
     *
     * @return json ObjectMapper
     */
    public static ObjectMapper createJsonMapper() {
        return JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .addModule(new JavaTimeModule())
                .build();

    }

    /**
     * Convert bean to JSON string using Jackson
     *
     * @return json string or an empty string if pojo is null
     */
    public static String convertPojoToJson(Object pojo) {
        try {
            if (pojo == null) {
                return "";
            }
            ObjectMapper jsonMapper = createJsonMapper();
            return jsonMapper.writeValueAsString(pojo);
        } catch (JsonProcessingException e) {
            throw new JsonParsingException(e);
        }
    }

    /**
     * Parse JSON string to Map using Jackson
     *
     * @return map object with json data or an empty Map if json is null or blank
     */
    public static Map<String, Object> parseJsonToMap(String json) {
        try {
            if (json == null || json.isBlank()) {
                return Map.of();
            }
            return createJsonMapper().readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new JsonParsingException(e);
        }
    }

    /**
     * Parse JSON string to a string Map using Jackson
     *
     * @return map object with json data or an empty Map if json is null or blank
     */
    public static Map<String, String> parseJsonToStringMap(String json) {
        try {
            if (json == null || json.isBlank()) {
                return Map.of();
            }
            return createJsonMapper().readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new JsonParsingException(e);
        }
    }

    /**
     * Parse JSON string to java type (java bean)
     *
     * @param json
     * @param pojoType
     *
     * @return object of type or null if json is null or empty
     */
    public static <T> T parseJsonToPojo(String json, Class<T> pojoType) {
        try {
            if (json == null || json.isBlank()) {
                return null;
            }

            ObjectMapper jsonMapper = createJsonMapper();
            return jsonMapper.readerFor(pojoType).readValue(json);
        } catch (IOException e) {
            throw new JsonParsingException(e);
        }
    }


    /**
     * Parse JSON map to java type (java bean)
     * @param map
     * @param pojoType
     * @return object of type or null if json is null or empty
     */
    public static <T> T parseJsonToPojo(Map map, Class<T> pojoType) {
        try {
            if (map == null || map.isEmpty()) {
                return null;
            }

            ObjectMapper jsonMapper = createJsonMapper();
            return jsonMapper.convertValue(map, pojoType);
        } catch (IllegalArgumentException e) {
            throw new JsonParsingException(e);
        }
    }

    /**
     * Convert any plain old java object to XML
     */
    public static String convertPojoToXml(Object pojo) {
        try {
            if (pojo == null) {
                return "";
            }
            var xmlMapper = createXmlMapper();
            return xmlMapper.writeValueAsString(pojo);
        } catch (JsonProcessingException e) {
            throw new XmlParsingException(e);
        }
    }

    /**
     * Create a xml {@link ObjectMapper} with enable IDENT_OUTPUT and disabled FAIL_ON_EMPTY_BEANS. Also add support
     * to {@link JavaTimeModule} from JSR310 dependency
     *
     * @return xml ObjectMapper
     */
    public static ObjectMapper createXmlMapper() {
        return XmlMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .addModule(new JavaTimeModule())
                .build();

    }

    /**
     * Parse XML text to plain old java object
     */
    public static <T> T parseXmlToPojo(String xml, Class<T> pojoType) {
        try {
            if (xml == null || xml.isBlank()) {
                return null;
            }
            var xmlMap = createXmlMapper();
            return xmlMap.readerFor(pojoType).readValue(xml);
        } catch (JsonProcessingException e) {
            throw new XmlParsingException(e);
        }
    }

    /**
     * Parse json to a of Lis<Pojo>
     *
     * @return a List of pojo or an empty List if json is null or blank
     */
    public static <T> List<T> parseJsonToList(String json, Class<T> pojoType) {
        try {
            if (json == null || json.isBlank()) {
                return List.of();
            }

            var jsonMapper = createJsonMapper();
            JavaType type = jsonMapper.getTypeFactory().
                    constructCollectionType(List.class, pojoType);

            return jsonMapper.readerFor(type).readValue(json);
        } catch (IOException e) {
            throw new JsonParsingException(e);
        }
    }

    /**
     * Convert list of pojo to JSON string using Jackson
     *
     * @return json string or empty string is list is null or empty
     */
    public static <T> String convertListToJson(List<T> list) {
        try {
            if (list == null || list.isEmpty()) {
                return "";
            }
            ObjectMapper jsonMapper = createJsonMapper();
            return jsonMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new JsonParsingException(e);
        }
    }

}


