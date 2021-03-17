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
package tools.dynamia.io.impl;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import tools.dynamia.io.Resource;
import tools.dynamia.io.ResourceLocator;

import java.io.IOException;


/**
 * The Class SpringResourceLocator.
 *
 * @author Mario A. Serrano Leones
 */
@Component
public class SpringResourceLocator implements ResourceLocator, ApplicationContextAware {

    /**
     * The app context.
     */
    private ApplicationContext appContext;

    /* (non-Javadoc)
	 * @see com.dynamia.tools.io.ResourceLocator#getResource(java.lang.String)
     */
    @Override
    public Resource getResource(String location) {
        org.springframework.core.io.Resource res = appContext.getResource(location);
        if (res != null) {
            return new SpringResource(res);
        }
        return null;
    }

    /* (non-Javadoc)
	 * @see com.dynamia.tools.io.ResourceLocator#getResources(java.lang.String)
     */
    @Override
    public Resource[] getResources(String location) throws IOException {
        org.springframework.core.io.Resource[] res = appContext.getResources(location);
        if (res != null && res.length > 0) {
            Resource[] resources = new Resource[res.length];
            for (int i = 0; i < resources.length; i++) {
                resources[i] = new SpringResource(res[i]);
            }
            return resources;
        }

        return null;
    }

    /* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.appContext = applicationContext;

    }
}
