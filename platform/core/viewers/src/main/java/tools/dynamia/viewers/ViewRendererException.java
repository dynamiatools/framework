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
 * The Class ViewRendererException.
 *
 * @author Mario A. Serrano Leones
 */
public class ViewRendererException extends RuntimeException {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = -5178795511000905514L;

    /**
     * Instantiates a new view renderer exception.
     *
     * @param cause the cause
     */
    public ViewRendererException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new view renderer exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public ViewRendererException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new view renderer exception.
     *
     * @param message the message
     */
    public ViewRendererException(String message) {
        super(message);
    }

    /**
     * Instantiates a new view renderer exception.
     */
    public ViewRendererException() {
    }
}
