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

import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.integration.Containers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageChannels {

    public static final String ALL_TOPICS = "*";

    /**
     * Lookup listeners.
     *
     * @param channelName the channel uid
     * @return the list
     */
    public static List<MessageListener> lookupListeners(String channelName, String topic) {


        Collection<MessageListener> listeners = Containers.get().findObjects(MessageListener.class);

        return filterListeners(channelName, topic, listeners);
    }

    public static List<MessageListener> filterListeners(String channelName, String topic, Collection<MessageListener> listeners) {
        List<MessageListener> result = new ArrayList<>();
        if (listeners != null) {
            for (MessageListener messageListener : listeners) {
                if (match(channelName, topic, messageListener)) {
                    result.add(messageListener);
                }
            }
        }
        result.sort(new MessageChannelExchangeComparator());
        return result;
    }

    public static boolean match(String channelName, String topic, MessageListener<?> messageListener) {
        MessageChannelExchange exchange = getMessageChannelExchange(messageListener);
        if (exchange != null) {
            return (channelMatch(channelName, exchange) && topicMatch(topic, exchange))
                    || (exchange.channel().isEmpty() && topicMatch(topic, exchange));
        } else {
            return true;
        }
    }

    public static MessageChannelExchange getMessageChannelExchange(MessageListener messageListener) {
        MessageChannelExchange exchange = null;
        if (BeanUtils.isAnnotated(MessageChannelExchange.class, messageListener.getClass())) {
            exchange = messageListener.getClass().getAnnotation(MessageChannelExchange.class);

        }
        return exchange;
    }

    private static boolean channelMatch(String channelName, MessageChannelExchange exchange) {
        return channelName.equals(exchange.channel());
    }

    private static boolean topicMatch(String messageTopic, MessageChannelExchange exchange) {

        boolean match = false;
        for (String topic : exchange.topic()) {
            if (messageTopic.equals(topic) || topic.equals(ALL_TOPICS)) {
                match = true;
            } else {
                Pattern pattern = Pattern.compile(topic);
                Matcher matcher = pattern.matcher(messageTopic);
                match = matcher.matches();
            }

            if (match) {
                break;
            }
        }

        return match;
    }

    private MessageChannels() {
    }

}
