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
 * A factory for creating ViewDescriptor objects.
 *
 * @author Mario A. Serrano Leones
 */
public interface ViewDescriptorFactory {

    /**
     * Gets the descriptor.
     *
     * @param id the id
     * @return the descriptor
     */
    ViewDescriptor getDescriptor(String id);

    /**
     * Gets the descriptor. If descriptor is not found a new one may be created
     *
     * @param targetClass the target class
     * @param viewType the view type
     * @return the descriptor
     */
    ViewDescriptor getDescriptor(Class<?> targetClass, String viewType);

    /**
     * Gets the descriptor. If descriptor is not found a new one may be created
     *
     * @param beanClass
     * @param device
     * @param viewType
     * @return
     */
    ViewDescriptor getDescriptor(Class beanClass, String device, String viewType);

    /**
     * find view descriptor, return null if not found.
     *
     * @param beanClass
     * @param device
     * @param viewType
     * @return
     */
    ViewDescriptor findDescriptor(Class beanClass, String device, String viewType);

    void loadViewDescriptors();

    /**
     * find view descriptor, return null if not found.
     *
     * @param targetClass the target class
     * @param viewType the view type
     * @return the descriptor
     */
    ViewDescriptor findDescriptor(Class beanClass, String viewType);

	ViewDescriptor getDescriptor(String id, String device);
}
