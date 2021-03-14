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


/**
 * The Class FunctionNotFoundException.
 *
 * @author Mario A. Serrano Leones
 */
public class FunctionNotFoundException extends RuntimeException {

    /**
     * Instantiates a new function not found exception.
     */
    public FunctionNotFoundException() {
    }

    /**
     * Instantiates a new function not found exception.
     *
     * @param string the string
     */
    public FunctionNotFoundException(String string) {
        super(string);
    }

    /**
     * Instantiates a new function not found exception.
     *
     * @param string the string
     * @param thrwbl the thrwbl
     */
    public FunctionNotFoundException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    /**
     * Instantiates a new function not found exception.
     *
     * @param thrwbl the thrwbl
     */
    public FunctionNotFoundException(Throwable thrwbl) {
        super(thrwbl);
    }

}
