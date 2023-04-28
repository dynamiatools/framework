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
package tools.dynamia.zk.reports;

import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.domain.EntityReference;
import tools.dynamia.domain.EntityReferenceRepository;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.ViewDescriptor;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ViewDescriptorDataSource extends JRAbstractBeanDataSource {

    private final ViewDescriptor viewDescriptor;
    private Object currentBean;
    private Iterator iterator;
    private final Collection data;
    private final Map<String, Object> cache = new HashMap<>();

    public ViewDescriptorDataSource(ViewDescriptor viewDescriptor, Collection beanCollection) {
        super(false);
        this.data = beanCollection;
        this.viewDescriptor = viewDescriptor;
        if (this.data != null) {
            this.iterator = this.data.iterator();
        }

    }

    @Override
    public Object getFieldValue(JRField jrfield) {
        Field field = viewDescriptor.getField(jrfield.getName());

        Object value = null;
        if (field.getFieldClass() != null && field.getFieldClass().equals(boolean.class)) {
            value = BeanUtils.invokeBooleanGetMethod(currentBean, field.getName());
        } else {
            value = BeanUtils.invokeGetMethod(currentBean, field.getName());

            value = checkAndLoadEntityReferenceValue(field, value);
        }

        if (value != null && jrfield.getValueClass().equals(String.class)) {
            value = value.toString();
        }
        return value;
    }

    private Object checkAndLoadEntityReferenceValue(Field col, Object value) {
        try {
            String entityAlias = (String) col.getParams().get("entityAlias");
            if (entityAlias != null && value instanceof Serializable) {
                String key = entityAlias + ":" + value;
                Object cacheValue = cache.get(key);
                if (cacheValue == null) {
                    EntityReferenceRepository repo = DomainUtils.getEntityReferenceRepositoryByAlias(entityAlias);
                    if (repo != null) {
                        EntityReference ref = repo.load((Serializable) value);
                        if (ref != null) {
                            value = ref.toString();
                            cache.put(key, value);
                        }
                    }
                } else {
                    value = cacheValue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    @Override
    public void moveFirst() {
        if (this.data != null) {
            this.iterator = this.data.iterator();
        }
    }

    @Override
    public boolean next() {
        boolean hasNext = false;

        if (this.iterator != null) {
            hasNext = this.iterator.hasNext();

            if (hasNext) {
                this.currentBean = this.iterator.next();
            }
        }

        return hasNext;
    }

}
