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
package tools.dynamia.zk;

import org.zkoss.bind.sys.BinderCtrl;
import org.zkoss.zk.ui.event.EventQueues;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subscribe {

    /**
     * EventQueue name
     *
     * @return
     */
    String value() default BinderCtrl.DEFAULT_QUEUE_NAME;

    /**
     * Target eventName
     *
     * @return
     */
    String eventName() default "";

    /**
     * EventQueue Scope
     *
     * @return
     */
    String scope() default EventQueues.DESKTOP;

    /**
     * Autocreate EventQueue if not exists
     *
     * @return
     */
    boolean autocreate() default true;

    /**
     * Async subscriber invocation
     *
     * @return
     */
    boolean async() default false;

    String[] command() default {};

}
