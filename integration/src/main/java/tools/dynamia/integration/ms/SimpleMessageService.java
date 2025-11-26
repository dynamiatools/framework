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

import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.integration.SimpleObjectContainer;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Very simple MessageService implementation, its use a SimpleObjectContainer to
 * find or auto create channels and perform all its operations
 */
public class SimpleMessageService implements MessageService {


    private final SimpleObjectContainer container = new SimpleObjectContainer("LocalMessageChannels");
    private final LoggingService LOGGER = LoggingService.get(SimpleMessageService.class);

    /**
     * Creates a new MessageChannel with the given name, if exists returns the existing one
     *
     * @param name the unique name identifier for the channel
     * @return the MessageChannel instance
     */
    @Override
    public MessageChannel createChannel(String name) {
        return createChannel(name, null);
    }


    @Override
    public MessageChannel createChannel(String name, Properties properties) {
        SimpleMessageChannel channel = container.getObject(name, SimpleMessageChannel.class);
        if (channel == null) {
            channel = new SimpleMessageChannel(name);
            configureProperties(channel, properties);
            container.addObject(name, channel);
        }
        return channel;
    }

    private void configureProperties(SimpleMessageChannel channel, Properties properties) {
        if (properties != null) {
            try {
                channel.setAsync(Boolean.parseBoolean(properties.getProperty("async", "false")));
            } catch (Exception e) {
                LOGGER.error("Error parsing property async: " + e.getMessage());
            }
        }

    }

    @Override
    public Optional<MessageChannel> getChannel(String name) {
        return Optional.ofNullable(container.getObject(name, SimpleMessageChannel.class));
    }

    @Override
    public void publish(String channelName, Message message) {
        publish(channelName, message, "");

    }

    @Override
    public void publish(String channelName, Message message, String topic) {
        publish(channelName, message, topic, "");

    }

    @Override
    public void publish(String channelName, Message message, String topic, String callback) {
        try {
            MessageChannel channel = createChannel(channelName);
            channel.publish(message, topic, callback);

        } catch (Exception e) {
            LOGGER.error("Error publishing Message " + message + " to channel " + channelName + "   Topic: " + topic + " - exception " + e.getMessage());
            if (e.getClass().getName().contains("ValidationError")) {
                throw e;
            }
            if (e.getCause() != null && e.getCause().getClass().getName().contains("ValidationError")) {
                throw e;
            }
        }
    }

    @Override
    public void broadcast(Message message) {
        broadcast(message, MessageChannels.ALL_TOPICS);

    }

    @Override
    public void broadcast(Message message, String topic) {
        broadcast(message, topic, "");

    }

    @Override
    public void broadcast(Message message, String topic, String callback) {
        if (message != null) {
            LOGGER.info("Broadcasting Message " + message + " to topic " + topic);
            List<MessageChannel> channels = container.getObjects(MessageChannel.class);
            message.addHeader("broadcast", 1);
            message.addHeader("channelCount", channels.size());
            channels.forEach(m -> m.publish(message, topic, callback));
        }
    }

    @Override
    public <T extends Message> MessageChannelSubscription subscribe(String channelName, MessageListener<T> listener) {
        LOGGER.info("Subscribing to channel: " + channelName + " listener: " + listener);
        return createChannel(channelName).subscribe(listener);
    }

    @Override
    public <T extends Message> MessageChannelSubscription subscribe(String channelName, String topic, MessageListener<T> listener) {
        LOGGER.info("Subscribing to channel: " + channelName + " topic: " + topic + " listener: " + listener);
        return createChannel(channelName).subscribe(topic, listener);
    }
}
