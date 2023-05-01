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

import java.util.Set;


/**
 * The Interface Message.
 *
 */
public interface Message {

    String HEADER_NAME = "MessageName";
    String HEADER_TYPE = "MessageType";
    String HEADER_SOURCE = "MessageSource";
    String HEADER_TIMESTAMP = "MessageTimestamp";
    String HEADER_DESCRIPTION = "MessageDescription";
    String HEADER_LISTENER_COUNT = "MessageListenerCount";
    String HEADER_CORRELATION_ID = "MessageCorrelationId";

    /**
     * Gets the headers names.
     *
     * @return the headers
     */
    Set<String> getHeaderNames();

    /**
     * Adds the header.
     *
     * @param name  the name
     * @param value the value
     */
    void addHeader(String name, String value);

    void addHeader(String name, int value);

    void addHeader(String name, long value);

    void addHeader(String name, Number value);


    /**
     * Gets the header.
     *
     * @param name the name
     * @return the header
     */
    Object getHeader(String name);

    /**
     * Gets the content.
     *
     * @return the content
     */
    Object getContent();

}
