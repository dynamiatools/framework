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
package tools.dynamia.zk.crud;

import tools.dynamia.domain.AbstractEntity;
import tools.dynamia.zk.viewers.mv.MultiViewListener;

public abstract class AbstractExtendedMVListener implements MultiViewListener {

    protected String beanProperty;

    protected Class<AbstractEntity> beanClass;

    public void setBeanProperty(String property) {
        this.beanProperty = property;
    }

    @SuppressWarnings("unchecked")
    public void setBeanClass(String className) throws ClassNotFoundException {
        //noinspection unchecked
        this.beanClass = (Class<AbstractEntity>) Class.forName(className);
    }

    public void setBeanClass(Class<AbstractEntity> beanClass) {
        this.beanClass = beanClass;
    }
}
