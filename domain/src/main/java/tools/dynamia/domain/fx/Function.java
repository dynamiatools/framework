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
package tools.dynamia.domain.fx;

import java.util.Map;


/**
 * The Interface Function.
 *
 * @author Mario A. Serrano Leones
 * @param <D> data to processs
 * @param <R> Result type
 */
public interface Function<D, R> {

    /**
     * Gets the name.
     *
     * @return the name
     */
    String getName();

    /**
     * Gets the arguments names.
     *
     * @return the arguments names
     */
    String[] getArgumentsNames();

    /**
     * Compute.
     *
     * @param value the value
     * @param args the args
     * @return the r
     */
    R compute(D value, Map<String, Object> args);

}
