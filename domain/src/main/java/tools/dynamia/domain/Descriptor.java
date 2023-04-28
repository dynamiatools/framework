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

package tools.dynamia.domain;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD, TYPE})
@Retention(RUNTIME)
@Repeatable(Descriptors.class)
public @interface Descriptor {

    String label() default "";

    String[] fields() default {};

    String description() default "";

    String view() default "";

    String type() default "";

    /**
     * Parameters arrays separated by ':' character
     * Example [param1: value1, param2: value2]
     *
     * @return
     */
    String[] params() default {};

    /**
     * View Parameters arrays separated by ':' character
     * Example [param1: value1, param2: value2]
     *
     * @return
     */
    String[] viewParams() default {};
}

