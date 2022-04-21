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
package tools.dynamia.viewers.impl;

import tools.dynamia.viewers.ViewLayout;


/**
 * The Class DefaultViewDescriptor.
 *
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings({"rawtypes"})
public class DefaultViewDescriptor extends AbstractViewDescriptor{

    /**
     * The layout.
     */
    private ViewLayout layout;

    /**
     * Instantiates a new default view descriptor.
     */
    public DefaultViewDescriptor() {
    }

    /**
     * Instantiates a new default view descriptor.
     *
     * @param beanClass    the bean class
     * @param viewTypeName the view type name
     */
    public DefaultViewDescriptor(Class beanClass, String viewTypeName) {
        super(beanClass, viewTypeName);
    }

    /**
     * Instantiates a new default view descriptor.
     *
     * @param beanClass    the bean class
     * @param viewTypeName the view type name
     * @param autofields   the autofields
     */
    public DefaultViewDescriptor(Class<?> beanClass, String viewTypeName, boolean autofields) {
        super(beanClass, viewTypeName, autofields);

    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.viewers.ViewDescriptor#getLayout()
     */
    @Override
    public ViewLayout getLayout() {
        if (layout == null) {
            layout = new DefaultViewLayout();
        }

        return layout;
    }
}
