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
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Utility class for parsing and converting between JSON strings and Java objects (POJOs).
 * <p>
 * Provides static methods for serializing objects and maps to JSON, parsing JSON to objects and maps, and creating configured mappers.
 * Supports both JSON and XML formats using Jackson library, and handles Java 8 time types.
 * <p>
 * All methods are stateless and thread-safe.
 *
 * @author Mario A. Serrano Leones
 */
public class StringPojoParser {

    /**
     * Converts a {@link Map} to a JSON string using Jackson.
     *
     * @param map the map to convert
     * @return the JSON string, or an empty string if map is null or empty
     */
    public static String convertMapToJson(Map map) {
        try {
            if (map == null || map.isEmpty()) {
                return "";
            }
            var jsonMapper = createJsonMapper();
            return jsonMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new JsonParsingException(e);
        }
    }

    /**
     * Creates a configured JSON {@link JsonMapper} with indentation, disabled empty beans, and JavaTimeModule support.
     *
     * @return the configured JSON ObjectMapper
     */
    public static JsonMapper createJsonMapper() {
        return JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .addModule(new JavaTimeModule())
                .build();

    }

    /**
     * Converts a POJO to a JSON string using Jackson.
     *
     * @param pojo the object to convert
     * @return the JSON string, or an empty string if pojo is null
     */
    public static String convertPojoToJson(Object pojo) {
        try {
            if (pojo == null) {
                return "";
            }
            var jsonMapper = createJsonMapper();
            return jsonMapper.writeValueAsString(pojo);
        } catch (JsonProcessingException e) {
            throw new JsonParsingException(e);
        }
    }

    /**
     * Parses a JSON string to a {@link Map} using Jackson.
     *
     * @param json the JSON string
     * @return the parsed map, or an empty map if json is null or blank
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

            var jsonMapper = createJsonMapper();
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

            var jsonMapper = createJsonMapper();
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
     * Create a xml {@link XmlMapper} with enable IDENT_OUTPUT and disabled FAIL_ON_EMPTY_BEANS. Also add support
     * to {@link JavaTimeModule} from JSR310 dependency
     *
     * @return xml mapper
     */
    public static XmlMapper createXmlMapper() {
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
            var jsonMapper = createJsonMapper();
            return jsonMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new JsonParsingException(e);
        }
    }

}
