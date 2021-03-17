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

import org.springframework.beans.factory.annotation.Autowired;
import tools.dynamia.commons.SimpleCache;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.sterotypes.Component;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.ViewDescriptorsProvider;

import java.util.Collection;

/**
 * A factory for creating DefaultViewDescriptor objects.
 *
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings({"rawtypes"})
@Component
public class DefaultViewDescriptorFactory extends AbstractViewDescriptorFactory {

    private final LoggingService logger = new SLF4JLoggingService(DefaultViewDescriptor.class);

    @Autowired
    private Collection<ViewDescriptorsProvider> providers;


    @Override
    public void loadViewDescriptors() {
        loadViewDescriptorFromProviders(providers);
        loadViewDescriptorsFromFiles();
    }

    /**
     * Default view descriptors cache
     */
    private final SimpleCache<String, SimpleCache<Class, ViewDescriptor>> cache = new SimpleCache<>();

    /* (non-Javadoc)
     * @see com.dynamia.tools.viewers.impl.AbstractViewDescriptorFactory#getDefaultViewDescriptor(java.lang.Class, java.lang.String)
     */
    @Override
    public ViewDescriptor getDefaultViewDescriptor(Class beanClass, String viewType) {

        SimpleCache<Class, ViewDescriptor> subcache = cache.get(viewType);
        if (subcache == null) {
            subcache = new SimpleCache<>();
            cache.add(viewType, subcache);
        }
        ViewDescriptor vd = subcache.get(beanClass);
        if (vd == null) {
            vd = new DefaultViewDescriptor(beanClass, viewType);
            subcache.add(beanClass, vd);
        }

        return vd;
    }
}
