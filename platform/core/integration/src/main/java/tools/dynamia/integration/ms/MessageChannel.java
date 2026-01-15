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
 * Represents a communication channel in the messaging system. A channel acts as a conduit
 * for publishing messages to subscribers. The actual implementation depends on the underlying
 * messaging infrastructure (e.g., JMS Queue, Kafka Topic, RabbitMQ Exchange, Redis Channel).
 * <p>
 * Channels support topic-based message routing and callback mechanisms for acknowledgments
 * and response handling.
 * </p>
 */
public interface MessageChannel {

    /**
     * Returns the unique name identifier of this channel.
     *
     * @return the channel name
     */
    String getName();

    /**
     * Publishes a message to this channel. The message will be delivered to all
     * active subscribers of this channel.
     *
     * @param message the message to publish
     */
    void publish(Message message);

    /**
     * Publishes a message to this channel with topic-based routing. Subscribers can
     * filter and receive messages based on the specified topic for more granular control
     * over message consumption.
     *
     * @param message the message to publish
     * @param topic   the topic identifier for message routing and filtering
     */
    void publish(Message message, String topic);

    /**
     * Publishes a message to this channel with topic-based routing and callback support.
     * The callback can be used for acknowledgment handling, error reporting, or routing
     * responses back to the message publisher.
     *
     * @param message  the message to publish
     * @param topic    the topic identifier for message routing and filtering
     * @param callback the callback identifier for handling responses or acknowledgments
     */
    void publish(Message message, String topic, String callback);

    /**
     * Subscribes a listener to this channel to receive messages. The listener will be
     * invoked whenever a new message is published to the channel.
     *
     * @param listener the message listener to subscribe
     * @return a MessageChannelSubscription representing the subscription
     */
    <T extends Message> MessageChannelSubscription subscribe(MessageListener<T> listener);

    /**
     * Subscribes a listener to this channel for a specific topic. The listener will
     * only receive messages that match the specified topic.
     *
     * @param topic    the topic to subscribe to
     * @param listener the message listener to subscribe
     * @return a MessageChannelSubscription representing the subscription
     */
    <T extends Message> MessageChannelSubscription subscribe(String topic, MessageListener<T> listener);

}
