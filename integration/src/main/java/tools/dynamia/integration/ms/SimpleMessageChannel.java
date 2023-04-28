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

import tools.dynamia.integration.scheduling.SchedulerUtil;
import tools.dynamia.integration.scheduling.Task;

import java.util.List;

/**
 * The Class SimpleMessageChannel.
 */
@SuppressWarnings("rawtypes")
public class SimpleMessageChannel implements MessageChannel {

    private final String name;
    private boolean async = false;

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
        List<MessageListener> listeners = MessageChannels.lookupListeners(getName(), topic);
        message.addHeader(Message.HEADER_LISTENER_COUNT, 0);
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

    private boolean fireListener(MessageEvent event, MessageListener messageListener) {
        try {
            messageListener.onMessage(event);
            event.getMessage().addHeader(Message.HEADER_LISTENER_COUNT,
                    (Integer) event.getMessage().getHeader(Message.HEADER_LISTENER_COUNT) + 1);
            return true;
        } catch (ClassCastException e) {
            // No generic type, nothing to do
        } catch (Throwable e) {
            var msg = "Exception firing " + event + " to listener " + messageListener + ": " + e.getMessage();
            if (messageListener instanceof MessageExceptionHandler) {
                ((MessageExceptionHandler) messageListener).onMessageException(event, new MessageException(msg, e));
            } else {
                throw new MessageException(msg, e);
            }

        }
        return false;
    }
}
