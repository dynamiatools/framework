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

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;


/**
 * Generic abstraction for messaging systems that can be used as a facade for various messaging implementations
 * including Spring Application Events, JMS, RabbitMQ, Kafka, Redis Pub/Sub, and other message-oriented middleware.
 * <p>
 * This service provides a unified API for publishing messages to specific channels or broadcasting to all listeners,
 * with optional topic-based routing and callback support. The actual implementation depends on the underlying
 * messaging system being used.
 * </p>
 * <p>
 * Channels are similar to topics or queues in traditional messaging systems, allowing for organized message distribution.
 * Messages can be published to specific channels, and listeners can subscribe to these channels to receive messages.
 * </p>
 * <p>
 * Subscriptions can be registered using the {@link MessageChannel} interface obtained from the {@link #createChannel(String)} method.
 * or like a funcional interface using lambda expressions.
 * </p>
 * <p>
 * By default, a simple in-memory implementation is provided for basic use cases and testing purposes.
 * </p>
 *
 * @author Mario A. Serrano Leones
 * @see SimpleMessageService, {@link MessageChannel}, {@link Message}, {@link MessageListener}, {@link MessageEvent}, {@link MessageChannelExchange}
 */
public interface MessageService {

    /**
     * Creates a new message channel with the specified name. The actual channel implementation
     * depends on the underlying messaging system (e.g., JMS Queue, Kafka Topic, Redis Channel).
     *
     * @param name the unique name identifier for the channel
     * @return a MessageChannel instance for the created channel
     */
    MessageChannel createChannel(String name);

    /**
     * Creates a new message channel with the specified name and custom properties.
     * Properties can be used to configure channel-specific settings such as durability,
     * message TTL, persistence, etc., depending on the messaging system implementation.
     *
     * @param name       the unique name identifier for the channel
     * @param properties custom configuration properties for the channel
     * @return a MessageChannel instance for the created channel
     */
    MessageChannel createChannel(String name, Properties properties);

    /**
     * Retrieves an existing message channel by its name.
     *
     * @param name the unique name identifier of the channel
     * @return an Optional containing the MessageChannel if found, or empty if not found
     */
    Optional<MessageChannel> getChannel(String name);

    /**
     * Publishes a message to a specific channel. The message will be delivered to all
     * subscribers of the specified channel.
     *
     * @param channelName the name of the channel to publish to
     * @param message     the message to publish
     */
    void publish(String channelName, Message message);

    /**
     * Publishes a message to a specific channel. The message can be of type Message, String, or Map.
     * If the message is a String, it will be wrapped in a TextMessage. If it's a Map, it will be
     * wrapped in a MapMessage.
     *
     * @param channelName the name of the channel to publish to
     * @param message     the message to publish (Message, String, or Map)
     */
    default void publish(String channelName, Serializable message) {
        publish(channelName, message, "");
    }

    /**
     * Publishes a message to a specific channel with topic-based routing.
     * The message can be of type Message, String, or Map.
     * If the message is a String, it will be wrapped in a TextMessage. If it's a Map, it will be
     * wrapped in a MapMessage.
     *
     * @param channelName the name of the channel to publish to
     * @param message     the message to publish (Message, String, or Map)
     * @param topic       the topic identifier for message routing/filtering
     */
    default void publish(String channelName, Serializable message, String topic) {
        if (message instanceof Message msg) {
            publish(channelName, msg);
        } else if (message instanceof String strMsg) {
            publish(channelName, new TextMessage(strMsg));
        } else if (message instanceof Map<?, ?> map) {
            publish(channelName, new MapMessage((Map<String, Object>) map));
        } else if (message instanceof Number number) {
            publish(channelName, new NumberMessage(number));
        } else {
            publish(channelName, new ObjectMessage(message));
        }
    }

    /**
     * Publishes a message to a specific channel with topic-based routing.
     * Subscribers can filter messages based on the topic for more granular control.
     *
     * @param channelName the name of the channel to publish to
     * @param message     the message to publish
     * @param topic       the topic identifier for message routing/filtering
     */
    void publish(String channelName, Message message, String topic);

    /**
     * Publishes a message to a specific channel with topic-based routing and callback support.
     * The callback can be used for acknowledgment, error handling, or response routing.
     *
     * @param channelName the name of the channel to publish to
     * @param message     the message to publish
     * @param topic       the topic identifier for message routing/filtering
     * @param callback    the callback identifier for handling responses or acknowledgments
     */
    void publish(String channelName, Message message, String topic, String callback);

    /**
     * Broadcasts a message to all available channels and listeners in the messaging system.
     * This is useful for system-wide notifications or events that need to reach all components.
     *
     * @param message the message to broadcast
     */
    void broadcast(Message message);

    /**
     * Broadcasts a message to all available channels and listeners with topic-based filtering.
     * Only subscribers interested in the specified topic will receive the message.
     *
     * @param message the message to broadcast
     * @param topic   the topic identifier for message filtering
     */
    void broadcast(Message message, String topic);

    /**
     * Broadcasts a message to all available channels and listeners with topic-based filtering
     * and callback support. The callback can be used for acknowledgment or response handling.
     *
     * @param message  the message to broadcast
     * @param topic    the topic identifier for message filtering
     * @param callback the callback identifier for handling responses or acknowledgments
     */
    void broadcast(Message message, String topic, String callback);

    /**
     * Subscribes a listener to a specific channel. The listener will receive
     * all messages published to the specified channel.
     *
     * @param channelName the name of the channel to subscribe to
     * @param listener    the MessageListener that will handle incoming messages
     * @return a MessageChannelSubscription representing the subscription
     */
    <T extends Message> MessageChannelSubscription subscribe(String channelName, MessageListener<T> listener);

    /**
     * Subscribes a listener to a specific channel and topic. The listener will receive
     * messages published to the specified channel that match the given topic.
     *
     * @param channelName the name of the channel to subscribe to
     * @param topic       the topic identifier for filtering messages
     * @param listener    the MessageListener that will handle incoming messages
     * @return a MessageChannelSubscription representing the subscription
     */
    <T extends Message> MessageChannelSubscription subscribe(String channelName, String topic, MessageListener<T> listener);


    /**
     * Subscribes to a channel with a simple text message consumer. Its acts as a shortcut for a MessageListener that processes TextMessage only.
     *
     * @param channelName
     * @param onTextMessage
     * @param <T>
     * @return
     */
    default <T extends Message> MessageChannelSubscription subscribeText(String channelName, Consumer<String> onTextMessage) {
        return subscribe(channelName, (MessageEvent<TextMessage> evt) -> {
            onTextMessage.accept(evt.message().getContent());
        });
    }

}
