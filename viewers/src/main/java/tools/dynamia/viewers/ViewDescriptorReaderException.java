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
 * The Class ViewDescriptorReaderException.
 *
 * @author Mario A. Serrano Leones
 */
public class ViewDescriptorReaderException extends RuntimeException {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = -8353517894242739046L;

    /**
     * Instantiates a new view descriptor reader exception.
     *
     * @param cause the cause
     */
    public ViewDescriptorReaderException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new view descriptor reader exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public ViewDescriptorReaderException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new view descriptor reader exception.
     *
     * @param message the message
     */
    public ViewDescriptorReaderException(String message) {
        super(message);
    }

    /**
     * Instantiates a new view descriptor reader exception.
     */
    public ViewDescriptorReaderException() {
    }
}
