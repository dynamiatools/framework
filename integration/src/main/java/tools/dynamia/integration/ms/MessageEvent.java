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

/**
 * Represent a message event
 *
 * @param <T>
 */
public record MessageEvent<T extends Message>(T message, String topic, String callback) {

    /**
     * Shortcut to getMessage().getContent()
     */
    public Object getContent() {
        return message().getContent();
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


    public String getCallback() {
        return callback;
    }
}
