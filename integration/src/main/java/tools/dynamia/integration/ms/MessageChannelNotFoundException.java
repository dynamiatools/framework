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


/**
 * The Class MessageChannelNotFoundException.
 */
public class MessageChannelNotFoundException extends MessageException {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 2407632589182847206L;

    /**
     * Instantiates a new message channel not found exception.
     */
    public MessageChannelNotFoundException() {
        super();

    }

    /**
     * Instantiates a new message channel not found exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public MessageChannelNotFoundException(String message, Throwable cause) {
        super(message, cause);

    }

    /**
     * Instantiates a new message channel not found exception.
     *
     * @param message the message
     */
    public MessageChannelNotFoundException(String message) {
        super(message);

    }

    /**
     * Instantiates a new message channel not found exception.
     *
     * @param cause the cause
     */
    public MessageChannelNotFoundException(Throwable cause) {
        super(cause);

    }

}
