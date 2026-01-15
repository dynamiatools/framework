/*
 * Copyright (C) 2025 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Simplified transport class for MessageEvent in distributed systems.
 * <p>
 * This class is designed to facilitate the serialization and transmission of MessageEvent instances
 * across distributed systems such as message queues or storage systems. It flattens the generic
 * MessageEvent into a more straightforward structure with plain fields, making it easier to convert
 * to formats like JSON.
 * <p>
 * Key features:
 * - content: The payload of the message.
 * - headers: Additional metadata as a map.
 * - topic: The topic or channel for the event.
 * - callback: Optional callback information.
 * - createdAt: Timestamp of event creation.
 * - messageType: The simple class name of the original message type.
 * <p>
 * Use the static from() method to convert from MessageEvent to MessageEventTransport.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageEventTransport {

    private Object content;
    private Map<String, Object> headers = new HashMap<>();
    private String topic;
    private String callback;
    private Instant createdAt;
    private String messageType;

    public MessageEventTransport() {
    }

    public MessageEventTransport(Object content, Map<String, Object> headers, String topic, String callback, Instant createdAt, String messageType) {
        this.content = content;
        this.headers = headers != null ? headers : new HashMap<>();
        this.topic = topic;
        this.callback = callback;
        this.createdAt = createdAt;
        this.messageType = messageType;
    }

    /**
     * Conversion from MessageEvent
     *
     * @param event the message event
     * @param <T>   the message type
     * @return the message event transport
     */
    public static <T extends Message> MessageEventTransport from(MessageEvent<T> event) {
        Map<String, Object> headers = new HashMap<>();
        for (String headerName : event.getMessage().getHeaderNames()) {
            headers.put(headerName, event.getMessage().getHeader(headerName));
        }
        String messageType = event.getMessage().getClass().getSimpleName();
        return new MessageEventTransport(event.getContent(), headers, event.getTopic(), event.getCallback(), event.getCreatedAt(), messageType);
    }


    /**
     * Conversion to MessageEvent
     *
     * @return the message event
     */
    public MessageEvent<?> toMessageEvent() {

        String type = messageType != null ? messageType : "ObjectMessage";

        var message = content instanceof String ? new TextMessage((String) content) :
                switch (type) {
                    case "GenericMessage" -> new GenericMessage<>(content);
                    case "TextMessage" -> new TextMessage((String) content);
                    case "MapMessage" -> new MapMessage((Map<String, Object>) content);
                    case "NumberMessage" -> new NumberMessage((Number) content);
                    default -> new ObjectMessage((Serializable) content);
                };

        return new MessageEvent<>(message, topic, callback, createdAt);
    }

    // Getters and setters
    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
