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
package tools.dynamia.viewers;


/**
 * The Class InvalidValueForViewException.
 *
 * @author Mario A. Serrano Leones
 */
public class InvalidValueForViewException extends RuntimeException {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = -6797143568116803313L;

    /**
     * Instantiates a new invalid value for view exception.
     *
     * @param cause the cause
     */
    public InvalidValueForViewException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new invalid value for view exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public InvalidValueForViewException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new invalid value for view exception.
     *
     * @param message the message
     */
    public InvalidValueForViewException(String message) {
        super(message);
    }

    /**
     * Instantiates a new invalid value for view exception.
     */
    public InvalidValueForViewException() {
    }
}
