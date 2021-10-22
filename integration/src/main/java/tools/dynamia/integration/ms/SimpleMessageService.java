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

import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.SimpleObjectContainer;

import java.util.List;
import java.util.Properties;

/**
 * Very simple MessageService implementation, its use a SimpleObjectContainer to
 * find or auto create channels and perform all its operations
 */
public class SimpleMessageService implements MessageService {

    private final SimpleObjectContainer container = new SimpleObjectContainer("LocalMessageChannels");

    @Override
    public MessageChannel createChannel(String name) {
        return createChannel(name, null);
    }

    private final LoggingService LOGGER = new SLF4JLoggingService(SimpleMessageService.class);

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

                e.printStackTrace();
            }
        }

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
        MessageChannel channel = null;
        try {
            channel = createChannel(channelName, null);
            if (channel == null) {
                LOGGER.error("Cannot create channel " + channelName + ". Null returned");
            } else {
                channel.publish(message, topic, callback);
            }
        } catch (Exception e) {
            LOGGER.error("Error publishing Message " + message + " to channel " + channelName + "  (" + channel + "). Topic: " + topic + " - exception " + e.getMessage());
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
        broadcast(message, "");

    }

    @Override
    public void broadcast(Message message, String topic) {
        broadcast(message, topic, "");

    }

    @Override
    public void broadcast(Message message, String topic, String callback) {
        if (message != null) {
            message.addHeader("broadcast", 1);
            List<MessageChannel> channels = container.getObjects(MessageChannel.class);
            channels.forEach(m -> m.publish(message, topic, callback));
        }

    }

}
