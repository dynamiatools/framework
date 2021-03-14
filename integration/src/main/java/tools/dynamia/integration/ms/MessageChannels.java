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

import tools.dynamia.commons.BeanUtils;
import tools.dynamia.integration.Containers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("rawtypes")
public class MessageChannels {

    /**
     * Lookup listeners.
     *
     * @param channelName the channel uid
     * @return the list
     */
    public static List<MessageListener> lookupListeners(String channelName, String topic) {

        List<MessageListener> result = new ArrayList<>();
        Collection<MessageListener> listeners = Containers.get().findObjects(MessageListener.class);
        if (listeners != null) {
            for (MessageListener messageListener : listeners) {
                MessageChannelExchange exchange = getMessageChannelExchange(messageListener);
                if (exchange != null) {
                    if ((channelMatch(channelName, exchange) && topicMatch(topic, exchange))
                            || (exchange.channel().isEmpty() && topicMatch(topic, exchange))) {
                        result.add(messageListener);
                    }
                } else {
                    result.add(messageListener);
                }
            }
        }

        Collections.sort(result, new MessageChannelExchangeComparator());

        return result;
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
            if (messageTopic.equals(topic) || topic.equals("#")) {
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
