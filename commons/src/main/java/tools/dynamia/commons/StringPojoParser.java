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
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class StringPojoParser {

    /**
     * Convert Map to JSON string using Jackson
     *
     * @param map
     * @return
     */
    public static String convertMapToJson(Map map) {
        try {
            ObjectMapper jsonMapper = createJsonMapper();


            return jsonMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new JsonParsingException(e);
        }
    }

    public static ObjectMapper createJsonMapper() {
        var jsonMapper = new ObjectMapper();
        jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);
        jsonMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        return jsonMapper;
    }

    /**
     * Convert bean to JSON string using Jackson
     *
     * @param pojo
     * @return
     */
    public static String convertPojoToJson(Object pojo) {
        try {
            ObjectMapper jsonMapper = createJsonMapper();
            return jsonMapper.writeValueAsString(pojo);
        } catch (JsonProcessingException e) {
            throw new JsonParsingException(e);
        }
    }

    /**
     * Parse JSON string to Map using Jackson
     *
     * @param json
     * @return
     */
    public static Map<String, Object> parseJsonToMap(String json) {
        try {
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
     * @param <T>
     * @return
     */
    public static <T> T parseJsonToPojo(String json, Class<T> pojoType) {
        try {
            ObjectMapper jsonMapper = createJsonMapper();
            return jsonMapper.readerFor(pojoType).readValue(json);
        } catch (IOException e) {
            throw new JsonParsingException(e);
        }
    }

    /**
     * Convert any plain old java object to XML
     *
     * @param pojo
     * @return
     */
    public static String convertPojoToXml(Object pojo) {
        try {
            XmlMapper xmlMapper = createXmlMapper();
            return xmlMapper.writeValueAsString(pojo);
        } catch (JsonProcessingException e) {
            throw new XmlParsingException(e);
        }
    }

    public static XmlMapper createXmlMapper() {
        var xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        return xmlMapper;
    }

    /**
     * Parse XML text to plain old java object
     *
     * @param xml
     * @param pojoType
     * @param <T>
     * @return
     */
    public static <T> T parseXmlToPojo(String xml, Class<T> pojoType) {
        try {
            XmlMapper xmlMap = createXmlMapper();
            return xmlMap.readerFor(pojoType).readValue(xml);
        } catch (JsonProcessingException e) {
            throw new XmlParsingException(e);
        }
    }

    /**
     * Parse json to a of Lis<Pojo>
     *
     * @param json
     * @param pojoType
     * @param <T>
     * @return
     */
    public static <T> List<T> parseJsonToList(String json, Class<T> pojoType) {
        try {

            ObjectMapper jsonMapper = createJsonMapper();
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
     * @param list
     * @return
     */
    public static <T> String convertListToJson(List<T> list) {
        try {
            ObjectMapper jsonMapper = createJsonMapper();
            return jsonMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new JsonParsingException(e);
        }
    }

}


