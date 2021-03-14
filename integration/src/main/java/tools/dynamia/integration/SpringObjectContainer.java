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
package tools.dynamia.integration;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import tools.dynamia.commons.LocaleProvider;
import tools.dynamia.commons.Messages;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The Class SpringObjectContainer.
 *
 * @author Mario A. Serrano Leones
 */
@Component
public class SpringObjectContainer implements ObjectContainer, ApplicationContextAware {

    private LoggingService logger = new SLF4JLoggingService(SpringObjectContainer.class, "Containers");

    /**
     * The app context.
     */
    private ApplicationContext appContext;

    /**
     * Instantiates a new spring object container.
     */
    public SpringObjectContainer() {
        Containers.get().installObjectContainer(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.integration.ObjectContainer#getObject(java.lang.String,
     * java.lang.Class)
     */
    @Override
    public <T> T getObject(String name, Class<T> type) {
        return appContext.getBean(name, type);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.integration.ObjectContainer#getObject(java.lang.Class)
     */
    @Override
    public <T> T getObject(Class<T> type) {
        T bean = null;
        try {
            bean = appContext.getBean(type);
        } catch (NoUniqueBeanDefinitionException e) {
            Optional<T> b = getObjects(type).stream().findFirst();
            if (b.isPresent()) {
                bean = b.get();
            }
        } catch (NoSuchBeanDefinitionException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("No spring bean found with type: " + type);
            }
        }

        return bean;

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.integration.ObjectContainer#getObjects(java.lang.Class)
     */
    @Override
    public <T> List<T> getObjects(Class<T> type) {
        List<T> beans = new ArrayList<>();
        Map<String, T> map = appContext.getBeansOfType(type);
        if (map != null && !map.isEmpty()) {
            for (T t : map.values()) {
                beans.add(t);
            }
        }
        return beans;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dynamia.tools.integration.ObjectContainer#getObject(java.lang.String)
     */
    @Override
    public Object getObject(String name) {
        return appContext.getBean(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.context.ApplicationContextAware#setApplicationContext
     * (org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.appContext = applicationContext;

        Messages.setLocaleProvidersSupplier(() -> Containers.get().findObjects(LocaleProvider.class));
    }
}
