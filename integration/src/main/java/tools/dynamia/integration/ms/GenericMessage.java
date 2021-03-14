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
package tools.dynamia.integration.ms;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * The Class SimpleMessage.
 *
 * @param <T> the generic type
 */
public class GenericMessage<T> implements Message {

    /**
     * The headers.
     */
    private Map<String, Object> headers = new HashMap<>();

    /**
     * The content.
     */
    private T content;

    /**
     * Instantiates a new simple message.
     *
     * @param content the content
     */
    public GenericMessage(T content) {
        super();
        this.content = content;
        addHeader(HEADER_TIMESTAMP, System.currentTimeMillis());
    }

    public GenericMessage(String name, T content) {
        super();
        this.content = content;
        addHeader(HEADER_NAME, name);
        addHeader(HEADER_TIMESTAMP, System.currentTimeMillis());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.integration.ms.Message#getHeaders()
     */
    @Override
    public Set<String> getHeaderNames() {
        return headers.keySet();

    }

    @Override
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    @Override
    public void addHeader(String name, int value) {
        headers.put(name, value);
    }

    @Override
    public void addHeader(String name, long value) {
        headers.put(name, value);
    }

    @Override
    public void addHeader(String name, Number value) {
        headers.put(name, value);
    }

    public void addHeader(String name, Serializable value) {
        headers.put(name, value);
    }

    @Override
    public Object getHeader(String name) {
        return headers.get(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.integration.ms.Message#getContent()
     */
    @Override
    public T getContent() {
        return content;
    }

}
