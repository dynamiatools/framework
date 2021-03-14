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
package tools.dynamia.commons.reflect;


/**
 * The Class ReflectionException.
 *
 * @author Mario A. Serrano Leones
 */
public class ReflectionException extends RuntimeException {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 7140889099481447468L;

    /**
     * Instantiates a new reflection exception.
     */
    public ReflectionException() {

    }

    /**
     * Instantiates a new reflection exception.
     *
     * @param message the message
     */
    public ReflectionException(String message) {
        super(message);
    }

    /**
     * Instantiates a new reflection exception.
     *
     * @param cause the cause
     */
    public ReflectionException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new reflection exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public ReflectionException(String message, Throwable cause) {
        super(message, cause);

    }

}
