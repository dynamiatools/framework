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
package tools.dynamia.integration.scheduling;


/**
 * The Class TaskException.
 *
 * @author Mario A. Serrano Leones
 */
public class TaskException extends RuntimeException {

    /**
     * Instantiates a new task exception.
     */
    public TaskException() {
    }

    /**
     * Instantiates a new task exception.
     *
     * @param message the message
     */
    public TaskException(String message) {
        super(message);
    }

    /**
     * Instantiates a new task exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public TaskException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new task exception.
     *
     * @param cause the cause
     */
    public TaskException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new task exception.
     *
     * @param message the message
     * @param cause the cause
     * @param enableSuppression the enable suppression
     * @param writableStackTrace the writable stack trace
     */
    public TaskException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
