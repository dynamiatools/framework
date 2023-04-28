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
 * A factory for creating View objects.
 *
 * @author Mario A. Serrano Leones
 */
public interface ViewFactory {

    /**
     * Gets the view.
     *
     * @param viewDescriptor the view descriptor
     * @return the view
     */
    View getView(ViewDescriptor viewDescriptor);

    /**
     * Gets the view.
     *
     * @param <T>            the generic type
     * @param viewDescriptor the view descriptor
     * @param value          the value
     * @return the view
     */
    <T> View<T> getView(ViewDescriptor viewDescriptor, T value);

    /**
     * Gets the view.
     *
     * @param <T>   the generic type
     * @param type  the type
     * @param value the value
     * @return the view
     */
    <T> View<T> getView(String type, T value);

    /**
     * Gets the view.
     *
     * @param <T>    the generic type
     * @param type   the type
     * @param device the device
     * @param value  the value
     * @return the view
     */
    <T> View<T> getView(String type, String device, T value);

    /**
     * Gets the view.
     *
     * @param <T>       the generic type
     * @param type      the type
     * @param value     the value
     * @param beanClass the bean class
     * @return the view
     */
    <T> View<T> getView(String type, T value, Class<?> beanClass);

    /**
     * Gets the view.
     *
     * @param <T>       the generic type
     * @param type      the type
     * @param device    the device
     * @param value     the value
     * @param beanClass the bean class
     * @return the view
     */
    <T> View<T> getView(String type, String device, T value, Class<?> beanClass);

    /**
     * Gets the view.
     *
     * @param <T>            the generic type
     * @param type           the type
     * @param value          the value
     * @param viewDescriptor the view descriptor
     * @return the view
     */
    <T> View<T> getView(String type, T value, ViewDescriptor viewDescriptor);

}
