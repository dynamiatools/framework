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
package tools.dynamia.viewers;


/**
 * The Class ViewTypeNotFoundException.
 *
 * @author Mario A. Serrano Leones
 */
public class ViewTypeNotFoundException extends RuntimeException {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = -8076761489343406448L;

    /**
     * Instantiates a new view type not found exception.
     *
     * @param cause the cause
     */
    public ViewTypeNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new view type not found exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public ViewTypeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new view type not found exception.
     *
     * @param message the message
     */
    public ViewTypeNotFoundException(String message) {
        super(message);
    }

    /**
     * Instantiates a new view type not found exception.
     */
    public ViewTypeNotFoundException() {
    }
}
