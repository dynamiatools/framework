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

import java.time.Instant;

/**
 * Represent a message event
 *
 * @param <T>
 */
public class MessageEvent<T extends Message> {

    private final T message;
    private final String topic;
    private final String callback;
    private final Instant createdAt;

    public MessageEvent(T message) {
        this(message, null, null);
    }

    public MessageEvent(T message, String topic) {
        this(message, topic, null);
    }

    public MessageEvent(T message, String topic, String callback) {
        this(message, topic, callback, Instant.now());
    }

    public MessageEvent(T message, String topic, String callback, Instant createdAt) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }

        this.message = message;
        this.topic = topic;
        this.callback = callback;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public T message() {
        return message;
    }

    /**
     * Shortcut to getMessage().getContent()
     */
    public Object getContent() {
        return message().getContent();
    }

    public boolean hasCallback() {
        return callback != null && !callback.isBlank();
    }

    public boolean hasTopic() {
        return topic != null && !topic.isBlank();
    }

    public boolean hasContent() {
        return message != null && message.getContent() != null;
    }

    @Override
    public String toString() {
        return "MessageEvent{" +
                "message=" + message +
                ", topic='" + topic + '\'' +
                ", callback='" + callback + '\'' +
                '}';
    }


    public T getMessage() {
        return message;
    }


    public String getTopic() {
        return topic;
    }

    public String topic() {
        return topic;
    }

    public String getCallback() {
        return callback;
    }

    public String callback() {
        return callback;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
