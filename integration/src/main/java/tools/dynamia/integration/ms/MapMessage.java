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
package tools.dynamia.integration.ms;

import java.util.Map;


/**
 * The Class MapMessage.
 */
public class MapMessage extends GenericMessage<Map<String, Object>> {

    /**
     * Instantiates a new map message.
     *
     * @param content the content
     */
    public MapMessage(Map<String, Object> content) {
        super(content);
        
    }

    public MapMessage(String name, Map<String, Object> content) {
        super(name, content);
        
    }

    /**
     * Gets the value.
     *
     * @param key the key
     * @return the value
     */
    public Object getValue(String key) {
        return getContent().get(key);
    }
}
