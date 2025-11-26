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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation used to mark a type as a message channel exchange/consumer within the
 * messaging subsystem. It declares the target channel, optional topic filters, the
 * processing priority, and whether broadcast messages should be received.
 * <p>
 * Implementations of the messaging layer (e.g., Spring Application Events, JMS, Kafka,
 * RabbitMQ, Redis Pub/Sub) may use this metadata to register and route messages to
 * annotated components.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MessageChannelExchange {

    /**
     * Logical name of the channel this component listens to. Implementations use this value
     * to bind/subscribe to the underlying messaging destination.
     *
     * @return the channel name; an empty string may be treated as an implementation-specific default
     */
    String channel() default "";

    /**
     * Topic filters used for message routing within the channel. By default, the topic is "*",
     * which means all topics. Implementations may support wildcard patterns; "*" typically
     * matches any topic.
     *
     * @return one or more topic patterns
     */
    String[] topic() default MessageChannels.ALL_TOPICS;

    /**
     * Processing priority for this listener. Lower values indicate higher priority. The default
     * priority is 100.
     *
     * @return the numeric priority value
     */
    int priority() default 100;

    /**
     * Indicates whether this listener should receive broadcast messages in addition to
     * channel-specific messages. The default value is {@code true}.
     *
     * @return {@code true} if broadcast messages should be delivered; {@code false} otherwise
     */
    boolean broadcastReceive() default true;
}
