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

import tools.dynamia.commons.StringUtils;
import tools.dynamia.integration.scheduling.SchedulerUtil;
import tools.dynamia.integration.scheduling.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Class SimpleMessageChannel.
 */
@SuppressWarnings("rawtypes")
public class SimpleMessageChannel implements MessageChannel {

    private final String name;
    private boolean async = false;
    private final Map<String, BaseMessageChannelSubscription> subscriptions = new ConcurrentHashMap<>();

    /**
     * Instantiates a new simple message channel.
     *
     * @param name the uid
     */
    public SimpleMessageChannel(String name) {
        super();
        this.name = name;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.integration.ms.MessageChannel#getUID()
     */
    @Override
    public String getName() {
        return name;
    }

    public boolean isAsync() {
        return async;
    }

    void setAsync(boolean async) {
        this.async = async;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.integration.ms.MessageChannel#post(com.dynamia.tools
     * .integration.ms.Message)
     */
    @Override
    public void publish(Message message) {
        publish(message, "");
    }

    @Override
    public void publish(Message message, String topic) {
        publish(message, topic, "");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void publish(Message message, String topic, String callback) {
        MessageEvent event = new MessageEvent(message, topic, callback);
        List<MessageListener> listeners = getMessageListeners(topic);
        message.addHeader(Message.HEADER_LISTENER_COUNT, listeners.size());

        for (MessageListener messageListener : listeners) {
            if (!isAsync()) {
                fireListener(event, messageListener);
            } else {
                SchedulerUtil.run(new Task("MessageListenerTask: " + messageListener) {
                    @Override
                    public void doWork() {
                        fireListener(event, messageListener);
                    }
                });
            }
        }
    }

    /**
     * Find and filter message listeners for topic.
     *
     * @param topic the topic
     * @return the list of message listeners
     */
    private List<MessageListener> getMessageListeners(String topic) {
        List<MessageListener> listeners = new ArrayList<>();

        var subscriptionListeners = subscriptions.values()
                .stream().map(BaseMessageChannelSubscription::getListener)
                .toList();

        listeners.addAll(MessageChannels.filterListeners(getName(), topic, subscriptionListeners));
        listeners.addAll(MessageChannels.lookupListeners(getName(), topic));

        return listeners;
    }

    /**
     * Fire listener.
     *
     * @param event           the event
     * @param messageListener the message listener
     */
    private void fireListener(MessageEvent event, MessageListener messageListener) {
        try {
            //noinspection unchecked
            messageListener.onMessage(event);
            event.message().addHeader(Message.HEADER_LISTENER_COUNT,
                    (Integer) event.message().getHeader(Message.HEADER_LISTENER_COUNT) + 1);
        } catch (ClassCastException e) {
            // No generic type, nothing to do
        } catch (Throwable e) {
            var msg = "Exception firing " + event + " to listener " + messageListener + ": " + e.getMessage();
            if (messageListener instanceof MessageExceptionHandler) {
                //noinspection unchecked
                ((MessageExceptionHandler) messageListener).onMessageException(event, new MessageException(msg, e));
            } else {
                throw new MessageException(msg, e);
            }
        }
    }

    @Override
    public <T extends Message> MessageChannelSubscription subscribe(MessageListener<T> listener) {
        return subscribe(MessageChannels.ALL_TOPICS, listener);
    }

    @Override
    public <T extends Message> MessageChannelSubscription subscribe(String topic, MessageListener<T> listener) {
        String subscriptionId = StringUtils.randomString();
        var subcription = new BaseMessageChannelSubscription<>(getName(), subscriptionId, topic, listener) {
            @Override
            public void unsubscribe() {
                subscriptions.remove(subscriptionId);
            }
        };
        subscriptions.put(subscriptionId, subcription);

        return subcription;
    }
}
