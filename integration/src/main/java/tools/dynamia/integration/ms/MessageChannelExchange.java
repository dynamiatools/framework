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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * The Interface MessageChannelExchange.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MessageChannelExchange {

    /**
     * Channel name.
     *
     * @return the string
     */
    String channel() default "";

    /**
     * Channel Filter, by default topic is #, thats mean all topics.
     *
     * @return the string
     */
    String[] topic() default "#";

    /**
     * Priority. low value is hight priority, by default priority is 100
     *
     * @return the int
     */
    int priority() default 100;

    /**
     * Specified is this Listener receive broadcast messages, default is true
     *
     * @return
     */
    boolean broadcastReceive() default true;
}
