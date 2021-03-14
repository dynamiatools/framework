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
 * The Class ViewDescriptorNotFoundException.
 *
 * @author Mario A. Serrano Leones
 */
public class ViewDescriptorNotFoundException extends RuntimeException {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 8546895657557321542L;

    /**
     * The bean class.
     */
    private Class<?> beanClass;

    /**
     * The view type.
     */
    private String viewType;

    /**
     * Instantiates a new view descriptor not found exception.
     *
     * @param cause the cause
     */
    public ViewDescriptorNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new view descriptor not found exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public ViewDescriptorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new view descriptor not found exception.
     *
     * @param message the message
     */
    public ViewDescriptorNotFoundException(String message) {
        super(message);
    }

    /**
     * Instantiates a new view descriptor not found exception.
     *
     * @param message the message
     * @param beanClass the bean class
     * @param viewType the view type
     */
    public ViewDescriptorNotFoundException(String message, Class<?> beanClass, String viewType) {
        this.beanClass = beanClass;
        this.viewType = viewType;
    }

    /**
     * Instantiates a new view descriptor not found exception.
     */
    public ViewDescriptorNotFoundException() {
    }

    /**
     * Gets the bean class.
     *
     * @return the bean class
     */
    public Class<?> getBeanClass() {
        return beanClass;
    }

    /**
     * Gets the view type.
     *
     * @return the view type
     */
    public String getViewType() {
        return viewType;
    }
}
