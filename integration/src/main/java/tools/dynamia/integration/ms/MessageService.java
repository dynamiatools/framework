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

import java.util.Properties;


/**
 * The Interface MessageService.
 */
public interface MessageService {

    /**
     * Create a new message channel, implementation depends on MessageService
     * @param name
     * @return
     */
    MessageChannel createChannel(String name);

    /**
     * Create a new message channel, implementation depends on MessageService
     * @param name
     * @param properties
     * @return
     */
    MessageChannel createChannel(String name, Properties properties);

    /**
     * Post.
     *
     * @param channelName the channel uid
     * @param message the message
     */
    void publish(String channelName, Message message);

    /**
     * Publish.
     *
     * @param channelName the channel name
     * @param message the message
     * @param topic the topic
     */
    void publish(String channelName, Message message, String topic);

    /**
     * Publish.
     *
     * @param channelName the channel name
     * @param message the message
     * @param topic the topic
     * @param callback the callback
     */
    void publish(String channelName, Message message, String topic, String callback);

    /**
     * Broadcast.
     *
     * @param message the message
     */
    void broadcast(Message message);

    /**
     * Broadcast.
     *
     * @param message the message
     * @param topic the topic
     */
    void broadcast(Message message, String topic);

    /**
     * Broadcast.
     *
     * @param message the message
     * @param topic the topic
     * @param callback the callback
     */
    void broadcast(Message message, String topic, String callback);

}
